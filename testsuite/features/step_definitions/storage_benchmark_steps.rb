# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

### Step definitions for optional real-world storage benchmark workloads.

require 'base64'
require 'json'
require 'shellwords'
require 'time'

UYUNI_BENCH_DEFAULT_SOURCE_REPO = '/uyuni-bench-source/leap-15.6-backports-update'.freeze
UYUNI_BENCH_DEFAULT_RESULTS_PARENT = '/var/spacewalk/uyuni-bench/results/reposync'.freeze

# Return the Kubernetes server pod running Uyuni.
def reposync_benchmark_server_pod
  @reposync_benchmark_server_pod ||= begin
    pod = get_pod_name('server', 'server')
    raise ScriptError, 'Unable to determine the Uyuni server pod name' if pod.nil? || pod.empty?

    pod
  end
end

# Run a shell command on the RKE2 host.
def reposync_benchmark_run_on_host(command, timeout: DEFAULT_TIMEOUT, verbose: true, check_errors: true)
  get_target('server').run_local(command, timeout: timeout, verbose: verbose, check_errors: check_errors)
end

# Run a shell command inside the Uyuni server pod.
def reposync_benchmark_run_in_server_pod(command, timeout: DEFAULT_TIMEOUT, verbose: true, check_errors: true)
  pod = Shellwords.escape(reposync_benchmark_server_pod)
  escaped_command = Shellwords.escape(command)
  reposync_benchmark_run_on_host(
    "kubectl -n uyuni exec #{pod} -- bash -lc #{escaped_command}",
    timeout: timeout,
    verbose: verbose,
    check_errors: check_errors
  )
end

# Path to the mounted source repository as seen by the Uyuni server container.
def reposync_benchmark_source_repo
  ENV.fetch('UYUNI_BENCH_SOURCE_REPO', UYUNI_BENCH_DEFAULT_SOURCE_REPO).delete_suffix('/')
end

# URL passed to spacewalk-repo-sync.
def reposync_benchmark_source_url
  "file://#{reposync_benchmark_source_repo}/"
end

# Storage backend label stored in the benchmark summary.
def reposync_benchmark_storage_backend
  ENV.fetch('UYUNI_BENCH_STORAGE_BACKEND', ENV.fetch('STORAGE_BACKEND', 'unknown'))
end

# Directory for this benchmark run as seen by the Uyuni server container.
def reposync_benchmark_results_dir
  @reposync_benchmark_results_dir ||= begin
    if ENV['UYUNI_BENCH_RESULTS_DIR']
      ENV.fetch('UYUNI_BENCH_RESULTS_DIR').delete_suffix('/')
    else
      timestamp = Time.now.utc.strftime('%Y%m%d%H%M%S')
      File.join(UYUNI_BENCH_DEFAULT_RESULTS_PARENT, "#{reposync_benchmark_storage_backend}-#{timestamp}")
    end
  end
end

# Unique channel label for this benchmark run.
def reposync_benchmark_channel_label
  @reposync_benchmark_channel_label ||= begin
    ENV.fetch('UYUNI_BENCH_CHANNEL_LABEL') do
      "uyuni-bench-reposync-#{Time.now.utc.strftime('%Y%m%d%H%M%S')}"
    end
  end
end

# Parse useful fields from /usr/bin/time -v output.
def reposync_benchmark_parse_time_metrics(output)
  metrics = {}
  output.each_line do |line|
    key, value = line.split(':', 2).map(&:strip)
    next if key.nil? || value.nil?

    normalized_key = key.downcase.gsub(/[^a-z0-9]+/, '_').delete_suffix('_')
    metrics[normalized_key] = value
  end
  metrics
end

# Write JSON content into the server pod without depending on host path sharing.
def reposync_benchmark_write_json_in_server_pod(path, payload)
  encoded = Base64.strict_encode64(JSON.pretty_generate(payload))
  reposync_benchmark_run_in_server_pod(
    "mkdir -p #{Shellwords.escape(File.dirname(path))} && " \
    "printf '%s' #{Shellwords.escape(encoded)} | base64 -d > #{Shellwords.escape(path)}"
  )
end

Given('the reposync benchmark source repository is mounted in the server pod') do
  repo = Shellwords.escape(reposync_benchmark_source_repo)
  command = "test -r #{repo}/repodata/repomd.xml && find #{repo} -name '*.rpm' | wc -l"
  output, = reposync_benchmark_run_in_server_pod(command, timeout: 300)
  @reposync_benchmark_source_package_count = output.to_i

  if @reposync_benchmark_source_package_count.zero?
    raise ScriptError, "No RPM packages found in #{reposync_benchmark_source_repo}"
  end

  log "Reposync benchmark source: #{reposync_benchmark_source_url}"
  log "Reposync benchmark source RPMs: #{@reposync_benchmark_source_package_count}"
end

