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

# Convert Uyuni API package records into the values used by the benchmark:
# - exclude source and Debian packages;
# - build the RPM EVR used for repository checks;
# - lowercase checksums used to match zypper cache directories; and
# - retain stable fields for snapshot comparison and result.json.
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

# Check one minion's downloaded RPM inventory.
def package_download_verify_minion(inputs, minion, output)
  raise "#{minion} cache inventory failed" unless output['retcode'].zero?

  payloads = JSON.parse(output['stdout'])
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

  channel = ENV.fetch('UYUNI_BENCH_CHANNEL', '')
  raise 'UYUNI_BENCH_CHANNEL is invalid' unless channel.match?(/\A[A-Za-z0-9][A-Za-z0-9_.-]*\z/)

  timeout = Integer(ENV.fetch('UYUNI_BENCH_TIMEOUT_SECONDS', PACKAGE_DOWNLOAD_DEFAULT_TIMEOUT.to_s), 10)
  raise 'UYUNI_BENCH_TIMEOUT_SECONDS must be between 1 and 86400' unless timeout.between?(1, 86_400)

  @package_download_inputs = {
    minions: minions,
    channel: channel,
    repo_alias: "susemanager:#{channel}",
    storage_class: ENV.fetch('UYUNI_BENCH_STORAGE_CLASS', nil),
    timeout_seconds: timeout
  }
rescue JSON::ParserError
  raise 'UYUNI_BENCH_MINIONS must contain valid JSON'
rescue ArgumentError
  raise 'UYUNI_BENCH_TIMEOUT_SECONDS must be an integer'
end

# Prepare a fixed package snapshot before the benchmark:
# - Read the channel packages, subscribed systems, and Salt-to-Uyuni system ID mapping.
# - Keep the binary RPMs and verify that every selected minion is registered and subscribed.
# - Save the package records and their SHA-256 digest for comparison after the download.
# If the digest changes, the result is invalid because the tested package set was not stable.
Given('the initial configured channel package snapshot is valid') do
  inputs = @package_download_inputs
  api = $api_test

  # Read the current channel state from the Uyuni API.
  packages_response =
    api.call(
      'channel.software.listAllPackages',
      sessionKey: api.token,
      channelLabel: inputs[:channel]
    )
  subscribed =
    api.call(
      'channel.software.listSubscribedSystems',
      sessionKey: api.token,
      channelLabel: inputs[:channel]
    )
  id_map = api.call('system.getMinionIdMap', sessionKey: api.token)

  # Validate the packages and selected minions.
  packages = package_download_packages(packages_response)
  raise 'The configured channel has no binary RPM packages' if packages.empty?

  system_ids = inputs[:minions].to_h { |minion| [minion, id_map[minion]] }
  unregistered = system_ids.select { |_minion, system_id| system_id.nil? }
  raise "Salt minions are not registered in Uyuni: #{unregistered.keys.join(', ')}" unless unregistered.empty?

  subscribed_ids = subscribed.map { |system| system['id'] }
  missing = system_ids.reject { |_minion, system_id| subscribed_ids.include?(system_id) }
  raise "Minions are not subscribed to #{inputs[:channel]}: #{missing.keys.join(', ')}" unless missing.empty?

  # Save the initial package snapshot for the final comparison.
  snapshot_records =
    packages.map do |package|
      [package[:id], package[:tuple], package[:checksum_type], package[:checksum], package[:retracted]]
    end
  @package_download_inputs = inputs.merge(
    packages: packages,
    system_ids: system_ids,
    snapshot_captured_at: Time.now.utc,
    snapshot_digest: Digest::SHA256.hexdigest(JSON.generate(snapshot_records.sort_by(&:first))),
    subscribed_system_count: subscribed_ids.length
  )
end

Given('a ready server pod is reachable from the benchmark controller') do
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
  # The testsuite registers its controller as the 'localhost' target.
  stdout, stderr, code = get_target('localhost').run_local(
    command,
    separated_results: true,
    check_errors: false
  )
  raise "Unable to query the Uyuni server pod: #{stderr}" unless code.zero?

  pods = JSON.parse(stdout)['items']
  ready_pods =
    pods.select do |pod|
      status = pod.fetch('status', {})
      running = status['phase'] == 'Running'
      ready =
        Array(status['conditions']).any? do |condition|
          condition['type'] == 'Ready' && condition['status'] == 'True'
        end

      running && ready
    end
  raise "Expected one ready Uyuni server pod, found #{ready_pods.length}" unless ready_pods.length == 1

  @package_download_pod = ready_pods.first['metadata']['name']
end

# Check that every configured minion is ready before the measurement:
# - Apply the Salt channels state to update the minion repository configuration.
# - Read the OS family and architecture, then require matching SUSE clients.
# - Verify that the configured Uyuni repository exists and is enabled.
# - Refresh the repository metadata.
# - Verify that every RPM from the initial snapshot is available to every minion.
Given('the benchmark minions are ready for the configured channel') do
  inputs = @package_download_inputs
  pod = @package_download_pod

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
  pod = @package_download_pod
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
  pod = @package_download_pod
  timeout = inputs[:timeout_seconds]
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
    timed_out: workload_error.to_s.include?('exited with 124'),
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
      inventory_script = <<~PYTHON
        import json
        import os
        import stat
        import sys

        root = os.path.realpath(sys.argv[1])
        payloads = []
        for directory, _subdirectories, filenames in os.walk(root):
            for filename in filenames:
                path = os.path.join(directory, filename)
                metadata = os.lstat(path)
                if stat.S_ISREG(metadata.st_mode):
                    payloads.append({"path": path, "size": metadata.st_size})
        print(json.dumps(sorted(payloads, key=lambda item: item["path"]), separators=(",", ":")))
      PYTHON
      inventory =
        package_download_run_salt(
          inputs,
          pod,
          'cmd.run_all',
          [
            Shellwords.join(['python3', '-c', inventory_script, PACKAGE_DOWNLOAD_CACHE_ROOT]),
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
    storage_class: inputs[:storage_class],
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
