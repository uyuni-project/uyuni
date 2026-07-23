# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'time'

# Parse useful fields from /usr/bin/time -v output.
def reposync_benchmark_parse_time_metrics(output)
  metrics = {}
  output.each_line do |line|
    next if line.start_with?('Defaulted container ')

    key, value = line.split(':', 2).map(&:strip)
    next if key.nil? || value.nil?

    normalized_key = key.downcase.gsub(/[^a-z0-9]+/, '_').delete_suffix('_')
    metrics[normalized_key] = value
  end
  metrics
end

# Extract timestamp and message from a spacewalk-repo-sync log line.
def reposync_benchmark_parse_log_line(line)
  chomped = line.chomp
  full_match = chomped.match(/\A(?<timestamp>\d{4}\/\d{2}\/\d{2} \d{2}:\d{2}:\d{2} [+-]\d{2}:\d{2}) (?<message>.*)\z/)
  return [Time.strptime(full_match[:timestamp], '%Y/%m/%d %H:%M:%S %z'), full_match[:message]] if full_match

  short_match = chomped.match(/\A(?<timestamp>\d{2}:\d{2}:\d{2}) (?<message>.*)\z/)
  return if short_match.nil?

  parsed_time = Time.strptime(short_match[:timestamp], '%H:%M:%S')
  [Time.utc(2000, 1, 1, parsed_time.hour, parsed_time.min, parsed_time.sec), short_match[:message]]
rescue ArgumentError
  nil
end

# Redact credentials in repository URLs before writing them to benchmark results.
def reposync_benchmark_redact_repo_url(repo_url)
  repo_url.sub(/\?.*/, '?<redacted>')
end

# Duration between two parsed log markers.
def reposync_benchmark_log_duration(markers, start_key, finish_key)
  return if start_key.nil? || finish_key.nil?
  return if markers[start_key].nil? || markers[finish_key].nil?

  (markers[finish_key] - markers[start_key]).round(3)
end

# First marker key present in the parsed log.
def reposync_benchmark_first_log_marker(markers, keys)
  keys.find { |key| markers[key] }
end

# Rate helper for benchmark throughput values.
def reposync_benchmark_rate(count, duration)
  return if count.nil? || duration.nil? || duration <= 0

  (count.to_f / duration).round(3)
end

# Extract the last integer-only line from command output that may include shell banners.
def reposync_benchmark_last_integer(output)
  count_line = output.each_line.map(&:strip).reverse.find { |line| line.match?(/\A\d+\z/) }
  count_line ? count_line.to_i : 0
end

# Parse one spacewalk-repo-sync lifecycle message.
def reposync_benchmark_parse_reposync_lifecycle_message(stripped, timestamp, markers, repository, status)
  case stripped
  when /\ACommand:/
    markers[:command_at] ||= timestamp
  when 'Sync of channel started.'
    markers[:sync_started_at] ||= timestamp
  when /\ARepo URL:\s+(.+)/
    markers[:repo_url_at] ||= timestamp
    repository[:url] = reposync_benchmark_redact_repo_url(Regexp.last_match(1))
  when 'Sync completed.'
    markers[:sync_completed_at] ||= timestamp
    status[:sync_completed] = true
  else
    false
  end
end

# Parse package count messages.
def reposync_benchmark_parse_reposync_package_count_message(stripped, timestamp, markers, package_metrics, status)
  case stripped
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
  else
    false
  end
end

# Parse package download messages.
def reposync_benchmark_parse_reposync_download_message(stripped, timestamp, markers, package_metrics)
  case stripped
  when 'Downloading packages:'
    markers[:download_started_at] ||= timestamp
  when /\ADownloading total\s+(\d+)\s+files?/
    markers[:download_queue_started_at] ||= timestamp
    package_metrics[:total_files_to_download] = Regexp.last_match(1).to_i
  else
    false
  end
end

# Parse package import messages.
def reposync_benchmark_parse_reposync_import_message(stripped, timestamp, markers, package_metrics, import_batches)
  case stripped
  when 'Importing packages started.'
    markers[:import_started_at] ||= timestamp
  when 'Importing packages to DB:'
    markers[:import_started_at] ||= timestamp
    markers[:db_import_started_at] ||= timestamp
  when /\APackage batch #(\d+) of (\d+) completed/
    import_batches[Regexp.last_match(1).to_i] = true
    package_metrics[:package_import_batch_count] = Regexp.last_match(2).to_i
  when 'Importing packages finished.'
    markers[:import_finished_at] ||= timestamp
    markers[:db_import_finished_at] ||= timestamp
  else
    false
  end
end

# Parse package channel-link messages.
def reposync_benchmark_parse_reposync_link_message(stripped, timestamp, markers, package_metrics)
  case stripped
  when 'Linking packages to the channel.'
    markers[:import_finished_at] ||= timestamp
    markers[:db_import_finished_at] ||= timestamp
    markers[:channel_link_started_at] ||= timestamp
  when /\A(\d+) packages linked/
    markers[:last_package_linked_at] = timestamp
    package_metrics[:packages_linked] = Regexp.last_match(1).to_i
  else
    false
  end
end

# Parse one spacewalk-repo-sync package message.
def reposync_benchmark_parse_reposync_package_message(stripped, timestamp, markers, package_metrics, status, import_batches)
  reposync_benchmark_parse_reposync_package_count_message(stripped, timestamp, markers, package_metrics, status) ||
    reposync_benchmark_parse_reposync_download_message(stripped, timestamp, markers, package_metrics) ||
    reposync_benchmark_parse_reposync_import_message(stripped, timestamp, markers, package_metrics, import_batches) ||
    reposync_benchmark_parse_reposync_link_message(stripped, timestamp, markers, package_metrics)
end

# Parse one spacewalk-repo-sync patch message.
def reposync_benchmark_parse_reposync_patch_message(stripped, timestamp, markers, patch_metrics, status)
  case stripped
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
  else
    false
  end
end

# Parse one spacewalk-repo-sync message into mutable run metrics.
def reposync_benchmark_parse_reposync_log_message(stripped, timestamp, markers, package_metrics, patch_metrics, repository, status, import_batches)
  reposync_benchmark_parse_reposync_lifecycle_message(stripped, timestamp, markers, repository, status) ||
    reposync_benchmark_parse_reposync_package_message(stripped, timestamp, markers, package_metrics, status, import_batches) ||
    reposync_benchmark_parse_reposync_patch_message(stripped, timestamp, markers, patch_metrics, status)
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
    reposync_benchmark_parse_reposync_log_message(
      message.strip,
      timestamp,
      markers,
      package_metrics,
      patch_metrics,
      repository,
      status,
      import_batches
    )
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

  phase_timestamps =
    markers.transform_values(&:iso8601)

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
