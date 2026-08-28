# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'digest'
require 'fileutils'
require 'json'
require 'shellwords'
require 'time'

PACKAGE_DOWNLOAD_BENCHMARK_DEFAULT_TIMEOUT = 14_400
PACKAGE_DOWNLOAD_BENCHMARK_RUN_GRACE_SECONDS = 30
PACKAGE_DOWNLOAD_BENCHMARK_CACHE_ROOT = '/var/cache/zypp/packages'.freeze
PACKAGE_DOWNLOAD_BENCHMARK_IDLE_TIMEOUT = 600
PACKAGE_DOWNLOAD_BENCHMARK_IDLE_POLL_SECONDS = 2
PACKAGE_DOWNLOAD_BENCHMARK_SOURCE_ARCHES = %w[src nosrc source srcpackage].freeze

# Parse one required JSON environment variable.
def package_download_benchmark_json_env(name)
  value = ENV.fetch(name, nil)
  raise "#{name} must be set" if value.nil?

  JSON.parse(value)
rescue JSON::ParserError => e
  raise "#{name} must contain valid JSON: #{e.message}"
end

# Return the validated Salt minion inventory.
def package_download_benchmark_minions
  minions = package_download_benchmark_json_env('UYUNI_BENCH_MINIONS')
  raise 'UYUNI_BENCH_MINIONS must be a non-empty JSON array' unless minions.is_a?(Array) && !minions.empty?

  minions.each do |minion|
    valid = minion.is_a?(String) && !minion.empty? && !minion.start_with?('-') && !minion.match?(/[,\s[:cntrl:]]/)
    raise "Invalid Salt minion ID: #{minion.inspect}" unless valid
  end
  raise 'UYUNI_BENCH_MINIONS must not contain duplicate IDs' unless minions.uniq.length == minions.length

  minions
end

# Return the validated Uyuni software channel label.
def package_download_benchmark_channel
  channel = ENV.fetch('UYUNI_BENCH_CHANNEL', nil)
  raise 'UYUNI_BENCH_CHANNEL must be set' if channel.nil?

  valid = channel.match?(/\A[A-Za-z0-9][A-Za-z0-9_.-]*\z/) && channel.length <= 128
  raise 'UYUNI_BENCH_CHANNEL must be a valid software channel label' unless valid

  channel
end

# Return the validated Salt command timeout.
def package_download_benchmark_timeout
  value = ENV.fetch('UYUNI_BENCH_TIMEOUT_SECONDS', PACKAGE_DOWNLOAD_BENCHMARK_DEFAULT_TIMEOUT.to_s)
  timeout = Integer(value, 10)
  raise 'UYUNI_BENCH_TIMEOUT_SECONDS must be between 1 and 86400' unless timeout.between?(1, 86_400)

  timeout
rescue ArgumentError
  raise 'UYUNI_BENCH_TIMEOUT_SECONDS must be an integer'
end

# Return the optional StorageClass result metadata.
def package_download_benchmark_storage_class
  value = ENV.fetch('UYUNI_BENCH_STORAGE_CLASS', nil)
  return if value.nil?

  raise 'UYUNI_BENCH_STORAGE_CLASS must not be empty' if value.empty?
  raise 'UYUNI_BENCH_STORAGE_CLASS must not exceed 253 characters' if value.length > 253

  value
end

# Load the complete benchmark input contract.
def package_download_benchmark_inputs
  {
    minions: package_download_benchmark_minions,
    channel: package_download_benchmark_channel,
    storage_class: package_download_benchmark_storage_class,
    timeout_seconds: package_download_benchmark_timeout
  }
end

# Return one required string field from a channel package API record.
def package_download_benchmark_package_string(package, field, allow_blank: false)
  value = package[field]
  raise "Package #{package['id'].inspect} has invalid #{field}" unless value.is_a?(String)

  value = value.strip
  raise "Package #{package['id'].inspect} has blank #{field}" if !allow_blank && value.empty?

  value
end

# Convert a channel package API record to the production package tuple.
def package_download_benchmark_package(package)
  raise "Invalid channel package record: #{package.inspect}" unless package.is_a?(Hash)

  id = package['id']
  raise "Channel package has invalid id: #{id.inspect}" unless id.is_a?(Integer) && id.positive?

  name = package_download_benchmark_package_string(package, 'name')
  version = package_download_benchmark_package_string(package, 'version')
  release = package_download_benchmark_package_string(package, 'release', allow_blank: true)
  epoch = package_download_benchmark_package_string(package, 'epoch', allow_blank: true)
  api_arch = package_download_benchmark_package_string(package, 'arch_label')
  checksum = package_download_benchmark_package_string(package, 'checksum')
  checksum_type = package_download_benchmark_package_string(package, 'checksum_type')
  arch = api_arch.sub(/-deb\z/, '')

  raise "Package #{id} has an invalid retracted flag" unless [true, false, nil].include?(package['retracted'])
  raise "Package #{id} has an invalid name" unless name.match?(/\A[A-Za-z0-9][A-Za-z0-9+_.-]*\z/)
  raise "Package #{id} has an invalid architecture" unless arch.match?(/\A[A-Za-z0-9][A-Za-z0-9_.-]*\z/)
  raise "Package #{id} is not a binary package (#{api_arch})" if PACKAGE_DOWNLOAD_BENCHMARK_SOURCE_ARCHES.include?(arch)
  raise "Package #{id} has a non-numeric epoch" unless epoch.empty? || epoch.match?(/\A[0-9]+\z/)
  raise "Package #{id} has an invalid checksum" unless checksum.match?(/\A[0-9A-Fa-f]+\z/)
  raise "Package #{id} has an invalid checksum type" unless checksum_type.match?(/\A[A-Za-z0-9_-]+\z/)
  raise "Package #{id} is not an RPM package (#{api_arch})" if api_arch.end_with?('-deb')

  evr = +''
  evr << "#{epoch}:" unless epoch.empty?
  evr << version
  evr << "-#{release}" unless release.empty? || release == 'X'
  raise "Package #{id} has an invalid universal EVR" unless evr.match?(/\A[A-Za-z0-9][A-Za-z0-9+_.~:^%-]*\z/)

  {
    id: id,
    name: name,
    api_arch: api_arch,
    arch: arch,
    epoch: epoch,
    version: version,
    release: release,
    evr: evr,
    cache_evr: evr.sub(/\A0:/, ''),
    checksum: checksum,
    checksum_type: checksum_type,
    retracted: package['retracted'],
    tuple: [name, arch, evr]
  }
