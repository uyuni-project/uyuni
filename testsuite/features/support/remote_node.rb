# Copyright (c) 2024 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'timeout'
require_relative 'network_utils'

# The RemoteNode class represents a remote node.
# It is used to interact with the remote node through SSH.
class RemoteNode
  attr_accessor :host, :hostname, :port, :user, :password, :target, :full_hostname, :private_ip, :public_ip, :private_interface, :public_interface, :os_family, :os_version, :has_mgrctl

  # Initializes a new remote node.
  #
  # @param host [String] The hostname of the remote node.
  # @param port [Integer] The port to use for the SSH connection.
  # @param user [String] The user to use for the SSH connection.
  # @param password [String] The password to use for the SSH connection
  # @return [RemoteNode] The remote node.
  def initialize(host, port: 22, user: 'root', password: nil)
    @host = host
    @port = port
    @user = user
    @password = password
    puts "Initializing a remote node for '#{@host}'."
    raise(NotImplementedError, "Host #{@host} is not defined as a valid host in the Test Framework.") unless ENV_VAR_BY_HOST.key? @host

    unless ENV.key? ENV_VAR_BY_HOST[@host]
      warn "Host #{@host} is not defined as environment variable."
      return
    end

    @target = ENV.fetch(ENV_VAR_BY_HOST[@host], nil).to_s.strip
    out, _err, _code = ssh('hostname', host: @target)
    @hostname = out.strip
    raise LoadError, "We can't connect to #{@host} through SSH." if @hostname.empty?

    $named_nodes[host] = @hostname
    if @host == 'server'
      _out, _err, code = ssh('which mgrctl', host: @target)
      @has_mgrctl = code.zero?
      out, _code = run('sed -n \'s/^java.hostname *= *\(.\+\)$/\1/p\' /etc/rhn/rhn.conf')
    else
      out, _err, _code = ssh('hostname -f', host: @target)
    end
    @full_hostname = out.strip
    raise StandardError, "No FQDN for '#{@hostname}'. Response code: #{code}" if @full_hostname.empty?

    # Remove /etc/motd, or any output from run will contain the content of /etc/motd
    run('rm -f /etc/motd && touch /etc/motd')
    $stdout.puts "Host '#{@host}' is alive with determined hostname #{@hostname} and FQDN #{@full_hostname}" unless $build_validation
    @os_version, @os_family = get_os_version

    if (PRIVATE_ADDRESSES.key? host) && !$private_net.nil?
      @private_ip = net_prefix + PRIVATE_ADDRESSES[host]
      @private_interface = 'eth1'
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
    ssh_command(command, host, port: @port, user: @user, password: @password)
  end

  # Copies a file from the local machine to the remote node.
  #
  # @param local_path [String] The path to the file to copy.
  # @param remote_path [String] The path in the destination.
  # @param host [String] The hostname of the remote node.
  def scp(local_path, remote_path, host: @full_hostname)
    scp_command(local_path, remote_path, host, port: @port, user: @user, password: @password)
  end

  # Runs a command and returns the output, error, and exit code.
  #
  # @param cmd [String] The command to run.
  # @param runs_in_container [Boolean] Whether the command should be run in the container or on the host. Defaults to true.
  # @param separated_results [Boolean] Whether the results should be stored separately. Defaults to false.
  # @param check_errors [Boolean] Whether to check for errors or not. Defaults to true.
  # @param timeout [Integer] The timeout to be used, in seconds.
  # @param user [String] The user to be used to run the command.
  # @param successcodes [Array<Integer>] An array with the values to be accepted as success codes from the command run.
  # @param buffer_size [Integer] The maximum buffer size in bytes.
  # @param verbose [Boolean] Whether to log the output of the command in case of success.
  # @return [Array<String, String, Integer>] The output, error, and exit code.
  def run(cmd, runs_in_container: true, separated_results: false, check_errors: true, timeout: DEFAULT_TIMEOUT, user: 'root', successcodes: [0], buffer_size: 65_536, verbose: false)
    cmd_prefixed = @has_mgrctl && runs_in_container ? "mgrctl exec -i '#{cmd.gsub('\'', '\'"\'"\'')}'" : cmd
    run_local(cmd_prefixed, separated_results: separated_results, check_errors: check_errors, timeout: timeout, user: user, successcodes: successcodes, buffer_size: buffer_size, verbose: verbose)
  end

  # Runs a command locally and returns the output, error, and exit code.
  #
  # @param cmd [String] The command to run.
  # @param separated_results [Boolean] Whether the results should be stored separately.
  # @param check_errors [Boolean] Whether to check for errors or not.
  # @param timeout [Integer] The timeout to be used, in seconds.
  # @param user [String] The user to be used to run the command.
  # @param successcodes [Array<Integer>] An array with the values to be accepted as success codes from the command run.
  # @param buffer_size [Integer] The maximum buffer size in bytes.
  # @param verbose [Boolean] Whether to log the output of the command in case of success.
  # @return [Array<String, Integer>] The output, error, and exit code.
  def run_local(cmd, separated_results: false, check_errors: true, timeout: DEFAULT_TIMEOUT, user: 'root', successcodes: [0], buffer_size: 65_536, verbose: false)
    out, err, code = ssh_command(cmd, @target, user: user, timeout: timeout, buffer_size: buffer_size)
    out_nocolor = out.gsub(/\e\[([;\d]+)?m/, '')
    raise ScriptError, "FAIL: #{cmd} returned status code = #{code}.\nOutput:\n#{out_nocolor}" if check_errors && !successcodes.include?(code)

    $stdout.puts "#{cmd} returned status code = #{code}.\nOutput:\n'#{out_nocolor}'" if verbose
    if separated_results
      [out, err, code]
    else
      [out + err, code]
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

  # Copies a local file to a remote node.
  #
  # @param local_file [String] The path to the file to copy.
  # @param remote_file [String] The path in the destination.
  # @return [Integer] The exit code.
  def inject(local_file, remote_file)
    if @has_mgrctl
      tmp_folder, _code = run_local('mktemp -d')
      tmp_file = File.join(tmp_folder.strip, File.basename(local_file))
      code, _remote = scp(local_file, tmp_file)
      if code.zero?
        _out, code = run_local("mgrctl cp --user #{user} #{tmp_file} server:#{remote_file}")
        raise ScriptError, "Failed to copy #{tmp_file} to container" unless code.zero?
      end
      run_local("rm -r #{tmp_folder}")
    else
      code = scp(local_file, remote_file)
    end
    code
  end

  # Copies a remote file to a local one.
  #
  # @param remote_file [String] The path in the destination.
  # @param local_file [String] The path to the file to copy.
  # @return [Integer] The exit code.
  def extract(remote_file, local_file)
    if @has_mgrctl
      tmp_folder, _code = run_local('mktemp -d')
      tmp_file = File.join(tmp_folder.strip, File.basename(remote_file))
      _out, code = run_local("mgrctl cp --user #{user} server:#{remote_file} #{tmp_file}")
      raise ScriptError, "Failed to extract #{remote_file} from container" unless code.zero?

      code = scp(tmp_file, local_file)
      raise ScriptError, "Failed to extract #{tmp_file} from host" unless code.zero?

      run_local("rm -r #{tmp_folder}")
    else
      code = scp(remote_file, local_file)
    end
    code
  end

  # Check if a file exists on a node.
  # Handles checking in server container if possible.
  #
  # @param file [String] The path of the file to check.
  # @return [Boolean] Returns true if the file exists, false otherwise.
  def file_exists(file)
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
  def folder_exists(file)
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
    result = run_local('echo test', timeout: 0, check_errors: false).first
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
    %w[br0 eth0 eth1 ens0 ens1 ens2 ens3 ens4 ens5 ens6].each do |dev|
      output, code = run_local("ip address show dev #{dev} | grep 'inet '", check_errors: false)
      next unless code.zero?

      @public_interface = dev
      return '' if output.empty?

      return output.split[1].split('/').first
    end
    raise ArgumentError, "Cannot resolve public ip of #{host}"
  end

  # Extract the OS version and OS family
  # We get these data decoding the values in '/etc/os-release'
  def get_os_version
    os_family_raw, code = run('grep "^ID=" /etc/os-release', check_errors: false)
    return nil, nil unless code.zero?

    os_family = os_family_raw.strip.split('=')[1]
    return nil, nil if os_family.nil?

    os_family.delete! '"'
    os_version_raw, code = run('grep "^VERSION_ID=" /etc/os-release', check_errors: false)
    return nil, nil unless code.zero?

    os_version = os_version_raw.strip.split('=')[1]
    return nil, nil if os_version.nil?

    os_version.delete! '"'
    # on SLES, we need to replace the dot with '-SP'
    os_version.gsub!('.', '-SP') if os_family =~ /^sles/
    $stdout.puts "Node: #{@hostname}, OS Version: #{os_version}, Family: #{os_family}"
    [os_version, os_family]
  end
end
