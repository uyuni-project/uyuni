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

# Extract timestamp and message from a spacewalk-repo-sync log line.
def reposync_benchmark_parse_log_line(line)
  match = line.chomp.match(/\A(?<timestamp>\d{4}\/\d{2}\/\d{2} \d{2}:\d{2}:\d{2} [+-]\d{2}:\d{2}) (?<message>.*)\z/)
  return nil if match.nil?

  [Time.strptime(match[:timestamp], '%Y/%m/%d %H:%M:%S %z'), match[:message]]
rescue ArgumentError
  nil
end

# Redact credentials in repository URLs before writing them to benchmark results.
def reposync_benchmark_redact_repo_url(repo_url)
  repo_url.sub(/\?.*/, '?<redacted>')
end

# Duration between two parsed log markers.
def reposync_benchmark_log_duration(markers, start_key, finish_key)
  return nil if start_key.nil? || finish_key.nil?
  return nil if markers[start_key].nil? || markers[finish_key].nil?

  (markers[finish_key] - markers[start_key]).round(3)
end

# First marker key present in the parsed log.
def reposync_benchmark_first_log_marker(markers, keys)
  keys.find { |key| markers[key] }
end

# Rate helper for benchmark throughput values.
def reposync_benchmark_rate(count, duration)
  return nil if count.nil? || duration.nil? || duration <= 0

  (count.to_f / duration).round(3)
end

# Parse one spacewalk-repo-sync run into benchmark-oriented counters and phase timings.
def reposync_benchmark_parse_reposync_log_run(output)
  markers = {}
  package_metrics = {}
  patch_metrics = { added_patch_count: 0 }
  repository = {}
  status = {
    sync_completed: false,
    no_new_packages: false,
    no_new_patches: false
  }
  import_batches = {}

  output.each_line do |line|
    parsed = reposync_benchmark_parse_log_line(line)
    next if parsed.nil?

    timestamp, message = parsed
    stripped = message.strip

    case stripped
    when /\ACommand:/
      markers[:command_at] ||= timestamp
    when 'Sync of channel started.'
      markers[:sync_started_at] ||= timestamp
    when /\ARepo URL:\s+(.+)/
      markers[:repo_url_at] ||= timestamp
      repository[:url] = reposync_benchmark_redact_repo_url(Regexp.last_match(1))
    when /\APackages in repo:\s+(\d+)/
      markers[:package_metadata_at] ||= timestamp
      package_metrics[:packages_in_repo] = Regexp.last_match(1).to_i
    when /\APackages already synced:\s+(\d+)/
      package_metrics[:packages_already_synced] = Regexp.last_match(1).to_i
    when /\APackages to sync:\s+(\d+)/
      package_metrics[:packages_to_sync] = Regexp.last_match(1).to_i
    when /\ANew packages to download:\s+(\d+)/
      package_metrics[:new_packages_to_download] = Regexp.last_match(1).to_i
    when 'No new packages to sync.'
      status[:no_new_packages] = true
      package_metrics[:packages_to_sync] ||= 0
      package_metrics[:new_packages_to_download] ||= 0
      package_metrics[:total_files_to_download] ||= 0
    when 'Downloading packages:'
      markers[:download_started_at] ||= timestamp
    when /\ADownloading total\s+(\d+)\s+files?/
      markers[:download_queue_started_at] ||= timestamp
      package_metrics[:total_files_to_download] = Regexp.last_match(1).to_i
    when 'Importing packages started.'
      markers[:import_started_at] ||= timestamp
    when 'Importing packages to DB:'
      markers[:db_import_started_at] ||= timestamp
    when /\APackage batch #(\d+) of (\d+) completed/
      import_batches[Regexp.last_match(1).to_i] = true
      package_metrics[:package_import_batch_count] = Regexp.last_match(2).to_i
    when 'Importing packages finished.'
      markers[:import_finished_at] ||= timestamp
      markers[:db_import_finished_at] ||= timestamp
    when 'Linking packages to the channel.'
      markers[:channel_link_started_at] ||= timestamp
    when /\A(\d+) packages linked/
      markers[:last_package_linked_at] = timestamp
      package_metrics[:packages_linked] = Regexp.last_match(1).to_i
    when /\APatches in repo:\s+(\d+)\./
      markers[:patch_metadata_at] ||= timestamp
      patch_metrics[:patches_in_repo] = Regexp.last_match(1).to_i
    when /\AAdd Patch\s+\S+/
      patch_metrics[:added_patch_count] += 1
    when /\ASyncing\s+(\d+)\s+new patch/
      markers[:patch_sync_started_at] ||= timestamp
      patch_metrics[:new_patches_to_sync] = Regexp.last_match(1).to_i
    when 'No new patch to sync.'
      status[:no_new_patches] = true
      patch_metrics[:new_patches_to_sync] ||= 0
    when 'Regenerating bootstrap repositories.'
      markers[:bootstrap_started_at] ||= timestamp
    when /\AUpdating overview of\s+(\d+)\s+systems?/
      markers[:overview_update_started_at] ||= timestamp
      status[:systems_overview_updated_count] = Regexp.last_match(1).to_i
    when 'Sync completed.'
      markers[:sync_completed_at] ||= timestamp
      status[:sync_completed] = true
    end
  end

  package_metrics[:package_import_batches_completed] = import_batches.length unless import_batches.empty?

  metadata_finished_at = reposync_benchmark_first_log_marker(markers, %i[download_started_at import_started_at])
  patch_finished_at = reposync_benchmark_first_log_marker(
    markers,
    %i[bootstrap_started_at overview_update_started_at sync_completed_at]
  )
  bootstrap_finished_at = reposync_benchmark_first_log_marker(markers, %i[overview_update_started_at sync_completed_at])

  phase_durations = {
    total_seconds: reposync_benchmark_log_duration(markers, :sync_started_at, :sync_completed_at),
    metadata_seconds: reposync_benchmark_log_duration(markers, :sync_started_at, metadata_finished_at),
    download_seconds: reposync_benchmark_log_duration(markers, :download_started_at, :import_started_at),
    import_seconds: reposync_benchmark_log_duration(markers, :import_started_at, :import_finished_at),
    db_import_seconds: reposync_benchmark_log_duration(markers, :db_import_started_at, :db_import_finished_at),
    channel_link_seconds: reposync_benchmark_log_duration(markers, :channel_link_started_at, :patch_metadata_at),
    patch_sync_seconds: reposync_benchmark_log_duration(markers, :patch_metadata_at, patch_finished_at),
    bootstrap_regeneration_seconds: reposync_benchmark_log_duration(
      markers,
      :bootstrap_started_at,
      bootstrap_finished_at
    ),
    overview_update_seconds: reposync_benchmark_log_duration(markers, :overview_update_started_at, :sync_completed_at),
    post_import_seconds: reposync_benchmark_log_duration(markers, :import_finished_at, :sync_completed_at)
  }.delete_if { |_key, value| value.nil? }

  download_count = package_metrics[:total_files_to_download] || package_metrics[:new_packages_to_download]
  import_count = package_metrics[:packages_to_sync]
  patch_count = patch_metrics[:new_patches_to_sync] || patch_metrics[:added_patch_count]
  throughput = {
    downloaded_packages_per_second: reposync_benchmark_rate(download_count, phase_durations[:download_seconds]),
    imported_packages_per_second: reposync_benchmark_rate(import_count, phase_durations[:db_import_seconds]),
    linked_packages_per_second: reposync_benchmark_rate(
      package_metrics[:packages_linked],
      phase_durations[:channel_link_seconds]
    ),
    synced_patches_per_second: reposync_benchmark_rate(patch_count, phase_durations[:patch_sync_seconds])
  }.delete_if { |_key, value| value.nil? }

  phase_timestamps = markers.each_with_object({}) do |(key, value), timestamps|
    timestamps[key] = value.iso8601
  end

  {
    repository: repository,
    package_metrics: package_metrics,
    patch_metrics: patch_metrics,
    phase_timestamps: phase_timestamps,
    phase_durations_seconds: phase_durations,
    throughput: throughput,
    status: status
  }