end

# Raise when one value maps to more than one channel package.
def package_download_benchmark_reject_duplicates(packages, description, &identity)
  duplicates = packages.group_by(&identity).select { |_value, records| records.length > 1 }
  return if duplicates.empty?

  details = duplicates.map { |value, records| "#{value.inspect} (ids #{records.map { |record| record[:id] }.join(', ')})" }
  raise "Channel contains duplicate #{description}: #{details.join('; ')}"
end

# Return a benchmark API client pointed at the configured Uyuni endpoint.
def package_download_benchmark_api
  @package_download_benchmark_api ||=
    begin
      host = ENV.fetch('SERVER', nil).strip
      raise 'SERVER must not be empty' if host.empty?

      if $api_test.is_a?(ApiTestXmlrpc)
        ApiTestXmlrpc.new(host)
      else
        ssl_verify = $api_protocol == 'http' ? false : !$is_gh_validation
        ApiTestHttp.new(host, ssl_verify)
      end
    end
end

# Call a benchmark API method with the session key first for XML-RPC compatibility.
def package_download_benchmark_api_call(name, params = {})
  api = package_download_benchmark_api
  api.call(name, { sessionKey: api.token }.merge(params))
end

# Validate and canonicalize one complete channel package response.
def package_download_benchmark_api_packages(api_packages)
  raise 'channel.software.listAllPackages did not return a non-empty array' unless api_packages.is_a?(Array) && !api_packages.empty?

  packages =
    api_packages.map { |package| package_download_benchmark_package(package) }
                .sort_by { |package| [package[:name], package[:cache_evr], package[:arch], package[:id]] }
  package_download_benchmark_reject_duplicates(packages, 'package IDs') { |package| package[:id] }
  packages
end

# Return the stable digest for one canonical channel package set.
def package_download_benchmark_snapshot_digest(packages)
  records = packages.map { |package| [package[:id], package[:tuple], package[:checksum_type], package[:checksum], package[:retracted]] }
  Digest::SHA256.hexdigest(JSON.generate(records))
end

# Map the configured Salt minion IDs to Uyuni system IDs.
def package_download_benchmark_system_ids(minions)
  id_map = package_download_benchmark_api_call('system.getMinionIdMap')
  raise 'system.getMinionIdMap did not return an object' unless id_map.is_a?(Hash)

  invalid = id_map.reject { |minion, system_id| minion.is_a?(String) && !minion.empty? && system_id.is_a?(Integer) && system_id.positive? }
  raise "system.getMinionIdMap returned invalid entries: #{invalid.inspect}" unless invalid.empty?

  missing = minions - id_map.keys
  raise "Benchmark minions are not registered Salt systems: #{missing.join(', ')}" unless missing.empty?

  minions.to_h { |minion| [minion, id_map[minion]] }
end

# Validate the initial all-package channel snapshot and configured subscriptions.
def package_download_benchmark_snapshot(inputs)
  api_packages = package_download_benchmark_api_call('channel.software.listAllPackages', channelLabel: inputs[:channel])
  subscribed = package_download_benchmark_api_call('channel.software.listSubscribedSystems', channelLabel: inputs[:channel])
  raise 'channel.software.listSubscribedSystems did not return an array' unless subscribed.is_a?(Array)

  subscribed_ids =
    subscribed.map do |system|
      valid = system.is_a?(Hash) &&
              system['id'].is_a?(Integer) &&
              system['id'].positive? &&
              system['name'].is_a?(String) &&
              !system['name'].empty?
      raise "Invalid subscribed system record: #{system.inspect}" unless valid

      system['id']
    end
  raise 'The channel has duplicate subscribed system IDs' unless subscribed_ids.uniq.length == subscribed_ids.length

  system_ids = package_download_benchmark_system_ids(inputs[:minions])
  missing_subscriptions = system_ids.reject { |_minion, system_id| subscribed_ids.include?(system_id) }
  raise "Benchmark minions are not subscribed to #{inputs[:channel]}: #{missing_subscriptions.keys.join(', ')}" unless missing_subscriptions.empty?

  packages = package_download_benchmark_api_packages(api_packages)
  repo_alias = "susemanager:#{inputs[:channel]}"

  inputs.merge(
    packages: packages,
    repo_alias: repo_alias,
    system_ids: system_ids,
    snapshot_captured_at: Time.now.utc,
    snapshot_digest: package_download_benchmark_snapshot_digest(packages),
    subscribed_system_count: subscribed_ids.length
  )
