# Copyright (c) 2026 Akash Kumar <meakash7902@gmail.com>
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
  return @reposync_benchmark_server_pod if @reposync_benchmark_server_pod

  command =
    Shellwords.join(
      [
        'kubectl',
        '--namespace',
        'uyuni',
        'get',
        'pods',
        '--selector',
        'app.kubernetes.io/component=server',
        '--output=json'
      ]
    )
  stdout, stderr, code = get_target('localhost').run_local(
    command,
    separated_results: true,
    check_errors: false
  )
  raise ScriptError, "Unable to query the Uyuni server pod: #{stderr}" unless code.zero?

  pods = JSON.parse(stdout)['items']
  raise ScriptError, 'The Uyuni server pod response did not contain an items array' unless pods.is_a?(Array)

  ready_pods =
    pods.select do |pod|
      conditions = pod.dig('status', 'conditions')
      pod.dig('status', 'phase') == 'Running' &&
        conditions.is_a?(Array) &&
        conditions.any? { |condition| condition['type'] == 'Ready' && condition['status'] == 'True' }
    end
  raise ScriptError, "Expected exactly one ready Uyuni server pod, found #{ready_pods.length}" unless ready_pods.length == 1

  pod = ready_pods.first.dig('metadata', 'name')
  raise ScriptError, 'The ready Uyuni server pod has no metadata.name' unless pod.is_a?(String) && !pod.empty?

  @reposync_benchmark_server_pod = pod
rescue JSON::ParserError => e
  raise ScriptError, "Unable to parse the Uyuni server pod response: #{e.message}"
end

# Run a shell command inside the Uyuni server pod from the testsuite controller.
def reposync_benchmark_run_in_server_pod(command, timeout: DEFAULT_TIMEOUT, verbose: true, check_errors: true)
  kubectl_command =
    Shellwords.join(
      [
        'kubectl',
        '--namespace',
        'uyuni',
        'exec',
        '--container',
        'uyuni',
        reposync_benchmark_server_pod,
        '--',
        'sh',
        '-lc',
        command
      ]
    )
  get_target('localhost').run_local(
    kubectl_command,
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
  ENV.fetch('UYUNI_BENCH_STORAGE_BACKEND', 'unknown')
end

# Parse an integer environment variable and raise a clear error for invalid values.
def reposync_benchmark_integer_env(name, default, minimum:)
  value = ENV.fetch(name, default).to_s
  valid_value = value.match?(/\A\d+\z/) && value.to_i >= minimum
  raise ScriptError, "#{name} must be an integer >= #{minimum}, got #{value.inspect}" unless valid_value

  value.to_i
end

# Extract the last integer-only line from command output that may include shell banners.
def reposync_benchmark_last_integer(output)
  count_line = output.each_line.map(&:strip).reverse.find { |line| line.match?(/\A\d+\z/) }
  count_line ? count_line.to_i : 0
end

# Directory for this benchmark run as seen by the Uyuni server container.
def reposync_benchmark_results_dir
  return @reposync_benchmark_results_dir if @reposync_benchmark_results_dir

  @reposync_benchmark_results_dir =
    if ENV['UYUNI_BENCH_RESULTS_DIR']
      ENV['UYUNI_BENCH_RESULTS_DIR'].delete_suffix('/')
    else
      timestamp = Time.now.utc.strftime('%Y%m%d%H%M%S')
      File.join(UYUNI_BENCH_DEFAULT_RESULTS_PARENT, "#{reposync_benchmark_storage_backend}-#{timestamp}")
    end
end

# Unique channel label for this benchmark run.
def reposync_benchmark_channel_label
  return @reposync_benchmark_channel_label if @reposync_benchmark_channel_label

  @reposync_benchmark_channel_label =
    ENV.fetch('UYUNI_BENCH_CHANNEL_LABEL') do
      "uyuni-bench-reposync-#{Time.now.utc.strftime('%Y%m%d%H%M%S')}"
    end
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
  output, = reposync_benchmark_run_in_server_pod(command)
  @reposync_benchmark_source_package_count = reposync_benchmark_last_integer(output)

  raise ScriptError, "No RPM packages found in #{reposync_benchmark_source_repo}" if @reposync_benchmark_source_package_count.zero?

  log "Reposync benchmark source: #{reposync_benchmark_source_url}"
  log "Reposync benchmark source RPMs: #{@reposync_benchmark_source_package_count}"
end

When('I create a unique reposync benchmark channel') do
  label = reposync_benchmark_channel_label
  summary = 'Uyuni reposync storage benchmark channel'
  arch = ENV.fetch('UYUNI_BENCH_CHANNEL_ARCH', 'channel-x86_64')
  parent = ENV.fetch('UYUNI_BENCH_PARENT_CHANNEL', '')

  assert_equal(1, $api_test.channel.software.create(label, label, summary, arch, parent))
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
  timeout = reposync_benchmark_integer_env('UYUNI_BENCH_REPOSYNC_TIMEOUT', '14400', minimum: 1)

  sync_command = "spacewalk-repo-sync -c #{channel} --url=#{source_url}"
  command = "mkdir -p #{results_dir} && " \
            'if [ -x /usr/bin/time ]; then ' \
            "/usr/bin/time -v -o #{time_path} #{sync_command}; " \
            "else : > #{time_path}; #{sync_command}; fi " \
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
    time_verbose_path: @reposync_benchmark_time_path
  }

  reposync_benchmark_write_json_in_server_pod(@reposync_benchmark_summary_path, summary)
  log "Reposync benchmark summary: #{@reposync_benchmark_summary_path}"
end

Then('the reposync benchmark should finish successfully') do
  raise ScriptError, 'Reposync benchmark did not run' if @reposync_benchmark_exit_code.nil?
  raise ScriptError, "spacewalk-repo-sync failed with exit code #{@reposync_benchmark_exit_code}" unless @reposync_benchmark_exit_code.zero?

  minimum_packages = reposync_benchmark_integer_env(
    'UYUNI_BENCH_MIN_PACKAGES',
    @reposync_benchmark_source_package_count.to_s,
    minimum: 0
  )
  imported = @reposync_benchmark_imported_package_count.to_i
  raise ScriptError, "Expected at least #{minimum_packages} imported packages, got #{imported}" if imported < minimum_packages

  summary = Shellwords.escape(@reposync_benchmark_summary_path)
  stdout = Shellwords.escape(@reposync_benchmark_stdout_path)
  stderr = Shellwords.escape(@reposync_benchmark_stderr_path)
  time = Shellwords.escape(@reposync_benchmark_time_path)
  artifact_check = "test -s #{summary} && test -f #{stdout} && test -f #{stderr} && test -f #{time}"
  _output, code = reposync_benchmark_run_in_server_pod(artifact_check, check_errors: false, verbose: false)
  raise ScriptError, "Reposync benchmark artifacts are incomplete under #{reposync_benchmark_results_dir}" unless code.zero?

  log "Reposync benchmark channel: #{reposync_benchmark_channel_label}"
  log "Reposync benchmark imported packages: #{imported}"
  log "Reposync benchmark raw stdout: #{@reposync_benchmark_stdout_path}"
  log "Reposync benchmark raw stderr: #{@reposync_benchmark_stderr_path}"
  log "Reposync benchmark raw time data: #{@reposync_benchmark_time_path}"
end
