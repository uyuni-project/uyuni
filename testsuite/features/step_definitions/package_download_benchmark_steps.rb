# Copyright (c) 2026 Akash Kumar <meakash7902@gmail.com>
# Licensed under the terms of the MIT license.

require 'digest'
require 'fileutils'
require 'json'
require 'shellwords'
require 'time'

PACKAGE_DOWNLOAD_DEFAULT_TIMEOUT = 14_400
PACKAGE_DOWNLOAD_CONTROL_TIMEOUT = 600
PACKAGE_DOWNLOAD_CACHE_ROOT = '/var/cache/zypp/packages'.freeze
PACKAGE_DOWNLOAD_IDLE_POLL_SECONDS = 2
PACKAGE_DOWNLOAD_SOURCE_ARCHES = %w[src nosrc source srcpackage].freeze

# Convert Uyuni API package records into the values used by the benchmark. Source and Debian
# packages are excluded, the RPM EVR is built for repository checks, checksums are lowercased to
# match zypper cache directories, and the stable fields are kept for the snapshot and result.json.
def package_download_packages(records)
  packages =
    records.filter_map do |package|
      arch_label = package['arch_label']
      arch = arch_label.sub(/-deb\z/, '')
      next if arch_label.end_with?('-deb') || PACKAGE_DOWNLOAD_SOURCE_ARCHES.include?(arch)

      epoch = package['epoch'].to_s
      version = package['version'].to_s
      release = package['release'].to_s
      evr = +''
      evr << "#{epoch}:" unless epoch.empty?
      evr << version
      evr << "-#{release}" unless release.empty? || release == 'X'

      {
        id: package['id'],
        name: package['name'],
        arch: arch,
        epoch: epoch,
        version: version,
        release: release,
        evr: evr,
        # Salt's zypper provider omits a zero epoch from repository versions.
        cache_evr: evr.sub(/\A0:/, ''),
        checksum: package['checksum'].downcase,
        checksum_type: package['checksum_type'],
        retracted: package['retracted'],
        tuple: [package['name'], arch, evr]
      }
    end
  packages.sort_by { |package| [package[:name], package[:cache_evr], package[:arch], package[:id]] }
end

# Run Salt through kubectl from the testsuite controller.
def package_download_run_salt(inputs, pod, function, arguments = [], context:, timeout_seconds: nil, remote_timeout: nil)
  if timeout_seconds.nil?
    timeout_seconds = [
      inputs[:timeout_seconds],
      PACKAGE_DOWNLOAD_CONTROL_TIMEOUT
    ].min
  end
  # Leave time for Salt's structured result to return after its own deadline.
  remote_timeout ||= timeout_seconds + 60
  salt = [
    'salt',
    '--static',
    '--out=json',
    '--no-color',
    '--timeout',
    timeout_seconds.to_s,
    '--list',
    inputs[:minions].join(','),
    function,
    *arguments
  ]
  command =
    Shellwords.join(
      ['kubectl', '--namespace', 'uyuni', 'exec', '--container', 'uyuni', pod, '--', *salt]
    )
  stdout, stderr, code = get_target('localhost').run_local(
    command,
    separated_results: true,
    check_errors: false,
    timeout: remote_timeout
  )
  raise "#{context} exited with #{code}: #{stderr.to_s.byteslice(0, 4096)}" unless code.zero?

  output = JSON.parse(stdout)
  raise "#{context} returned invalid data" unless output.is_a?(Hash)

  returned = output.keys.sort
  expected = inputs[:minions].sort
  raise "#{context} returned targets #{returned.inspect}, expected #{expected.inspect}" unless returned == expected

  output
rescue JSON::ParserError
  raise "#{context} did not return valid JSON"
end