When('I create a unique reposync benchmark channel') do
  label = reposync_benchmark_channel_label
  name = label
  summary = 'Uyuni reposync storage benchmark channel'
  arch = ENV.fetch('UYUNI_BENCH_CHANNEL_ARCH', 'channel-x86_64')
  parent = ENV.fetch('UYUNI_BENCH_PARENT_CHANNEL', '')

  assert_equal(1, $api_test.channel.software.create(label, name, summary, arch, parent))
  log "Created reposync benchmark channel: #{label}"
end

When('I run the reposync benchmark for the mounted source repository') do
  @reposync_benchmark_summary_path = File.join(reposync_benchmark_results_dir, 'summary.json')
  @reposync_benchmark_stdout_path = File.join(reposync_benchmark_results_dir, 'spacewalk-repo-sync.stdout.log')
  @reposync_benchmark_stderr_path = File.join(reposync_benchmark_results_dir, 'spacewalk-repo-sync.stderr.log')
  @reposync_benchmark_time_path = File.join(reposync_benchmark_results_dir, 'time.verbose.log')

  channel = Shellwords.escape(reposync_benchmark_channel_label)
  source_url = Shellwords.escape(reposync_benchmark_source_url)
  results_dir = Shellwords.escape(reposync_benchmark_results_dir)
  stdout_path = Shellwords.escape(@reposync_benchmark_stdout_path)
  stderr_path = Shellwords.escape(@reposync_benchmark_stderr_path)
  time_path = Shellwords.escape(@reposync_benchmark_time_path)
  timeout = ENV.fetch('UYUNI_BENCH_REPOSYNC_TIMEOUT', '14400').to_i

  command = "mkdir -p #{results_dir} && " \
            "/usr/bin/time -v -o #{time_path} " \
            "spacewalk-repo-sync -c #{channel} --url=#{source_url} " \
            "> #{stdout_path} 2> #{stderr_path}"

  started_at = Time.now.utc
  started_monotonic = Process.clock_gettime(Process::CLOCK_MONOTONIC)
  _output, @reposync_benchmark_exit_code = reposync_benchmark_run_in_server_pod(
    command,
    timeout: timeout,
    check_errors: false
  )
  finished_at = Time.now.utc
  duration = Process.clock_gettime(Process::CLOCK_MONOTONIC) - started_monotonic

  time_output, = reposync_benchmark_run_in_server_pod(
    "test -f #{time_path} && cat #{time_path} || true",
    verbose: false,
    check_errors: false
  )

  @reposync_benchmark_package_count_error = nil
  begin
    @reposync_benchmark_imported_package_count = $api_test.call(
      'channel.software.listAllPackages',
      sessionKey: $api_test.token,
      channelLabel: reposync_benchmark_channel_label
    ).length
  rescue StandardError => e
    @reposync_benchmark_imported_package_count = 0
    @reposync_benchmark_package_count_error = e.message
  end

  summary = {
    workload: 'spacewalk_repo_sync',
    started_at: started_at.iso8601,
    finished_at: finished_at.iso8601,
    duration_seconds: duration.round(3),
    exit_code: @reposync_benchmark_exit_code,
    storage_backend: reposync_benchmark_storage_backend,
    channel_label: reposync_benchmark_channel_label,
    source_url: reposync_benchmark_source_url,
    source_package_count: @reposync_benchmark_source_package_count,
    imported_package_count: @reposync_benchmark_imported_package_count,
    package_count_error: @reposync_benchmark_package_count_error,
    stdout_path: @reposync_benchmark_stdout_path,
    stderr_path: @reposync_benchmark_stderr_path,
    time_verbose_path: @reposync_benchmark_time_path,
    time_metrics: reposync_benchmark_parse_time_metrics(time_output)
  }

  reposync_benchmark_write_json_in_server_pod(@reposync_benchmark_summary_path, summary)
  log "Reposync benchmark summary: #{@reposync_benchmark_summary_path}"
end

Then('the reposync benchmark should finish successfully') do
  raise ScriptError, 'Reposync benchmark did not run' if @reposync_benchmark_exit_code.nil?
  unless @reposync_benchmark_exit_code.zero?
    raise ScriptError, "spacewalk-repo-sync failed with exit code #{@reposync_benchmark_exit_code}"
  end

  minimum_packages = ENV.fetch(
    'UYUNI_BENCH_MIN_PACKAGES',
    @reposync_benchmark_source_package_count.to_s
  ).to_i
  imported = @reposync_benchmark_imported_package_count.to_i
  if imported < minimum_packages
    raise ScriptError, "Expected at least #{minimum_packages} imported packages, got #{imported}"
  end

  summary = Shellwords.escape(@reposync_benchmark_summary_path)
  _output, code = reposync_benchmark_run_in_server_pod("test -s #{summary}", check_errors: false, verbose: false)
  raise ScriptError, "Reposync benchmark summary not found at #{@reposync_benchmark_summary_path}" unless code.zero?

  log "Reposync benchmark channel: #{reposync_benchmark_channel_label}"
  log "Reposync benchmark imported packages: #{imported}"
end