end

# Build a safely serialized command with a process-level timeout.
def package_download_benchmark_timeout_command(argv, timeout:)
  Shellwords.join(['timeout', '--signal=TERM', '--kill-after=10s', "#{timeout}s", *argv])
end

# Build a kubectl exec command for the Uyuni container.
def package_download_benchmark_kubectl(pod, argv)
  ['kubectl', '--namespace', 'uyuni', 'exec', '--container', 'uyuni', pod, '--', *argv]
end

# Find one ready Uyuni server pod.
def package_download_benchmark_server_pod
  timeout = 30
  command =
    package_download_benchmark_timeout_command(
      [
        'kubectl',
        '--namespace',
        'uyuni',
        'get',
        'pods',
        '--selector',
        'app.kubernetes.io/component=server',
        '--output=json'
      ],
      timeout: timeout
    )
  stdout, stderr, code = get_target('localhost').run(
    command,
    runs_in_container: false,
    separated_results: true,
    check_errors: false,
    timeout: timeout + PACKAGE_DOWNLOAD_BENCHMARK_RUN_GRACE_SECONDS
  )
  raise "Unable to query the Uyuni server pod: #{stderr}" unless code.zero?

  pods = JSON.parse(stdout)['items']
  raise 'The Uyuni server pod response did not contain an items array' unless pods.is_a?(Array)

  ready_pods =
    pods.select do |pod|
      conditions = pod.dig('status', 'conditions')
      pod.dig('status', 'phase') == 'Running' &&
        conditions.is_a?(Array) &&
        conditions.any? { |condition| condition['type'] == 'Ready' && condition['status'] == 'True' }
    end
  raise "Expected exactly one ready Uyuni server pod, found #{ready_pods.length}" unless ready_pods.length == 1

  pod_name = ready_pods.first.dig('metadata', 'name')
  raise 'The ready Uyuni server pod has no metadata.name' unless pod_name.is_a?(String) && !pod_name.empty?

  pod_name
rescue JSON::ParserError, KeyError => e
  raise "Unable to parse the Uyuni server pod response: #{e.message}"
end