# Wait until the benchmark minions have no running Salt jobs.
def package_download_wait_for_idle(inputs, pod)
  deadline =
    Process.clock_gettime(Process::CLOCK_MONOTONIC) +
    [inputs[:timeout_seconds], PACKAGE_DOWNLOAD_CONTROL_TIMEOUT].min
  idle_observations = 0

  loop do
    jobs =
      package_download_run_salt(
        inputs,
        pod,
        'saltutil.running',
        context: 'Salt job check',
        timeout_seconds: 30,
        remote_timeout: 90
      )
    raise 'Salt job check returned invalid data' unless jobs.values.all?(Array)

    # Two empty polls avoid treating a brief gap between Salt jobs as idle.
    idle_observations = jobs.values.all?(&:empty?) ? idle_observations + 1 : 0
    return if idle_observations == 2
    raise 'Salt minions did not become idle' if Process.clock_gettime(Process::CLOCK_MONOTONIC) >= deadline

    sleep PACKAGE_DOWNLOAD_IDLE_POLL_SECONDS
  end
end

# Parse the "<size><TAB><path>" lines printed by find on a minion.
def package_download_parse_inventory(stdout)
  stdout.to_s.each_line.filter_map do |line|
    size, path = line.chomp.split("\t", 2)
    { 'path' => path, 'size' => size.to_i } unless path.nil? || path.empty?
  end
end

# Check one minion's downloaded RPM inventory.
def package_download_verify_minion(inputs, minion, output)
  raise "#{minion} cache inventory failed" unless output['retcode'].zero?

  payloads = package_download_parse_inventory(output['stdout'])
  expected_root = "#{PACKAGE_DOWNLOAD_CACHE_ROOT}/#{inputs[:repo_alias]}/"
  valid_payloads =
    payloads.select do |payload|
      payload['size'].positive? &&
        payload['path'].start_with?(expected_root)
    end

  expected = inputs[:packages].group_by { |package| package[:checksum] }
  # Zypper stores each RPM under <repository>/<checksum>/<filename>.
  actual = valid_payloads.group_by { |payload| File.basename(File.dirname(payload['path'])).downcase }
  missing = expected.keys - actual.keys
  extra = actual.keys - expected.keys
  duplicates = actual.reject { |_checksum, files| files.length == 1 }.keys
  errors = []
  errors << "#{payloads.length - valid_payloads.length} invalid payload entries" unless payloads.length == valid_payloads.length
  errors << "#{missing.length} package payloads are missing" unless missing.empty?
  errors << "#{extra.length} unexpected package payloads were downloaded" unless extra.empty?
  errors << "#{duplicates.length} package checksums have multiple payloads" unless duplicates.empty?

  verified_checksums = expected.keys & (actual.keys - duplicates)
  verified_payloads = verified_checksums.flat_map { |checksum| actual[checksum] }
  {
    id: minion,
    status: errors.empty? ? 'passed' : 'failed',
    expected_package_count: inputs[:packages].length,
    expected_payload_count: expected.length,
    verified_package_count: verified_checksums.sum { |checksum| expected[checksum].length },
    verified_payload_count: verified_payloads.length,
    verified_payload_bytes: verified_payloads.sum { |payload| payload['size'] },
    downloaded_payload_count: valid_payloads.length,
    downloaded_payload_bytes: valid_payloads.sum { |payload| payload['size'] },
    extra_payload_count: extra.sum { |checksum| actual[checksum].length },
    extra_payload_bytes: extra.sum { |checksum| actual[checksum].sum { |payload| payload['size'] } },
    missing_records: missing,
    mismatched_records: duplicates,
    extra_payloads: extra,
    errors: errors
  }
end

