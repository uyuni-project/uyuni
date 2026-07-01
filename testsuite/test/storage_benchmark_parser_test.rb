# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'minitest/autorun'
require_relative '../features/support/storage_benchmark_parser'

class StorageBenchmarkParserTest < Minitest::Test
  def test_parse_full_reposync_log_run
    metrics = reposync_benchmark_parse_reposync_log(full_reposync_log)

    assert_equal(1, metrics[:runs_seen])
    assert_equal({ url: 'file:///uyuni-bench-source/leap-15.6-backports-update/?<redacted>' }, metrics[:repository])

    assert_equal(
      {
        packages_in_repo: 3027,
        packages_already_synced: 0,
        packages_to_sync: 3027,
        new_packages_to_download: 3027,
        total_files_to_download: 3027,
        package_import_batch_count: 2,
        package_import_batches_completed: 2,
        packages_linked: 3027
      },
      metrics[:package_metrics]
    )

    assert_equal(
      {
        added_patch_count: 0,
        patches_in_repo: 0,
        new_patches_to_sync: 0
      },
      metrics[:patch_metrics]
    )

    assert_equal(
      {
        total_seconds: 202.0,
        metadata_seconds: 6.0,
        download_seconds: 72.0,
        import_seconds: 119.0,
        db_import_seconds: 119.0,
        channel_link_seconds: 3.0,
        patch_sync_seconds: 2.0,
        post_import_seconds: 5.0
      },
      metrics[:phase_durations_seconds]
    )

    assert_equal(
      {
        downloaded_packages_per_second: 42.042,
        imported_packages_per_second: 25.437,
        linked_packages_per_second: 1009.0,
        synced_patches_per_second: 0.0
      },
      metrics[:throughput]
    )

    assert_equal(
      {
        sync_completed: true,
        no_new_packages: false,
        no_new_patches: true
      },
      metrics[:status]
    )
    assert_equal('2026-06-24T20:24:31+00:00', metrics[:phase_timestamps][:sync_started_at])
  end

  def test_parse_multiple_runs_reports_latest_run
    metrics = reposync_benchmark_parse_reposync_log(multiple_runs_log)

    assert_equal(2, metrics[:runs_seen])
    assert_equal(3027, metrics[:package_metrics][:packages_in_repo])
    assert_equal(3027, metrics[:package_metrics][:packages_to_sync])
    assert_equal(0, metrics[:package_metrics][:new_packages_to_download])
    assert_equal(3027, metrics[:package_metrics][:packages_linked])
    assert_equal(11.0, metrics[:phase_durations_seconds][:total_seconds])
    assert_equal(4.0, metrics[:phase_durations_seconds][:db_import_seconds])
    assert_equal(756.75, metrics[:throughput][:imported_packages_per_second])
    assert(metrics[:status][:sync_completed])
  end

  def test_parse_no_new_packages_run
    metrics = reposync_benchmark_parse_reposync_log(no_new_packages_log)

    assert_equal(1, metrics[:runs_seen])
    assert_equal(10, metrics[:package_metrics][:packages_in_repo])
    assert_equal(0, metrics[:package_metrics][:packages_to_sync])
    assert_equal(0, metrics[:package_metrics][:new_packages_to_download])
    assert_equal(0, metrics[:package_metrics][:total_files_to_download])
    assert(metrics[:status][:no_new_packages])
    assert(metrics[:status][:sync_completed])
  end

  def test_parse_log_line_supports_full_and_short_timestamps
    full_timestamp, full_message = reposync_benchmark_parse_log_line(
      '2026/06/24 20:24:31 +00:00 Sync of channel started.'
    )
    short_timestamp, short_message = reposync_benchmark_parse_log_line('20:30:13 Sync completed.')

    assert_equal(Time.new(2026, 6, 24, 20, 24, 31, '+00:00'), full_timestamp)
    assert_equal('Sync of channel started.', full_message)
    assert_equal(Time.utc(2000, 1, 1, 20, 30, 13), short_timestamp)
    assert_equal('Sync completed.', short_message)
    assert_nil(reposync_benchmark_parse_log_line('Defaulted container "uyuni" out of: uyuni, apache'))
  end

  def test_parse_time_metrics_ignores_container_selection_noise
    metrics = reposync_benchmark_parse_time_metrics(<<~LOG)
      Defaulted container "uyuni" out of: uyuni, apache
      Command being timed: "spacewalk-repo-sync -c channel"
      User time (seconds): 12.34
      Maximum resident set size (kbytes): 123456
    LOG

    assert_equal(
      {
        'command_being_timed' => '"spacewalk-repo-sync -c channel"',
        'user_time_seconds' => '12.34',
        'maximum_resident_set_size_kbytes' => '123456'
      },
      metrics
    )
  end

  def test_last_integer_ignores_shell_noise
    output = <<~OUTPUT
      Defaulted container "uyuni" out of: uyuni, apache
      warning: ignored line
      17
      done
      3027
    OUTPUT

    assert_equal(3027, reposync_benchmark_last_integer(output))
    assert_equal(0, reposync_benchmark_last_integer('no integer here'))
  end

  private

  def full_reposync_log
    <<~LOG
      2026/06/24 20:24:30 +00:00 Command: spacewalk-repo-sync -c uyuni-bench --url=file:///uyuni-bench-source/leap-15.6-backports-update/?token=secret
      2026/06/24 20:24:31 +00:00 Sync of channel started.
      2026/06/24 20:24:32 +00:00   Repo URL: file:///uyuni-bench-source/leap-15.6-backports-update/?token=secret
      2026/06/24 20:24:37 +00:00     Packages in repo:              3027
      2026/06/24 20:24:37 +00:00     Packages already synced:          0
      2026/06/24 20:24:37 +00:00     Packages to sync:              3027
      2026/06/24 20:24:37 +00:00     New packages to download:      3027
      2026/06/24 20:24:37 +00:00   Downloading packages:
      2026/06/24 20:24:38 +00:00   Downloading total 3027 files
      2026/06/24 20:25:49 +00:00   Importing packages to DB:
      2026/06/24 20:26:00 +00:00   Package batch #1 of 2 completed
      2026/06/24 20:26:10 +00:00   Package batch #2 of 2 completed
      2026/06/24 20:27:48 +00:00   Linking packages to the channel.
      2026/06/24 20:27:49 +00:00     1000 packages linked
      2026/06/24 20:27:50 +00:00     2000 packages linked
      2026/06/24 20:27:51 +00:00     3027 packages linked
      2026/06/24 20:27:51 +00:00     Patches in repo: 0.
      2026/06/24 20:27:51 +00:00   No new patch to sync.
      2026/06/24 20:27:53 +00:00 Sync completed.
    LOG
  end

  def multiple_runs_log
    <<~LOG
      Defaulted container "uyuni" out of: uyuni, apache
      20:00:00 Command: spacewalk-repo-sync -c first
      20:00:01 Sync of channel started.
      20:00:02     Packages in repo:              1
      20:00:02   No new packages to sync.
      20:00:03 Sync completed.
      20:30:13 Command: spacewalk-repo-sync -c second
      20:30:14 Sync of channel started.
      20:30:15     Packages in repo:              3027
      20:30:19     Packages already synced:          0
      20:30:19     Packages to sync:              3027
      20:30:19     New packages to download:         0
      20:30:19   Downloading packages:
      20:30:19   Importing packages to DB:
      20:30:23   Linking packages to the channel.
      20:30:25     3027 packages linked
      20:30:25     Patches in repo: 0.
      20:30:25   No new patch to sync.
      20:30:25 Sync completed.
    LOG
  end

  def no_new_packages_log
    <<~LOG
      20:00:00 Command: spacewalk-repo-sync -c channel
      20:00:01 Sync of channel started.
      20:00:02     Packages in repo:              10
      20:00:03   No new packages to sync.
      20:00:04 Sync completed.
    LOG
  end
end