# Build one synchronous Salt list-target command.
def package_download_benchmark_salt(inputs, function, arguments, timeout_seconds: inputs[:timeout_seconds])
  [
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
end

# Parse the complete JSON result emitted by Salt in static mode.
def package_download_benchmark_parse_salt(stdout, context)
  output = JSON.parse(stdout)
  raise "#{context} did not return a JSON object" unless output.is_a?(Hash)

  output
rescue JSON::ParserError => e
  raise "#{context} did not return valid JSON: #{e.message}"
end

# Return missing and unexpected Salt targets.
def package_download_benchmark_target_errors(output, expected_minions)
  returned_minions = output.keys
  errors = (expected_minions - returned_minions).map { |minion| "missing target #{minion}" }
  errors.concat((returned_minions - expected_minions).map { |minion| "unexpected target #{minion}" })
  errors
end

# Run and validate one structured Salt call outside the measurement.
def package_download_benchmark_salt_call(inputs, pod, function, arguments, context, timeout_seconds: inputs[:timeout_seconds])
  salt = package_download_benchmark_salt(inputs, function, arguments, timeout_seconds: timeout_seconds)
  timeout = timeout_seconds + 60
  command = package_download_benchmark_timeout_command(package_download_benchmark_kubectl(pod, salt), timeout: timeout)
  stdout, stderr, code = get_target('localhost').run(
    command,
    runs_in_container: false,
    separated_results: true,
    check_errors: false,
    timeout: timeout + PACKAGE_DOWNLOAD_BENCHMARK_RUN_GRACE_SECONDS
  )
  raise "#{context} exited with #{code}: #{package_download_benchmark_excerpt(stderr)}" unless code.zero?

  output = package_download_benchmark_parse_salt(stdout, context)
  errors = package_download_benchmark_target_errors(output, inputs[:minions])
  raise "#{context} returned invalid targets: #{errors.join('; ')}" unless errors.empty?

  output
end

# Return failed or malformed states from one Salt state.apply response.
def package_download_benchmark_state_errors(state_output)
  return ['malformed state.apply return'] unless state_output.is_a?(Hash)
  return ['empty state.apply return'] if state_output.empty?

  state_output.filter_map do |state_id, state|
    next "#{state_id}: malformed state return" unless state.is_a?(Hash)
    next if state['result'] == true

    comment = package_download_benchmark_excerpt(state['comment'].to_s)
    "#{state_id}: result=#{state['result'].inspect}, comment=#{comment.inspect}"
  end
end

# Validate the exact enabled repository alias on every minion.
def package_download_benchmark_validate_repositories(inputs, repos)
  errors =
    repos.filter_map do |minion, repo|
      valid = repo.is_a?(Hash) &&
              repo['alias'] == inputs[:repo_alias] &&
              repo['name'].is_a?(String) &&
              !repo['name'].empty? &&
              repo['enabled'] == true
      next if valid

      "#{minion}: #{inputs[:repo_alias]} is missing or disabled"
    end
  raise "Channel repository preflight failed: #{errors.join('; ')}" unless errors.empty?

  names = repos.values.map { |repo| repo['name'] }.uniq
  raise "Channel repository preflight returned different names: #{names.join(', ')}" unless names.length == 1

  names.first
end

# Refresh and validate all configured package repositories outside the measurement.
def package_download_benchmark_refresh_repositories(inputs, pod)
  refreshed = package_download_benchmark_salt_call(
    inputs,
    pod,
    'pkg.refresh_db',
    ['force=True', "repos=#{inputs[:repo_alias]}"],
    'Repository metadata refresh'
  )
  errors =
    refreshed.filter_map do |minion, repositories|
      valid = repositories.is_a?(Hash) &&
              repositories.keys == [inputs[:repo_name]] &&
              [true, false].include?(repositories[inputs[:repo_name]])
      "#{minion}: malformed selected-repository pkg.refresh_db return" unless valid
    end
  raise "Repository metadata refresh failed: #{errors.join('; ')}" unless errors.empty?
end

# Prove that every frozen package name and EVR is visible in the selected client repository.
def package_download_benchmark_validate_available_packages(inputs, pod)
  available = package_download_benchmark_salt_call(
    inputs,
    pod,
    'pkg.list_repo_pkgs',
    ["fromrepo=#{inputs[:repo_name]}"],
    'Channel package availability preflight'
  )
  errors =
    available.filter_map do |minion, packages|
      unless packages.is_a?(Hash) &&
             packages.all? { |name, versions| name.is_a?(String) && versions.is_a?(Array) && versions.all?(String) }

        next "#{minion}: malformed pkg.list_repo_pkgs return"
      end

      missing = inputs[:packages].reject { |package| packages.fetch(package[:name], []).include?(package[:cache_evr]) }
      next if missing.empty?

      examples = missing.first(20).map { |package| "#{package[:id]}:#{package[:name]}-#{package[:evr]}" }
      "#{minion}: #{missing.length} channel packages are unavailable (#{examples.join(', ')})"
    end
  raise "Channel package availability preflight failed: #{errors.join('; ')}" unless errors.empty?
end

# Apply channel configuration and validate client/provider compatibility.
def package_download_benchmark_preflight(inputs, pod)
  channel_states = package_download_benchmark_salt_call(inputs, pod, 'state.apply', ['channels'], 'Channel state preflight')
  state_errors =
    channel_states.flat_map do |minion, state_output|
      package_download_benchmark_state_errors(state_output).map { |error| "#{minion}: #{error}" }
    end
  raise "Channel state preflight failed: #{state_errors.join('; ')}" unless state_errors.empty?

  grains = package_download_benchmark_salt_call(inputs, pod, 'grains.item', %w[os_family osarch], 'Client grains preflight')
  client_details =
    inputs[:minions].map do |minion|
      value = grains[minion]
      valid = value.is_a?(Hash) && value['os_family'].is_a?(String) && value['osarch'].is_a?(String)
      raise "#{minion}: malformed os_family/osarch grains" unless valid

      [minion, value['os_family'], value['osarch']]
    end
  non_suse = client_details.reject { |_minion, os_family, _osarch| os_family == 'Suse' }
  raise "Package download benchmark requires SUSE clients: #{non_suse.map(&:first).join(', ')}" unless non_suse.empty?

  osarches = client_details.map(&:last).uniq
  raise "Package download benchmark requires one common client osarch, found #{osarches.join(', ')}" unless osarches.length == 1

  osarch = osarches.first

  repos = package_download_benchmark_salt_call(inputs, pod, 'pkg.get_repo', [inputs[:repo_alias]], 'Channel repository preflight')
  repo_name = package_download_benchmark_validate_repositories(inputs, repos)
  inputs = inputs.merge(repo_name: repo_name)
  package_download_benchmark_refresh_repositories(inputs, pod)
  package_download_benchmark_validate_available_packages(inputs, pod)

  inputs.merge(client_os_family: 'Suse', client_osarch: osarch)
end

# Return all Salt jobs currently active on the benchmark minions.
def package_download_benchmark_running_jobs(inputs, pod)
  output = package_download_benchmark_salt_call(
    inputs,
    pod,
    'saltutil.running',
    [],
    'Salt job idle check',
    timeout_seconds: 30
  )
  output.each do |minion, jobs|
    raise "#{minion}: saltutil.running returned malformed data" unless jobs.is_a?(Array)

    invalid = jobs.reject { |job| job.is_a?(Hash) && job['fun'].is_a?(String) && !job['fun'].empty? }
    raise "#{minion}: saltutil.running returned malformed jobs" unless invalid.empty?
  end
  output
end

# Require two consecutive complete observations with no active Salt jobs.
def package_download_benchmark_wait_for_idle(inputs, pod)
  wait_seconds = [inputs[:timeout_seconds], PACKAGE_DOWNLOAD_BENCHMARK_IDLE_TIMEOUT].min
  deadline = Process.clock_gettime(Process::CLOCK_MONOTONIC) + wait_seconds
  idle_observations = 0
  latest = {}
  loop do
    latest = package_download_benchmark_running_jobs(inputs, pod)
    idle_observations = latest.values.all?(&:empty?) ? idle_observations + 1 : 0
    return if idle_observations >= 2

    break if Process.clock_gettime(Process::CLOCK_MONOTONIC) >= deadline

    sleep PACKAGE_DOWNLOAD_BENCHMARK_IDLE_POLL_SECONDS
  end

  details =
    latest.filter_map do |minion, jobs|
      next if jobs.empty?

      "#{minion}: #{jobs.map { |job| "#{job['fun']} (jid=#{job.fetch('jid', 'unknown')})" }.join(', ')}"
    end
  details << 'two consecutive idle observations were not completed' if details.empty?
  raise "Salt minions did not become idle within #{wait_seconds} seconds: #{details.join('; ')}"
end

# Clear package payload caches on every benchmark minion outside the measurement.
def package_download_benchmark_clear_cache(inputs, pod)
  command_timeout = [inputs[:timeout_seconds] - 15, 1].max
  script = <<~SH
    cache_root=#{PACKAGE_DOWNLOAD_BENCHMARK_CACHE_ROOT}
    test -d "$cache_root" || { echo "Missing RPM payload cache: $cache_root" >&2; exit 10; }
    find "$cache_root" -type f -delete || exit 11
    remaining=$(find "$cache_root" -type f -print -quit) || exit 12
    test -z "$remaining" ||
      { echo "Package payload cache is not empty" >&2; exit 12; }
  SH
  package_download_benchmark_wait_for_idle(inputs, pod)
  salt =
    package_download_benchmark_salt(
      inputs,
      'cmd.run_all',
      [script, 'python_shell=True', "timeout=#{command_timeout}"]
    )
  timeout = inputs[:timeout_seconds] + 60
  command = package_download_benchmark_timeout_command(package_download_benchmark_kubectl(pod, salt), timeout: timeout)
  stdout, stderr, code = get_target('localhost').run(
    command,
    runs_in_container: false,
    separated_results: true,
    check_errors: false,
    timeout: timeout + PACKAGE_DOWNLOAD_BENCHMARK_RUN_GRACE_SECONDS
  )
  raise "RPM cache reset exited with #{code}: #{stderr}" unless code.zero?

  output = package_download_benchmark_parse_salt(stdout, 'RPM cache reset')
  errors = package_download_benchmark_target_errors(output, inputs[:minions])
  output.each do |minion, result|
    if !result.is_a?(Hash)
      errors << "#{minion}: malformed cmd.run_all return"
    elsif result['retcode'] != 0
      errors << "#{minion}: cache reset retcode=#{result['retcode'].inspect}, stderr=#{package_download_benchmark_excerpt(result['stderr'].to_s).inspect}"
    end
  end
  raise "RPM cache reset failed: #{errors.join('; ')}" unless errors.empty?

  package_download_benchmark_wait_for_idle(inputs, pod)
end

# Return the argv used for the measured raw repository download.
def package_download_benchmark_zypper_argv(inputs)
  [
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
end

# Return the safely serialized command passed to cmd.run_all.
def package_download_benchmark_zypper_command(inputs)
  Shellwords.join(package_download_benchmark_zypper_argv(inputs))
end

# Summarize the raw-download command returned by one minion.
def package_download_benchmark_minion_result(minion, command_output)
  result = {
    id: minion,
    status: 'failed',
    retcode: nil,
    pid: nil,
    stdout: nil,
    stderr: nil,
    errors: []
  }
  unless command_output.is_a?(Hash)
    result[:errors] << 'malformed cmd.run_all return'
    return result
  end

  retcode = command_output['retcode']
  stdout = command_output['stdout']
  stderr = command_output['stderr']
  result[:errors] << "invalid retcode #{retcode.inspect}" unless retcode.is_a?(Integer)
  result[:errors] << "zypper download exited with #{retcode}" if retcode.is_a?(Integer) && !retcode.zero?
  result[:errors] << 'malformed stdout' unless stdout.is_a?(String)
  result[:errors] << 'malformed stderr' unless stderr.is_a?(String)
  result.merge!(
    status: result[:errors].empty? ? 'passed' : 'failed',
    retcode: retcode,
    pid: command_output['pid'],
    stdout: package_download_benchmark_excerpt(stdout),
    stderr: package_download_benchmark_excerpt(stderr)
  )
  result
end

# Return a bounded command-output excerpt for an actionable report.
def package_download_benchmark_excerpt(value, limit = 4096)
  return if value.nil?

  text = value.to_s.scrub
  return if text.empty?
  return text if text.bytesize <= limit

  "#{text.byteslice(0, limit).scrub}... [truncated]"
end

# Download every package from the selected repository on all minions concurrently.
def package_download_benchmark_workload(inputs, pod)
  timeout_seconds = inputs[:timeout_seconds]
  command_timeout = [timeout_seconds - 30, 1].max
  salt_timeout = [timeout_seconds - 15, 1].max
  salt = package_download_benchmark_salt(
    inputs,
    'cmd.run_all',
    [
      package_download_benchmark_zypper_command(inputs),
      'python_shell=False',
      'output_loglevel=quiet',
      "timeout=#{command_timeout}"
    ],
    timeout_seconds: salt_timeout
  )
  argv = package_download_benchmark_kubectl(pod, salt)
  started_at = Time.now.utc
  started_monotonic = Process.clock_gettime(Process::CLOCK_MONOTONIC)

  stdout = ''
  stderr = ''
  code = nil
  exception = nil
  begin
    command = package_download_benchmark_timeout_command(argv, timeout: timeout_seconds)
    stdout, stderr, code = get_target('localhost').run(
      command,
      runs_in_container: false,
      separated_results: true,
      check_errors: false,
      timeout: timeout_seconds + PACKAGE_DOWNLOAD_BENCHMARK_RUN_GRACE_SECONDS
    )
  rescue StandardError => e
    exception = "#{e.class}: #{package_download_benchmark_excerpt(e.message)}"
  end

  finished_at = Time.now.utc
  duration_seconds = Process.clock_gettime(Process::CLOCK_MONOTONIC) - started_monotonic
  errors = []
  errors << "command raised #{exception}" unless exception.nil?
  errors << "command exited with #{code.inspect}: #{package_download_benchmark_excerpt(stderr)}" unless code&.zero?

  returns = {}
  unless stdout.empty?
    begin
      returns = package_download_benchmark_parse_salt(stdout, 'Package download workload')
    rescue StandardError => e
      errors << e.message
    end
  end
  target_errors = package_download_benchmark_target_errors(returns, inputs[:minions])
  errors.concat(target_errors)

  minions =
    inputs[:minions].filter_map do |minion|
      package_download_benchmark_minion_result(minion, returns[minion]) if returns.key?(minion)
    end
  failed_targets = minions.reject { |minion| minion[:status] == 'passed' }
  errors << 'one or more minions failed the package download workload' unless failed_targets.empty?
  successful_target_count = minions.count { |minion| minion[:status] == 'passed' }
  no_return =
    returns.values.any? do |value|
      value.is_a?(String) && value.match?(/\A(?:Minion did not return(?:\. \[No response\])?|Timed out waiting for minion response)\z/i)
    end
  timed_out = [124, 137].include?(code) || no_return
  uncertain_completion = !exception.nil? || timed_out || !target_errors.empty?

  {
    status: errors.empty? ? 'passed' : 'failed',
    command: package_download_benchmark_zypper_argv(inputs),
    timeout_seconds: timeout_seconds,
    timed_out: timed_out,
    uncertain_completion: uncertain_completion,
    started_at: started_at.iso8601(6),
    finished_at: finished_at.iso8601(6),
    duration_seconds: duration_seconds.round(6),
    returned_target_count: returns.length,
    successful_target_count: successful_target_count,
    errors: errors,
    per_minion: minions,
    salt_exit_code: code,
    salt_stderr: package_download_benchmark_excerpt(stderr),
    raw_stdout: returns.empty? ? package_download_benchmark_excerpt(stdout) : nil
  }
end

# Return a Python program that emits every regular package-cache file as JSON.
def package_download_benchmark_inventory_script
  <<~PYTHON
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
    print(json.dumps(sorted(payloads, key=lambda payload: payload["path"]), separators=(",", ":")))
  PYTHON
end

# Return the safely serialized cache-inventory command.
def package_download_benchmark_inventory_command
  Shellwords.join(['python3', '-c', package_download_benchmark_inventory_script, PACKAGE_DOWNLOAD_BENCHMARK_CACHE_ROOT])
end

# Parse one minion's structured cache-inventory command return.
def package_download_benchmark_cache_payloads(command_output)
  errors = []
  return [[], ['malformed cache inventory cmd.run_all return']] unless command_output.is_a?(Hash)

  retcode = command_output['retcode']
  stdout = command_output['stdout']
  errors << "cache inventory exited with #{retcode.inspect}" unless retcode&.zero?
  errors << 'cache inventory returned malformed stdout' unless stdout.is_a?(String)
  return [[], errors] unless errors.empty?

  payloads = JSON.parse(stdout)
  return [[], ['cache inventory did not return a JSON array']] unless payloads.is_a?(Array)

  invalid =
    payloads.reject do |payload|
      payload.is_a?(Hash) &&
        payload['path'].is_a?(String) &&
        !payload['path'].empty? &&
        payload['size'].is_a?(Integer)
    end
  errors << "#{invalid.length} malformed cache inventory entries" unless invalid.empty?
  [payloads - invalid, errors]
rescue JSON::ParserError => e
  [[], ["cache inventory did not return valid JSON: #{e.message}"]]
end

# Return all integrity errors for one cached RPM.
def package_download_benchmark_payload_errors(payload, repo_alias, checksum)
  path = payload['path']
  size = payload['size']
  expanded_path = File.expand_path(path)
  expected_root = "#{PACKAGE_DOWNLOAD_BENCHMARK_CACHE_ROOT}/#{repo_alias}/"
  errors = []
  errors << "invalid size #{size.inspect}" unless size.positive?
  errors << "path is not canonical: #{path}" unless path == expanded_path
  errors << "path is outside #{expected_root}" unless expanded_path.start_with?(expected_root)
  errors << "path is not an RPM: #{path}" unless File.extname(expanded_path).casecmp?('.rpm')
  checksum_directory = File.basename(File.dirname(expanded_path))
  errors << "checksum directory is not #{checksum}" unless checksum_directory.casecmp?(checksum)
  errors
end

# Return the compact frozen-package identity used in verification errors.
def package_download_benchmark_package_reference(package)
  {
    package_id: package[:id],
    name: package[:name],
    arch: package[:arch],
    evr: package[:evr],
    checksum: package[:checksum]
  }
end

# Match one frozen channel artifact to its checksum-named cache payload.
def package_download_benchmark_verify_artifact(checksum, packages, candidates, repo_alias)
  references = packages.map { |package| package_download_benchmark_package_reference(package) }
  return [nil, references, nil] if candidates.empty?

  candidate_errors =
    candidates.map do |payload|
      [payload, package_download_benchmark_payload_errors(payload, repo_alias, checksum)]
    end
  match = candidate_errors.find { |_payload, payload_errors| payload_errors.empty? }
  return [match.first, [], nil] if candidates.length == 1 && match

  mismatch = {
    packages: references,
    candidates: candidate_errors.map { |payload, payload_errors| payload.merge('errors' => payload_errors) }
  }
  [nil, [], mismatch]
end

# Verify every frozen channel artifact on one minion by its repository checksum path.
def package_download_benchmark_verify_minion(minion, command_output, inputs)
  payloads, errors = package_download_benchmark_cache_payloads(command_output)
  expected = inputs[:packages].group_by { |package| package[:checksum].downcase }
  actual = payloads.group_by { |payload| File.basename(File.dirname(payload['path'])).downcase }
  verified_package_count = 0
  verified_payloads = []
  missing = []
  mismatched = []
  consumed_paths = {}

  expected.each do |checksum, packages|
    candidates = actual.fetch(checksum, [])
    candidates.each { |payload| consumed_paths[payload['path']] = true }
    verified, missing_records, mismatch =
      package_download_benchmark_verify_artifact(checksum, packages, candidates, inputs[:repo_alias])
    missing.concat(missing_records)
    mismatched << mismatch if mismatch
    if verified
      verified_package_count += packages.length
      verified_payloads << verified
    end
  end

  extras = payloads.reject { |payload| consumed_paths.key?(payload['path']) }
  errors << "#{missing.length} channel package records are missing" unless missing.empty?
  errors << "#{mismatched.length} channel package artifacts are mismatched" unless mismatched.empty?
  errors << "#{extras.length} unexpected package payloads were downloaded" unless extras.empty?

  {
    id: minion,
    status: errors.empty? ? 'passed' : 'failed',
    expected_package_count: inputs[:packages].length,
    expected_payload_count: expected.length,
    verified_package_count: verified_package_count,
    verified_payload_count: verified_payloads.length,
    verified_payload_bytes: verified_payloads.sum { |payload| payload['size'] },
    downloaded_payload_count: payloads.length,
    downloaded_payload_bytes: payloads.sum { |payload| payload['size'] },
    extra_payload_count: extras.length,
    extra_payload_bytes: extras.sum { |payload| payload['size'] },
    missing_records: missing,
    mismatched_records: mismatched,
    extra_payloads: extras,
    errors: errors
  }
end

# Query and verify the structured downloaded-package inventory after timing.
def package_download_benchmark_verify(inputs, pod)
  inventory_timeout = [inputs[:timeout_seconds], PACKAGE_DOWNLOAD_BENCHMARK_IDLE_TIMEOUT].min
  command_timeout = [inventory_timeout - 15, 1].max
  returns =
    package_download_benchmark_salt_call(
      inputs,
      pod,
      'cmd.run_all',
      [
        package_download_benchmark_inventory_command,
        'python_shell=False',
        'output_loglevel=quiet',
        "timeout=#{command_timeout}"
      ],
      'Package cache verification',
      timeout_seconds: inventory_timeout
    )
  per_minion =
    inputs[:minions].map do |minion|
      package_download_benchmark_verify_minion(minion, returns[minion], inputs)
    end
  errors = []
  errors << 'one or more minions failed package cache verification' if per_minion.any? { |minion| minion[:status] == 'failed' }
  expected_payloads_per_minion = inputs[:packages].map { |package| package[:checksum].downcase }.uniq.length

  {
    status: errors.empty? ? 'passed' : 'failed',
    expected_package_count: inputs[:minions].length * inputs[:packages].length,
    expected_payload_count: inputs[:minions].length * expected_payloads_per_minion,
    verified_package_count: per_minion.sum { |minion| minion[:verified_package_count] },
    verified_payload_count: per_minion.sum { |minion| minion[:verified_payload_count] },
    verified_payload_bytes: per_minion.sum { |minion| minion[:verified_payload_bytes] },
    downloaded_payload_count: per_minion.sum { |minion| minion[:downloaded_payload_count] },
    downloaded_payload_bytes: per_minion.sum { |minion| minion[:downloaded_payload_bytes] },
    extra_payload_count: per_minion.sum { |minion| minion[:extra_payload_count] },
    extra_payload_bytes: per_minion.sum { |minion| minion[:extra_payload_bytes] },
    returned_target_count: returns.length,
    successful_target_count: per_minion.count { |minion| minion[:status] == 'passed' },
    errors: errors,
    per_minion: per_minion
  }
rescue StandardError => e
  package_download_benchmark_failed_verification(
    inputs,
    "cache verification raised #{e.class}: #{package_download_benchmark_excerpt(e.message)}"
  )
end

# Confirm that channel membership did not change during the measured workload.
def package_download_benchmark_verify_snapshot(inputs)
  api_packages = package_download_benchmark_api_call('channel.software.listAllPackages', channelLabel: inputs[:channel])
  packages = package_download_benchmark_api_packages(api_packages)
  digest = package_download_benchmark_snapshot_digest(packages)
  errors = []
  errors << "package count changed from #{inputs[:packages].length} to #{packages.length}" unless packages.length == inputs[:packages].length
  errors << "snapshot digest changed from #{inputs[:snapshot_digest]} to #{digest}" unless digest == inputs[:snapshot_digest]

  {
    status: errors.empty? ? 'passed' : 'failed',
    checked_at: Time.now.utc.iso8601(6),
    package_count: packages.length,
    sha256: digest,
    errors: errors
  }
rescue StandardError => e
  {
    status: 'failed',
    checked_at: Time.now.utc.iso8601(6),
    package_count: nil,
    sha256: nil,
    errors: ["channel snapshot verification raised #{e.class}: #{package_download_benchmark_excerpt(e.message)}"]
  }
end

# Verify both downloaded payloads and the frozen channel snapshot.
def package_download_benchmark_complete_verification(inputs, pod)
  verification = package_download_benchmark_verify(inputs, pod)
  snapshot = package_download_benchmark_verify_snapshot(inputs)
  verification[:channel_snapshot] = snapshot
  verification[:errors].concat(snapshot[:errors].map { |error| "channel snapshot: #{error}" })
  verification[:status] = 'failed' unless verification[:errors].empty?
  verification
end

# Return the immutable channel package fields recorded with the result.
def package_download_benchmark_report_package(package)
  {
    id: package[:id],
    name: package[:name],
    arch: package[:arch],
    epoch: package[:epoch],
    version: package[:version],
    release: package[:release],
    evr: package[:evr],
    checksum: package[:checksum],
    checksum_type: package[:checksum_type],
    retracted: package[:retracted],
    tuple: package[:tuple]
  }
end

# Build schema version 2 result data from the measured workload and verification.
def package_download_benchmark_result(inputs, pod, workload, verification)
  errors = workload[:errors].map { |error| "workload: #{error}" }
  errors.concat(verification[:errors].map { |error| "verification: #{error}" })

  {
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
      packages: inputs[:packages].map { |package| package_download_benchmark_report_package(package) }
    },
    execution: workload,
    verification: verification,
    errors: errors
  }
end

# Build a complete failure-shaped verification result.
def package_download_benchmark_failed_verification(inputs, error)
  expected_payloads_per_minion = inputs[:packages].map { |package| package[:checksum].downcase }.uniq.length
  {
    status: 'failed',
    expected_package_count: inputs[:minions].length * inputs[:packages].length,
    expected_payload_count: inputs[:minions].length * expected_payloads_per_minion,
    verified_package_count: 0,
    verified_payload_count: 0,
    verified_payload_bytes: 0,
    downloaded_payload_count: 0,
    downloaded_payload_bytes: 0,
    extra_payload_count: 0,
    extra_payload_bytes: 0,
    returned_target_count: 0,
    successful_target_count: 0,
    errors: [error],
    per_minion: [],
    channel_snapshot: nil
  }
end

# Run one concurrent all-package download and verify every cached record.
def package_download_benchmark_execute(inputs, pod)
  workload = package_download_benchmark_workload(inputs, pod)
  verification =
    begin
      package_download_benchmark_wait_for_idle(inputs, pod)
      package_download_benchmark_complete_verification(inputs, pod)
    rescue StandardError => e
      message = package_download_benchmark_excerpt(e.message)
      package_download_benchmark_failed_verification(inputs, "verification raised #{e.class}: #{message}")
    end

  package_download_benchmark_result(inputs, pod, workload, verification)
end

# Write one result document to the testsuite results directory.
def package_download_benchmark_write_result(result)
  timestamp = Time.parse(result[:started_at]).strftime('%Y%m%dT%H%M%S.%6NZ')
  directory = File.expand_path("../../results/package-download/#{timestamp}-#{Process.pid}", __dir__)
  FileUtils.mkdir_p(directory)
  path = File.join(directory, 'result.json')
  File.write(path, "#{JSON.pretty_generate(result)}\n")
  path
end

Given('the Salt package download benchmark inputs are valid') do
  @package_download_benchmark_inputs = package_download_benchmark_inputs
end

Given('the initial configured channel package snapshot is valid') do
  @package_download_benchmark_inputs = package_download_benchmark_snapshot(@package_download_benchmark_inputs)
end

Given('a ready server pod is reachable from the benchmark controller') do
  @package_download_benchmark_pod = package_download_benchmark_server_pod
end

Given('the benchmark minions are ready for the configured channel') do
  @package_download_benchmark_inputs =
    package_download_benchmark_preflight(@package_download_benchmark_inputs, @package_download_benchmark_pod)
end

When('I clear RPM payload caches on the benchmark minions outside the measurement') do
  package_download_benchmark_clear_cache(@package_download_benchmark_inputs, @package_download_benchmark_pod)
end

When('I execute and record the channel package downloads') do
  @package_download_benchmark_result = package_download_benchmark_execute(
    @package_download_benchmark_inputs,
    @package_download_benchmark_pod
  )
  @package_download_benchmark_result_path = package_download_benchmark_write_result(@package_download_benchmark_result)
  log "Package download result: #{@package_download_benchmark_result_path}"
end

Then('the package download result report should exist') do
  raise 'Package download result report was not written' unless File.file?(@package_download_benchmark_result_path)
end

Then('every configured minion should have downloaded every channel package') do
  result = @package_download_benchmark_result
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