Given('the Salt package download benchmark inputs are valid') do
  minions = JSON.parse(ENV.fetch('UYUNI_BENCH_MINIONS', ''))
  raise 'UYUNI_BENCH_MINIONS must be a non-empty JSON array' unless minions.is_a?(Array) && !minions.empty?

  # Salt's --list option accepts minion IDs as one comma-separated value.
  # Each ID must be a non-empty String without commas, whitespace, control
  # characters, or a leading hyphen that could look like a CLI option.
  #
  # Valid:   ["minion-1.tf.local", "minion-2.tf.local"]
  # Invalid: ["minion 1"]           # contains whitespace
  # Invalid: ["minion-1,minion-2"]  # contains Salt's list separator
  # Invalid: ["-minion-1"]          # starts like a CLI option
  valid_minions =
    minions.all? do |minion|
      minion.is_a?(String) &&
        !minion.empty? &&
        !minion.start_with?('-') &&
        !minion.match?(/[,\s[:cntrl:]]/)
    end
  raise 'UYUNI_BENCH_MINIONS contains an invalid Salt ID' unless valid_minions
  raise 'UYUNI_BENCH_MINIONS contains duplicate Salt IDs' unless minions.uniq.length == minions.length

  # Reuse the channel synced by the reposync benchmark when it ran earlier in this run set.
  channel = ENV.fetch('UYUNI_BENCH_CHANNEL_LABEL') { $reposync_benchmark_channel_label }.to_s
  raise 'Set UYUNI_BENCH_CHANNEL_LABEL or run the reposync benchmark first' if channel.empty?
  raise 'UYUNI_BENCH_CHANNEL_LABEL is invalid' unless channel.match?(/\A[A-Za-z0-9][A-Za-z0-9_.-]*\z/)

  timeout = reposync_benchmark_integer_env('UYUNI_BENCH_DOWNLOAD_TIMEOUT', PACKAGE_DOWNLOAD_DEFAULT_TIMEOUT.to_s, minimum: 1)
  # Every controller command runs through ssh_exec! with DEFAULT_TIMEOUT, so a longer download
  # timeout would be cut short. Fail here instead of during the measurement.
  raise ScriptError, "DEFAULT_TIMEOUT (#{DEFAULT_TIMEOUT}) must be at least UYUNI_BENCH_DOWNLOAD_TIMEOUT (#{timeout})" if timeout > DEFAULT_TIMEOUT

  @package_download_inputs = {
    minions: minions,
    channel: channel,
    repo_alias: "susemanager:#{channel}",
    storage_backend: reposync_benchmark_storage_backend,
    timeout_seconds: timeout
  }
rescue JSON::ParserError
  raise 'UYUNI_BENCH_MINIONS must contain valid JSON'
end

# Return the Uyuni system IDs subscribed to the benchmark channel.
def package_download_subscribed_system_ids(channel)
  systems =
    $api_test.call(
      'channel.software.listSubscribedSystems',
      sessionKey: $api_test.token,
      channelLabel: channel
    )
  systems.map { |system| system['id'] }
end

# Return true when the server has generated the repository metadata of the channel.
def package_download_repodata_ready?(channel)
  repodata = Shellwords.escape("/var/cache/rhn/repodata/#{channel}")
  _output, code =
    reposync_benchmark_run_in_server_pod(
      "test -f #{repodata}/repomd.xml && ! test -f #{repodata}/solv.new",
      check_errors: false,
      verbose: false
    )
  code.zero?
end

# Subscribe every configured minion to the benchmark channel through the API. The channel synced
# by the reposync benchmark is new, so nothing is subscribed to it yet. Uyuni schedules a channel
# change action that applies the channels state on the minion; wait for it so the repository is
# configured on every minion before the download.
Given('the benchmark minions are subscribed to the benchmark channel') do
  inputs = @package_download_inputs
  api = $api_test

  # spacewalk-repo-sync only queues the metadata generation; taskomatic writes repomd.xml later.
  repeat_until_timeout(timeout: inputs[:timeout_seconds], message: "Repository metadata of #{inputs[:channel]} is not generated") do
    break if package_download_repodata_ready?(inputs[:channel])

    sleep 10
  end

  id_map = api.call('system.getMinionIdMap', sessionKey: api.token)
  system_ids = inputs[:minions].to_h { |minion| [minion, id_map[minion]] }
  unregistered = system_ids.select { |_minion, system_id| system_id.nil? }
  raise "Salt minions are not registered in Uyuni: #{unregistered.keys.join(', ')}" unless unregistered.empty?

  subscribed_ids = package_download_subscribed_system_ids(inputs[:channel])
  parent = api.channel.software.get_details(inputs[:channel])['parent_channel_label'].to_s
  actions =
    system_ids.filter_map do |minion, system_id|
      next if subscribed_ids.include?(system_id)

      # A base channel replaces the current base channel of the minion.
      # A child channel is added to the current child channels of the minion.
      if parent.empty?
        base_label = inputs[:channel]
        child_labels = []
      else
        base_label = parent
        # scheduleChangeChannels rejects child channels that belong to another base channel.
        children = api.call('system.listSubscribedChildChannels', sessionKey: api.token, sid: system_id)
        kept = children.select { |channel| channel['parent_channel_label'] == parent }
        child_labels = (kept.map { |channel| channel['label'] } + [inputs[:channel]]).uniq
      end
      action_id =
        api.call(
          'system.scheduleChangeChannels',
          sessionKey: api.token,
          sid: system_id,
          baseChannelLabel: base_label,
          childLabels: child_labels,
          earliestOccurrence: api.date_now
        )
      log "Scheduled channel change action #{action_id} for #{minion}"
      action_id
    end
  actions.each { |action_id| wait_action_complete(action_id, timeout: PACKAGE_DOWNLOAD_CONTROL_TIMEOUT) }

  subscribed_ids = package_download_subscribed_system_ids(inputs[:channel])
  missing = system_ids.reject { |_minion, system_id| subscribed_ids.include?(system_id) }
  raise "Minions are not subscribed to #{inputs[:channel]}: #{missing.keys.join(', ')}" unless missing.empty?

  @package_download_inputs = inputs.merge(
    system_ids: system_ids,
    subscribed_system_count: subscribed_ids.length
  )
