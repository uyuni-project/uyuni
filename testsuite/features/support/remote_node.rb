# Copyright (c) 2024-2025 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'timeout'
require_relative 'network_utils'

# The RemoteNode class represents a remote node.
# It is used to interact with the remote node through SSH.
class RemoteNode
  attr_accessor :host, :hostname, :port, :target, :full_hostname, :private_ip, :public_ip, :private_interface, :public_interface, :os_family, :os_version, :local_os_family, :local_os_version, :has_mgrctl

  # Initializes a new remote node.
  #
  # @param host [String] The hostname of the remote node.
  # @param port [Integer] The port to use for the SSH connection.
  # @return [RemoteNode] The remote node.
  def initialize(host, port: 22)
    @host = host
    @port = port
    puts "Initializing a remote node for '#{@host}'."
    raise(NotImplementedError, "Host #{@host} is not defined as a valid host in the Test Framework.") unless ENV_VAR_BY_HOST.key? @host

    unless ENV.key? ENV_VAR_BY_HOST[@host]
      warn "Host #{@host} is not defined as environment variable."
      return
    end

    @target = ENV.fetch(ENV_VAR_BY_HOST[@host], nil).to_s.strip
    # Remove /etc/motd, or any output from run will contain the content of /etc/motd
    ssh('rm -f /etc/motd && touch /etc/motd', host: @target) unless @host == 'localhost'
    out, _err, _code = ssh('echo $HOSTNAME', host: @target)
    @hostname = out.strip
    raise LoadError, "We can't connect to #{@host} through SSH." if @hostname.empty?

    $named_nodes[host] = @hostname
    if @host == 'server'
      _out, _err, code = ssh('which mgrctl', host: @target)
      @has_mgrctl = code.zero?
      # Remove /etc/motd inside the container, or any output from run will contain the content of /etc/motd
      run('rm -f /etc/motd && touch /etc/motd')
      out, _code = run('sed -n \'s/^java.hostname *= *\(.\+\)$/\1/p\' /etc/rhn/rhn.conf')
    else
      out, _err, _code = ssh('hostname -f', host: @target)
    end
    @full_hostname = out.strip
    raise StandardError, "No FQDN for '#{@hostname}'. Response code: #{code}" if @full_hostname.empty?

    $stdout.puts "Host '#{@host}' is alive with determined hostname #{@hostname} and FQDN #{@full_hostname}" unless $build_validation

    # Determine OS version and OS family both inside the container and on the local host
    # in the case of non-containerized systems, both fields will be identical:
    @os_version, @os_family = get_os_version
    @local_os_version, @local_os_family = get_os_version(runs_in_container: false)

    if (PRIVATE_ADDRESSES.key? host) && !$private_net.nil?
      @private_ip = net_prefix + PRIVATE_ADDRESSES[host]
      @private_interface = nil
      %w[eth1 ens4].each do |dev|
        _output, code = run_local("ip address show dev #{dev}", check_errors: false)

        if code.zero?
          @private_interface = dev
          break
        end
      end
      raise StandardError, "No private interface for '#{@hostname}'." if @private_interface.nil?
    end

    ip = client_public_ip
    @public_ip = ip unless ip.empty?

    $node_by_host[@host] = self
    $host_by_node[self] = @host
  end

  # Runs a command on the remote node.
  #
  # @param command [String] The command to run.
  # @param host [String] The hostname of the remote node.
  # @return [Array<String, String, Integer>] The exit code and the output.
  def ssh(command, host: @full_hostname)
    ssh_command(command, host, port: @port)
  end

  # Copies a file from the local machine to the remote node.
  #
  # @param local_path [String] The path to the file to be uploaded.
  # @param remote_path [String] The path in the destination.
  # @param host [String] The hostname of the remote node.
  def scp_upload(local_path, remote_path, host: @full_hostname)
    scp_upload_command(local_path, remote_path, host, port: @port)
  end

  # Copies a file from the local machine to the remote node.
  #
  # @param remote_path [String] The path of the file to be downloaded.
  # @param local_path [String] The path to the destination file.
  # @param host [String] The hostname of the remote node.
  def scp_download(remote_path, local_path, host: @full_hostname)
    scp_download_command(remote_path, local_path, host, port: @port)
  end

  # Runs a command and returns the output, error, and exit code.
  #
  # @param cmd [String] The command to run.
  # @param runs_in_container [Boolean] Whether the command should be run in the container or on the host. Defaults to true.
  # @param separated_results [Boolean] Whether the results should be stored separately. Defaults to false.
  # @param check_errors [Boolean] Whether to check for errors or not. Defaults to true.
  # @param timeout [Integer] The timeout to be used, in seconds.
  # @param successcodes [Array<Integer>] An array with the values to be accepted as success codes from the command run.
  # @param buffer_size [Integer] The maximum buffer size in bytes.
  # @param verbose [Boolean] Whether to log the output of the command in case of success.
  # @param exec_option [Boolean] The container exec option.
  # @return [Array<String, String, Integer>] The output, error, and exit code.
  def run(cmd, runs_in_container: true, separated_results: false, check_errors: true, timeout: DEFAULT_TIMEOUT, successcodes: [0], buffer_size: 65_536, verbose: false, exec_option: '-i')
    cmd_prefixed = @has_mgrctl && runs_in_container ? "mgrctl exec #{exec_option} '#{cmd.gsub('\'', '\'"\'"\'')}'" : cmd
    run_local(cmd_prefixed, separated_results: separated_results, check_errors: check_errors, timeout: timeout, successcodes: successcodes, buffer_size: buffer_size, verbose: verbose)
  end

  # Runs a command that contains commands chained by pipes in it and returns the output, error, and exit code of all the commands chained. Just for debugging purpouses.
  #
  # @param cmd [String] The command to run.
  # @param expected_pipestatus_codes [Array<Integer>] Expected stderrs of the commands chained by pipes.
  # @param runs_in_container [Boolean] Whether the command should be run in the container or on the host. Defaults to true.
  # @param separated_results [Boolean] Whether the results should be stored separately. Defaults to false.
  # @param check_errors [Boolean] Whether to check for errors or not. Defaults to true.
  # @param timeout [Integer] The timeout to be used, in seconds.
  # @param successcodes [Array<Integer>] An array with the values to be accepted as success codes from the command run.
  # @param buffer_size [Integer] The maximum buffer size in bytes.
  # @param verbose [Boolean] Whether to log the output of the command in case of success.
  # @param exec_option [Boolean] The container exec option.
  # # @return [Array<String, Integer>] The output, error, and exit code.
  def run_pipe(cmd, expected_pipestatus_codes, runs_in_container: true, separated_results: false, check_errors: true, timeout: DEFAULT_TIMEOUT, successcodes: [0], buffer_size: 65_536, verbose: false, exec_option: '-i')
    pipestatus_file_path = '/tmp/temp_file_with_stderrs'
    cmd += "; echo ${PIPESTATUS[*]} > #{pipestatus_file_path}"
    cmd_read_codes = "cat #{pipestatus_file_path}; rm #{pipestatus_file_path}"
    if @has_mgrctl && runs_in_container
      cmd = "mgrctl exec #{exec_option} '#{cmd.gsub('\'', '\'"\'"\'')}'"
      cmd_read_codes = "mgrctl exec #{exec_option} '#{cmd_read_codes.gsub('\'', '\'"\'"\'')}'"
    end

    out, initial_code = run_local(cmd, separated_results: separated_results, check_errors: check_errors, timeout: timeout, successcodes: successcodes, buffer_size: buffer_size, verbose: verbose)
    stderr_of_commands, _code = run_local(cmd_read_codes)

    stderr_of_commands_array = stderr_of_commands.split.map(&:to_i)
    raise "Expected the number of expected pipestatus codes does not match the number of commands chained by pipes. Expected stderr:#{expected_pipestatus_codes}, current stderr:#{stderr_of_commands_array}" if expected_pipestatus_codes.length != stderr_of_commands_array.length
    raise "Expected outcome does not match with current outcome. Expected stderr:#{expected_pipestatus_codes}, current stderr:#{stderr_of_commands_array}" if check_errors && stderr_of_commands_array.each_index.any? { |i| stderr_of_commands_array[i] != expected_pipestatus_codes[i] }

    if separated_results
      [out, stderr_of_commands, initial_code]
    else
      [out + stderr_of_commands, initial_code]
    end
  end

  # Runs a command locally and returns the output, error, and exit code.
  #
  # @param cmd [String] The command to run.
  # @param separated_results [Boolean] Whether the results should be stored separately.
  # @param check_errors [Boolean] Whether to check for errors or not.
  # @param timeout [Integer] The timeout to be used, in seconds.
  # @param successcodes [Array<Integer>] An array with the values to be accepted as success codes from the command run.
  # @param buffer_size [Integer] The maximum buffer size in bytes.
  # @param verbose [Boolean] Whether to log the output of the command in case of success.
  # @return [Array<String, Integer>] The output, error, and exit code.
  def run_local(cmd, separated_results: false, check_errors: true, timeout: DEFAULT_TIMEOUT, successcodes: [0], buffer_size: 65_536, verbose: false)
    out, err, code = ssh_command(cmd, @target, timeout: timeout, buffer_size: buffer_size)
    out_nocolor = out.gsub(/\e\[([;\d]+)?m/, '')
    raise ScriptError, "FAIL: #{cmd} returned status code = #{code}.\nOutput:\n#{out_nocolor}" if check_errors && !successcodes.include?(code)

    $stdout.puts "#{cmd} returned status code = #{code}.\nOutput:\n'#{out_nocolor}'" if verbose
    if separated_results
      [out, err, code]
    else
      [out + err, code]
    end
  end

  # Runs a local command until it succeeds or times out.
  #
  # @param cmd [String] The command to run.
  # @param timeout [Integer] The timeout to be used, in seconds.
  # @param runs_in_container [Boolean] Whether the command should be run in the container or on the host.
  # @return [Array<String, Integer>] The result and exit code.
  def run_local_until_ok(cmd, timeout: DEFAULT_TIMEOUT, runs_in_container: true)
    repeat_until_timeout(timeout: timeout, report_result: true) do
      result, code = run_local(cmd, check_errors: false, runs_in_container: runs_in_container)
      return [result, code] if code.zero?

      sleep 2
      result
    end
  end

  # Runs a command until it succeeds or times out.
  #
  # @param cmd [String] The command to run.
  # @param timeout [Integer] The timeout to be used, in seconds.
  # @param runs_in_container [Boolean] Whether the command should be run in the container or on the host.
  # @return [Array<String, Integer>] The result and exit code.
  def run_until_ok(cmd, timeout: DEFAULT_TIMEOUT, runs_in_container: true)
    repeat_until_timeout(timeout: timeout, report_result: true) do
      result, code = run(cmd, check_errors: false, runs_in_container: runs_in_container)
      return [result, code] if code.zero?

      sleep 2
      result
    end
  end

  # Runs a command until it fails or times out.
  #
  # @param cmd [String] The command to run.
  # @param timeout [Integer] The timeout to be used, in seconds.
  # @param runs_in_container [Boolean] Whether the command should be run in the container or on the host.
  # @return [Array<String, Integer>] The result and exit code.
  def run_until_fail(cmd, timeout: DEFAULT_TIMEOUT, runs_in_container: true)
    repeat_until_timeout(timeout: timeout, report_result: true) do
      result, code = run(cmd, check_errors: false, runs_in_container: runs_in_container)
      return [result, code] if code.nonzero?

      sleep 2
      result
    end
  end

  # Waits until the process is no longer running.
  #
  # @param process [String] The name of the process to wait for.
  # @return [Array<String, Integer>] The result and exit code.
  def wait_while_process_running(process)
    repeat_until_timeout(report_result: true) do
      result, code = run("pgrep -x #{process} >/dev/null", check_errors: false)
      return [result, code] if code.nonzero?

      sleep 2
      result
    end
  end

  # Copies a file from the test runner (aka controller) into the remote node.
  #
  # @param test_runner_file [String] The path to the file to copy.
  # @param remote_node_file [String] The path in the destination.
  # @return [Integer] The exit code.
  def inject(test_runner_file, remote_node_file)
    if @has_mgrctl
      tmp_file = File.join('/tmp/', File.basename(test_runner_file))
      success = get_target('localhost').scp_upload(test_runner_file, tmp_file, host: @full_hostname)
      if success
        _out, code = run_local("mgrctl cp #{tmp_file} server:#{remote_node_file}")
        raise ScriptError, "Failed to copy #{tmp_file} to container" unless code.zero?
      end
    else
      success = get_target('localhost').scp_upload(test_runner_file, remote_node_file, host: @full_hostname)
    end
    success
  end

  # Copies a file from the remote node into the test runner (aka controller).
  #
  # @param remote_node_file [String] The path in the destination.
  # @param test_runner_file [String] The path to the file to copy.
  # @return [Integer] The exit code.
  def extract(remote_node_file, test_runner_file)
    if @has_mgrctl
      tmp_file = File.join('/tmp/', File.basename(remote_node_file))
      _out, code = run_local("mgrctl cp server:#{remote_node_file} #{tmp_file}", verbose: false)
      raise ScriptError, "Failed to extract #{remote_node_file} from container" unless code.zero?

      success = get_target('localhost').scp_download(tmp_file, test_runner_file, host: @full_hostname)
      raise ScriptError, "Failed to extract #{tmp_file} from host" unless success

    else
      success = get_target('localhost').scp_download(remote_node_file, test_runner_file, host: @full_hostname)
    end
    success
  end

  # Check if a file exists on a node.
  # Handles checking in server container if possible.
  #
  # @param file [String] The path of the file to check.
  # @return [Boolean] Returns true if the file exists, false otherwise.
  def file_exists?(file)
    if @has_mgrctl
      _out, code = run_local("mgrctl exec -- 'test -f #{file}'", check_errors: false)
    else
      _out, _err, code = ssh("test -f #{file}")
    end
    code.zero?
  end

  # Check if a folder exists on a node.
  # Handles checking in server container if possible.
  #
  # @param file [String] The path of the folder to check.
  # @return [Boolean] Returns true if the folder exists, false otherwise.
  def folder_exists?(file)
    if @has_mgrctl
      _out, code = run_local("mgrctl exec -- 'test -d #{file}'", check_errors: false)
    else
      _out, _err, code = ssh("test -d #{file}")
    end
    code.zero?
  end

  # Delete a file on a node.
  # Handles checking in server container if possible.
  #
  # @param file [String] The path of the file to be deleted.
  # @return [Integer] The exit code of the file deletion operation.
  def file_delete(file)
    if @has_mgrctl
      _out, code = run_local("mgrctl exec -- 'rm #{file}'", check_errors: false)
    else
      _out, _err, code = ssh("rm #{file}")
    end
    code
  end

  # Delete a folder on a node.
  # Handles checking in server container if possible.
  #
  # @param folder [String] The path of the folder to be deleted.
  # @return [Integer] The exit code of the operation.
  def folder_delete(folder)
    if @has_mgrctl
      _out, code = run_local("mgrctl exec -- 'rm -rf #{folder}'", check_errors: false)
    else
      _out, _err, code = ssh("rm -rf #{folder}")
    end
    code
  end

  # Checks if the node is offline.
  #
  # @return [Boolean] true if the node is offline, false otherwise.
  def node_offline?
    result = run_local('echo test', timeout: 1, check_errors: false).first
    return true if result.nil?

    result.empty?
  end

  # Wait until the node goes offline
  def wait_until_offline
    sleep 1 until node_offline?
    $stdout.puts "Node #{hostname} is offline."
  end

  # Wait until the node comes back online
  #
  # @param timeout [Integer] The maximum time to wait for the node to come online, in seconds.
  def wait_until_online(timeout: DEFAULT_TIMEOUT)
    repeat_until_timeout(timeout: timeout, report_result: true, message: "#{hostname} did not come back online within #{timeout} seconds.") do
      break unless node_offline?

      sleep 1
    end
    $stdout.puts "Node #{hostname} is online."
  end

  private

  # Obtain the Public IP for a node
  def client_public_ip
    if @os_family == 'macOS'
      %w[en0 en1 en2 en3 en4 en5 en6 en7].each do |dev|
        output, code = run_local("ipconfig getifaddr #{dev}", check_errors: false)

        next unless code.zero?

        @public_interface = dev
        return '' if output.empty?

        return output.strip
      end
    else
      %w[br0 eth0 eth1 eth1000 ens0 ens1 ens2 ens3 ens4 ens5 ens6 ens7].each do |dev|
        output, code = run_local("ip address show dev #{dev} | grep 'inet '", check_errors: false)

        next unless code.zero?

        @public_interface = dev
        return '' if output.empty?

        return output.split[1].split('/').first
      end
    end
    raise ArgumentError, "Cannot resolve public ip of #{host}"
  end

  # Extract the OS version and OS family
  # We get these data decoding the values in '/etc/os-release'
  def get_os_version(runs_in_container: true)
    os_family_raw, code = run('grep "^ID=" /etc/os-release', runs_in_container: runs_in_container, check_errors: false)
    os_family_raw, code = run('sw_vers --productName', runs_in_container: runs_in_container, check_errors: false) if code.nonzero?
    return nil, nil unless code.zero?

    os_family = os_family_raw.strip
    os_family = os_family.split('=')[1] unless os_family == 'macOS'
    return nil, nil if os_family.nil?

    os_family.delete! '"'

    if os_family == 'macOS'
      os_version_raw, code = run('sw_vers --productVersion', runs_in_container: runs_in_container, check_errors: false)
      return nil, nil unless code.zero?

      os_version = os_version_raw.strip
    else
      os_version_raw, code = run('grep "^VERSION_ID=" /etc/os-release', runs_in_container: runs_in_container, check_errors: false)
      return nil, nil unless code.zero?

      os_version = os_version_raw.strip.split('=')[1]
      return nil, nil if os_version.nil?
    end

    os_version.delete! '"'
    # on SLES, we need to replace the dot with '-SP'
    os_version.gsub!('.', '-SP') if os_family.match(/^sles/)
    $stdout.puts "Node: #{@hostname}, OS Version: #{os_version}, Family: #{os_family}"
    [os_version, os_family]
  end
end
