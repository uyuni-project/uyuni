# Copyright (c) 2016-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'twopence'
require 'timeout'

# Extend the objects node VMs with useful methods needed for testsuite.
# All function added here will be available like get_target('server').run
#  or get_target('sle_minion').run_until_ok etc.
module LavandaBasic
  # Initializes the hostnames, only one time.
  #
  # @param hostname [String] The hostname to initialize.
  def init_hostname(hostname)
    @in_hostname = hostname.strip
  end

  # Initializes the fully qualified domain name (FQDN) instance variable.
  #
  # @param fqdn [String] The fully qualified domain name of the host.
  def init_full_hostname(fqdn)
    @in_full_hostname = fqdn.strip
  end

  # Initializes the private IP instance variable.
  #
  # @param private_ip [String] The private IP address of the instance.
  def init_private_ip(private_ip)
    @in_private_ip = private_ip
  end

  # Initializes the public IP instance variable.
  #
  # @param public_ip [String] The public IP address of the server.
  def init_public_ip(public_ip)
    @in_public_ip = public_ip
  end

  # Initializes the private interface instance variable.
  #
  # @param private_interface [Boolean] A boolean value that indicates whether the interface is private or not.
  def init_private_interface(private_interface)
    @in_private_interface = private_interface
  end

  # Initializes the public interface instance variable.
  #
  # @param public_interface [String] The name of the public interface.
  def init_public_interface(public_interface)
    @in_public_interface = public_interface
  end

  # Initializes the OS family instance variable.
  #
  # @param os_family [String] The OS family to initialize.
  def init_os_family(os_family)
    @in_os_family = os_family
  end

  # Initializes the OS version instance variable.
  #
  # @param os_version [String] The version of the operating system.
  def init_os_version(os_version)
    @in_os_version = os_version
  end

  # Initializes the `@in_has_mgrctl` variable to true.
  def init_has_mgrctl
    @in_has_mgrctl = true
  end

  # Getter functions, executed on testsuite

  # Returns the hostname.
  #
  # @return [String] The hostname.
  # @raise [KeyError] If the hostname is empty.
  def hostname
    raise KeyError, 'empty hostname, something wrong' if @in_hostname.nil? || @in_hostname.empty?

    @in_hostname
  end

  # Returns the fully qualified domain name (FQDN).
  #
  # @return [String] The fully qualified domain name (FQDN).
  # @raise [KeyError] If the FQDN is empty.
  def full_hostname
    raise KeyError, 'empty hostname, something wrong' if @in_full_hostname.nil? || @in_full_hostname.empty?

    @in_full_hostname
  end

  # Returns the private IP address.
  #
  # @return [String] The private IP address.
  # @raise [KeyError] If the private IP address is empty.
  def private_ip
    raise KeyError, 'empty private_ip, something wrong' if @in_private_ip.nil? || @in_private_ip.empty?

    @in_private_ip
  end

  # Returns the public IP address.
  #
  # @return [String] The public IP address.
  # @raise [KeyError] If the public IP address is empty.
  def public_ip
    raise KeyError, 'empty public_ip, something wrong' if @in_public_ip.nil? || @in_public_ip.empty?

    @in_public_ip
  end

  # Returns the private interface.
  #
  # @return [String] The private interface.
  # @raise [KeyError] If the private interface is empty.
  def private_interface
    raise KeyError, 'empty private_interface, something wrong' if @in_private_interface.nil? || @in_private_interface.empty?

    @in_private_interface
  end

  # Returns the public interface.
  #
  # @return [String] The public interface.
  # @raise [KeyError] If the public interface is empty.
  def public_interface
    raise KeyError, 'empty public_interface, something wrong' if @in_public_interface.nil? || @in_public_interface.empty?

    @in_public_interface
  end

  # Returns the OS family.
  #
  # @return [String] The OS family.
  # @raise [KeyError] If the OS family is empty.
  def os_family
    raise KeyError, 'empty os_family, something wrong' if @in_os_family.nil? || @in_os_family.empty?

    @in_os_family
  end

  # Returns the OS version.
  #
  # @return [String] The OS version.
  # @raise [KeyError] If the OS version is empty.
  def os_version
    raise KeyError, 'empty os_version, something wrong' if @in_os_version.nil? || @in_os_version.empty?

    @in_os_version
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
    cmd_prefixed = @in_has_mgrctl && runs_in_container ? "mgrctl exec -i '#{cmd.gsub(/'/, '\'"\'"\'')}'" : cmd
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
    if separated_results
      out, err, _lo, _rem, code = test_and_store_results_separately(cmd, user, timeout, buffer_size)
    else
      out, _lo, _rem, code = test_and_store_results_together(cmd, user, timeout, buffer_size)
    end
    out_nocolor = out.gsub(/\e\[([;\d]+)?m/, '')
    raise ScriptError, "FAIL: #{cmd} returned status code = #{code}.\nOutput:\n#{out_nocolor}" if check_errors && !successcodes.include?(code)

    $stdout.puts "#{cmd} returned status code = #{code}.\nOutput:\n'#{out_nocolor}'" if verbose
    if separated_results
      [out, err, code]
    else
      [out, code]
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
  # @param user [String] The owner of the file.
  # @param dots [Boolean] Whether to display progress dots.
  # @return [Integer] The exit code.
  def inject(local_file, remote_file, user = 'root', dots = true)
    if @in_has_mgrctl
      tmp_folder, _code = run_local('mktemp -d')
      tmp_file = File.join(tmp_folder.strip, File.basename(local_file))
      code, _remote = inject_file(local_file, tmp_file, user, dots)
      if code.zero?
        _out, code = run_local("mgrctl cp --user #{user} #{tmp_file} server:#{remote_file}")
        raise ScriptError, "Failed to copy #{tmp_file} to container" unless code.zero?
      end
      run_local("rm -r #{tmp_folder}")
    else
      code, _remote = inject_file(local_file, remote_file, user, dots)
    end
    code
  end

  # Copies a remote file to a local one.
  #
  # @param remote_file [String] The path in the destination.
  # @param local_file [String] The path to the file to copy.
  # @param user [String] The owner of the file.
  # @param dots [Boolean] Whether to display progress dots.
  # @return [Integer] The exit code.
  def extract(remote_file, local_file, user = 'root', dots = true)
    if @in_has_mgrctl
      tmp_folder, _code = run_local('mktemp -d')
      tmp_file = File.join(tmp_folder.strip, File.basename(remote_file))
      _out, code = run_local("mgrctl cp --user #{user} server:#{remote_file} #{tmp_file}")
      raise ScriptError, "Failed to extract #{remote_file} from container" unless code.zero?

      code, _remote = extract_file(tmp_file, local_file, user, dots)
      raise ScriptError, "Failed to extract #{tmp_file} from host" unless code.zero?

      run_local("rm -r #{tmp_folder}")
    else
      code, _local = extract_file(remote_file, local_file, user, dots)
    end
    code
  end

  # Check if a file exists on a node.
  # Handles checking in server container if possible.
  #
  # @param file [String] The path of the file to check.
  # @return [Boolean] Returns true if the file exists, false otherwise.
  def file_exists(file)
    if @in_has_mgrctl
      _out, code = run_local("mgrctl exec -- 'test -f #{file}'", check_errors: false)
      exists = code.zero?
    else
      _out, local, _remote, code = test_and_store_results_together("test -f #{file}", 'root', 500)
      exists = code.zero? && local.zero?
    end
    exists
  end

  # Check if a folder exists on a node.
  # Handles checking in server container if possible.
  #
  # @param file [String] The path of the folder to check.
  # @return [Boolean] Returns true if the folder exists, false otherwise.
  def folder_exists(file)
    if @in_has_mgrctl
      _out, code = run_local("mgrctl exec -- 'test -d #{file}'", check_errors: false)
      exists = code.zero?
    else
      _out, local, _remote, code = test_and_store_results_together("test -d #{file}", 'root', 500)
      exists = code.zero? && local.zero?
    end
    exists
  end

  # Delete a file on a node.
  # Handles checking in server container if possible.
  #
  # @param file [String] The path of the file to be deleted.
  # @return [Integer] The exit code of the file deletion operation.
  def file_delete(file)
    if @in_has_mgrctl
      _out, code = run_local("mgrctl exec -- 'rm #{file}'", check_errors: false)
    else
      _out, _local, _remote, code = test_and_store_results_together("rm #{file}", 'root', 500)
    end
    code
  end

  # Delete a folder on a node.
  # Handles checking in server container if possible.
  #
  # @param folder [String] The path of the folder to be deleted.
  # @return [Integer] The exit code of the operation.
  def folder_delete(folder)
    if @in_has_mgrctl
      _out, code = run_local("mgrctl exec -- 'rm -rf #{folder}'", check_errors: false)
    else
      _out, _local, _remote, code = test_and_store_results_together("rm -rf #{folder}", 'root', 500)
    end
    code
  end

  # Checks if the node is offline.
  #
  # @return [Boolean] true if the node is offline, false otherwise.
  def node_offline?
    run_local('echo test', timeout: 0, check_errors: false).first.empty?
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
end