end

# Save a fixed package snapshot before the benchmark. Keep the binary RPMs and their SHA-256 digest
# for comparison after the download. If the digest changes, the result is invalid because the
# tested package set was not stable.
Given('the initial benchmark channel package snapshot is valid') do
  inputs = @package_download_inputs

  packages_response =
    $api_test.call(
      'channel.software.listAllPackages',
      sessionKey: $api_test.token,
      channelLabel: inputs[:channel]
    )
  packages = package_download_packages(packages_response)
  raise 'The benchmark channel has no binary RPM packages' if packages.empty?

  snapshot_records =
    packages.map do |package|
      [package[:id], package[:tuple], package[:checksum_type], package[:checksum], package[:retracted]]
    end
  @package_download_inputs = inputs.merge(
    packages: packages,
    snapshot_captured_at: Time.now.utc,
    snapshot_digest: Digest::SHA256.hexdigest(JSON.generate(snapshot_records.sort_by(&:first)))
  )
end

# Check that every configured minion is ready before the measurement: apply the Salt channels
# state, require SUSE clients of one architecture, verify that the benchmark repository is enabled,
# refresh its metadata, and verify that every RPM of the snapshot is visible to every minion.
Given('the benchmark minions are ready for the benchmark channel') do
  inputs = @package_download_inputs
  pod = reposync_benchmark_server_pod

  # Run `state.apply channels` so Uyuni updates the assigned software channel
  # repository configuration on every minion. The later `pkg.*` calls must use
  # this configuration instead of repository files left by an older run.
  states =
    package_download_run_salt(
      inputs,
      pod,
      'state.apply',
      ['channels'],
      context: 'Channel state preflight'
    )
  failed_states = states.values.flat_map(&:values).reject { |state| state['result'] == true }
  raise 'Channel state preflight failed' unless failed_states.empty?

  # Read the `os_family` and `osarch` Salt grains. This workload uses zypper,
  # so every target must be a SUSE client. Requiring one architecture also keeps
  # the visible RPM set and benchmark results comparable across all minions.
  grains =
    package_download_run_salt(
      inputs,
      pod,
      'grains.item',
      %w[os_family osarch],
      context: 'Client grains preflight'
    )
  client_details =
    inputs[:minions].map do |minion|
      value = grains[minion]
      [value['os_family'], value['osarch']]
    end
  raise 'Package download benchmark requires SUSE clients' unless client_details.all? { |family, _arch| family == 'Suse' }

  osarches = client_details.map(&:last).uniq
  raise "Package download benchmark requires one client architecture: #{osarches.join(', ')}" unless osarches.length == 1

  # Use `pkg.get_repo` to confirm that `susemanager:<channel>` exists and is
  # enabled on every minion. Save the common repository name returned by Salt;
  # `pkg.list_repo_pkgs` uses that name when it checks package availability.
  repos =
    package_download_run_salt(
      inputs,
      pod,
      'pkg.get_repo',
      [inputs[:repo_alias]],
      context: 'Channel repository preflight'
    )
  repos_ok = repos.values.all? { |repo| repo['alias'] == inputs[:repo_alias] && repo['enabled'] == true }
  raise "Repository is missing or disabled: #{inputs[:repo_alias]}" unless repos_ok

  repo_names = repos.values.map { |repo| repo['name'] }.uniq
  raise 'Minions returned different repository names' unless repo_names.length == 1

  inputs = inputs.merge(
    repo_name: repo_names.first,
    client_os_family: 'Suse',
    client_osarch: osarches.first
  )

  # Force `pkg.refresh_db` for only the benchmark repository. This makes the
  # package availability check use current zypper metadata instead of metadata
  # cached before the benchmark channel was prepared.
  refreshed =
    package_download_run_salt(
      inputs,
      pod,
      'pkg.refresh_db',
      ['force=True', "repos=#{inputs[:repo_alias]}"],
      context: 'Repository metadata refresh'
    )
  refresh_ok = refreshed.values.all? { |repositories| [true, false].include?(repositories[inputs[:repo_name]]) }
  raise 'Repository metadata refresh failed' unless refresh_ok

  # Use `pkg.list_repo_pkgs` to read the package versions each minion can see
  # in the benchmark repository, then compare them with the initial Uyuni
  # snapshot. Stop before measurement if any expected RPM is unavailable.
  available =
    package_download_run_salt(
      inputs,
      pod,
      'pkg.list_repo_pkgs',
      ["fromrepo=#{inputs[:repo_name]}"],
      context: 'Channel package availability'
    )
  available.each do |minion, packages|
    raise "#{minion} returned an invalid package list" unless packages.is_a?(Hash)

    missing =
      inputs[:packages].count do |package|
        !packages.fetch(package[:name], []).include?(package[:cache_evr])
      end
    raise "#{minion} cannot see #{missing} channel packages" unless missing.zero?
  end

  @package_download_inputs = inputs