end

# Parse a spacewalk-repo-sync log. If the log contains several runs, report the latest one.
def reposync_benchmark_parse_reposync_log(output)
  runs = []
  current_run = []

  output.each_line do |line|
    parsed = reposync_benchmark_parse_log_line(line)
    next if parsed.nil?

    _timestamp, message = parsed
    if message.strip.start_with?('Command:') && !current_run.empty?
      runs << current_run
      current_run = []
    end
    current_run << line
  end
  runs << current_run unless current_run.empty?

  parsed_runs = runs.map { |run_lines| reposync_benchmark_parse_reposync_log_run(run_lines.join) }
  result = parsed_runs.last || reposync_benchmark_parse_reposync_log_run('')
  result[:runs_seen] = parsed_runs.length
  result
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
  sync_stdout, = reposync_benchmark_run_in_server_pod(
    "test -f #{stdout_path} && cat #{stdout_path} || true",
    verbose: false,
    check_errors: false
  )
  sync_stderr, = reposync_benchmark_run_in_server_pod(
    "test -f #{stderr_path} && cat #{stderr_path} || true",
    verbose: false,
    check_errors: false
  )
  reposync_log_metrics = reposync_benchmark_parse_reposync_log([sync_stdout, sync_stderr].join("\n"))

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
    time_metrics: reposync_benchmark_parse_time_metrics(time_output),
    reposync_log_metrics: reposync_log_metrics
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