end

When('I clear RPM payload caches on the benchmark minions outside the measurement') do
  inputs = @package_download_inputs
  pod = reposync_benchmark_server_pod
  package_download_wait_for_idle(inputs, pod)
  control_timeout = [inputs[:timeout_seconds], PACKAGE_DOWNLOAD_CONTROL_TIMEOUT].min
  command_timeout = [control_timeout - 15, 1].max
  script = <<~SH
    cache_root=#{PACKAGE_DOWNLOAD_CACHE_ROOT}
    test -d "$cache_root" || exit 10
    find "$cache_root" -type f -delete || exit 11
    test -z "$(find "$cache_root" -type f -print -quit)" || exit 12
  SH
  output =
    package_download_run_salt(
      inputs,
      pod,
      'cmd.run_all',
      [script, 'python_shell=True', "timeout=#{command_timeout}"],
      context: 'RPM cache reset',
      timeout_seconds: control_timeout
    )
  failed =
    output.filter_map do |minion, result|
      minion unless result.is_a?(Hash) && result['retcode'].zero?
    end
  raise "RPM cache reset failed on: #{failed.join(', ')}" unless failed.empty?

  package_download_wait_for_idle(inputs, pod)
end

When('I execute and record the channel package downloads') do
  inputs = @package_download_inputs
  pod = reposync_benchmark_server_pod
  timeout = inputs[:timeout_seconds]
  # zypper download only fetches the matching RPMs into the package cache. It never runs the
  # dependency solver, so conflicting packages in the channel do not matter, and --all-matches
  # keeps every version instead of only the best one.
  zypper = [
    'zypper',
    '--quiet',
    '--non-interactive',
    '--no-refresh',
    'download',
    '--all-matches',
    '--repo',
    inputs[:repo_alias],
    '*'
  ]
  started_at = Time.now.utc
  started_monotonic = Process.clock_gettime(Process::CLOCK_MONOTONIC)
  workload_error = nil
  returns = {}

  begin
    # The minion command ends first, then Salt and SSH get 15 seconds each to return the result.
    returns =
      package_download_run_salt(
        inputs,
        pod,
        'cmd.run_all',
        [
          Shellwords.join(zypper),
          'python_shell=False',
          'output_loglevel=quiet',
          "timeout=#{[timeout - 30, 1].max}"
        ],
        context: 'Package download workload',
        timeout_seconds: [timeout - 15, 1].max,
        remote_timeout: timeout
      )
  rescue StandardError => e
    workload_error = e.message
  end

  finished_at = Time.now.utc
  duration = Process.clock_gettime(Process::CLOCK_MONOTONIC) - started_monotonic
  workload_per_minion =
    inputs[:minions].map do |minion|
      output = returns[minion]
      output = {} unless output.is_a?(Hash)
      minion_errors = []
      minion_errors << "zypper exited with #{output['retcode'].inspect}" unless output['retcode']&.zero?
      {
        id: minion,
        status: minion_errors.empty? ? 'passed' : 'failed',
        retcode: output['retcode'],
        pid: output['pid'],
        stdout: output['stdout'].to_s.byteslice(0, 4096),
        stderr: output['stderr'].to_s.byteslice(0, 4096),
        errors: minion_errors
      }
    end
  workload_errors = []
  workload_errors << workload_error unless workload_error.nil?
  workload_errors << 'one or more minions failed the package download' if workload_per_minion.any? { |minion| minion[:status] == 'failed' }
  workload = {
    status: workload_errors.empty? ? 'passed' : 'failed',
    command: zypper,
    timeout_seconds: timeout,
    uncertain_completion: !workload_error.nil?,
    started_at: started_at.iso8601(6),
    finished_at: finished_at.iso8601(6),
    duration_seconds: duration.round(6),
    returned_target_count: returns.length,
    successful_target_count: workload_per_minion.count { |minion| minion[:status] == 'passed' },
    errors: workload_errors,
    per_minion: workload_per_minion,
    salt_exit_code: workload_error.nil? ? 0 : nil,
    salt_stderr: workload_error,
    raw_stdout: nil
  }

  verification =
    begin
      package_download_wait_for_idle(inputs, pod)
      control_timeout = [inputs[:timeout_seconds], PACKAGE_DOWNLOAD_CONTROL_TIMEOUT].min
      # List every downloaded file with its size, one "<size><TAB><path>" line per file.
      inventory =
        package_download_run_salt(
          inputs,
          pod,
          'cmd.run_all',
          [
            Shellwords.join(['find', PACKAGE_DOWNLOAD_CACHE_ROOT, '-type', 'f', '-printf', '%s\t%p\n']),
            'python_shell=False',
            'output_loglevel=quiet',
            "timeout=#{[control_timeout - 15, 1].max}"
          ],
          context: 'Package cache verification',
          timeout_seconds: control_timeout
        )
      verification_per_minion =
        inputs[:minions].map do |minion|
          package_download_verify_minion(inputs, minion, inventory[minion])
        end

      current_packages =
        package_download_packages(
          $api_test.call(
            'channel.software.listAllPackages',
            sessionKey: $api_test.token,
            channelLabel: inputs[:channel]
          )
        )
      current_records =
        current_packages.map do |package|
          [package[:id], package[:tuple], package[:checksum_type], package[:checksum], package[:retracted]]
        end
      current_digest = Digest::SHA256.hexdigest(JSON.generate(current_records.sort_by(&:first)))
      snapshot_errors = current_digest == inputs[:snapshot_digest] ? [] : ['channel package snapshot changed']

      expected_payloads = inputs[:packages].map { |package| package[:checksum] }.uniq.length
      verification_errors =
        verification_per_minion.flat_map do |minion|
          minion[:errors].map { |error| "#{minion[:id]}: #{error}" }
        end
      verification_errors.concat(snapshot_errors.map { |error| "channel snapshot: #{error}" })
      totals =
        %i[
          verified_package_count
          verified_payload_count
          verified_payload_bytes
          downloaded_payload_count
          downloaded_payload_bytes
          extra_payload_count
          extra_payload_bytes
        ].to_h do |key|
          [key, verification_per_minion.sum { |minion| minion[key] }]
        end
      {
        status: verification_errors.empty? ? 'passed' : 'failed',
        expected_package_count: inputs[:minions].length * inputs[:packages].length,
        expected_payload_count: inputs[:minions].length * expected_payloads,
        verified_package_count: totals[:verified_package_count],
        verified_payload_count: totals[:verified_payload_count],
        verified_payload_bytes: totals[:verified_payload_bytes],
        downloaded_payload_count: totals[:downloaded_payload_count],
        downloaded_payload_bytes: totals[:downloaded_payload_bytes],
        extra_payload_count: totals[:extra_payload_count],
        extra_payload_bytes: totals[:extra_payload_bytes],
        returned_target_count: inventory.length,
        successful_target_count: verification_per_minion.count { |minion| minion[:status] == 'passed' },
        errors: verification_errors,
        per_minion: verification_per_minion,
        channel_snapshot: {
          status: snapshot_errors.empty? ? 'passed' : 'failed',
          checked_at: Time.now.utc.iso8601(6),
          package_count: current_packages.length,
          sha256: current_digest,
          errors: snapshot_errors
        }
      }
    rescue StandardError => e
      expected_payloads = inputs[:packages].map { |package| package[:checksum] }.uniq.length
      {
        status: 'failed',
        expected_package_count: inputs[:minions].length * inputs[:packages].length,
        expected_payload_count: inputs[:minions].length * expected_payloads,
        verified_package_count: 0,
        verified_payload_count: 0,
        verified_payload_bytes: 0,
        downloaded_payload_count: 0,
        downloaded_payload_bytes: 0,
        extra_payload_count: 0,
        extra_payload_bytes: 0,
        returned_target_count: 0,
        successful_target_count: 0,
        errors: ["verification failed: #{e.message}"],
        per_minion: [],
        channel_snapshot: nil
      }
    end

  errors = workload[:errors].map { |error| "workload: #{error}" }
  errors.concat(verification[:errors].map { |error| "verification: #{error}" })
  @package_download_result = {
    schema_version: 2,
    workload: 'zypper.download_all_matches',
    status: errors.empty? ? 'passed' : 'failed',
    storage_backend: inputs[:storage_backend],
    server_pod: pod,
    api_server: ENV.fetch('SERVER', nil),
    channel: inputs[:channel],
    repo_alias: inputs[:repo_alias],
    repo_name: inputs[:repo_name],
    client_os_family: inputs[:client_os_family],
    client_osarch: inputs[:client_osarch],
    timeout_seconds: inputs[:timeout_seconds],
    timeout_scope: 'single_concurrent_all_minion_download',
    started_at: workload[:started_at],
    finished_at: workload[:finished_at],
    duration_seconds: workload[:duration_seconds],
    target_ids: inputs[:minions],
    target_system_ids: inputs[:system_ids],
    expected_target_count: inputs[:minions].length,
    snapshot: {
      kind: 'initial_frozen_channel_binary_rpms',
      captured_at: inputs[:snapshot_captured_at].iso8601(6),
      sha256: inputs[:snapshot_digest],
      package_count: inputs[:packages].length,
      retracted_package_count: inputs[:packages].count { |package| package[:retracted] == true },
      subscribed_system_count: inputs[:subscribed_system_count],
      packages: inputs[:packages].map do |package|
        package.slice(
          :id,
          :name,
          :arch,
          :epoch,
          :version,
          :release,
          :evr,
          :checksum,
          :checksum_type,
          :retracted,
          :tuple
        )
      end
    },
    execution: workload,
    verification: verification,
    errors: errors
  }

  timestamp = Time.parse(workload[:started_at]).strftime('%Y%m%dT%H%M%S.%6NZ')
  directory = File.expand_path("../../results/package-download/#{timestamp}-#{Process.pid}", __dir__)
  FileUtils.mkdir_p(directory)
  @package_download_result_path = File.join(directory, 'result.json')
  File.write(@package_download_result_path, "#{JSON.pretty_generate(@package_download_result)}\n")
  log "Package download result: #{@package_download_result_path}"
end

Then('the package download result report should exist') do
  raise 'Package download result report was not written' unless File.file?(@package_download_result_path)
end

Then('every configured minion should have downloaded every channel package') do
  result = @package_download_result
  next if result[:status] == 'passed'

  details = result[:errors].dup
  result[:execution][:per_minion].each do |minion|
    details << "#{minion[:id]} workload: #{minion[:errors].join('; ')}" unless minion[:errors].empty?
  end
  result[:verification][:per_minion].each do |minion|
    details << "#{minion[:id]} verification: #{minion[:errors].join('; ')}" unless minion[:errors].empty?
  end
  raise "Package download benchmark failed: #{details.join(' | ')}"
end
