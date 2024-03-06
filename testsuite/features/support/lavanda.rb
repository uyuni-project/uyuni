# Copyright (c) 2016-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'twopence'
require 'timeout'

# Extend the objects node VMs with useful methods needed for testsuite.
# All function added here will be available like get_target('server').run
#  or get_target('sle_minion').run_until_ok etc.
module LavandaBasic
  # init the hostnames, only one time
  def init_hostname(hostname)
    @in_hostname = hostname.strip
  end

  ##
  # This function takes a fully qualified domain name (FQDN) as an argument and sets the instance variable
  # @in_full_hostname to the value of the FQDN.
  #
  # Args:
  #   fqdn: The fully qualified domain name of the host.
  def init_full_hostname(fqdn)
    @in_full_hostname = fqdn.strip
  end

  ##
  # It sets the instance variable @in_private_ip to the value of the private_ip parameter.
  #
  # Args:
  #   private_ip: The private IP address of the instance.
  def init_private_ip(private_ip)
    @in_private_ip = private_ip
  end

  ##
  # It initializes the instance variable @in_public_ip to the value of the argument public_ip.
  #
  # Args:
  #   public_ip: The public IP address of the server.
  def init_public_ip(public_ip)
    @in_public_ip = public_ip
  end

  ##
  # It sets the instance variable @in_private_interface to the value of the private_interface parameter.
  #
  # Args:
  #   private_interface: A boolean value that indicates whether the interface is private or not.
  def init_private_interface(private_interface)
    @in_private_interface = private_interface
  end

  ##
  # It sets the value of the instance variable `@in_public_interface` to the value of the parameter `public_interface`.
  #
  # Args:
  #   public_interface: The name of the public interface.
  def init_public_interface(public_interface)
    @in_public_interface = public_interface
  end

  ##
  # It sets the value of the instance variable @in_os_family to the value of the parameter os_family.
  #
  # Args:
  #   os_family: The OS family to initialize.
  def init_os_family(os_family)
    @in_os_family = os_family
  end

  ##
  # It initializes the variable @in_os_version to the value of the parameter os_version.
  #
  # Args:
  #   os_version: The version of the operating system.
  def init_os_version(os_version)
    @in_os_version = os_version
  end

  ##
  # Initializes the @in_has_mgrctl variable to true.
  def init_has_mgrctl
    @in_has_mgrctl = true
  end

  # getter functions, executed on testsuite
  def hostname
    raise KeyError, 'empty hostname, something wrong' if @in_hostname.nil? || @in_hostname.empty?

    @in_hostname
  end

  ##
  # It raises an exception if the hostname is empty, otherwise it returns the hostname.
  def full_hostname
    raise KeyError, 'empty hostname, something wrong' if @in_full_hostname.nil? || @in_full_hostname.empty?

    @in_full_hostname
  end

  ##
  # It raises an exception if the private_ip is empty, otherwise it returns the private_ip.
  def private_ip
    raise KeyError, 'empty private_ip, something wrong' if @in_private_ip.nil? || @in_private_ip.empty?

    @in_private_ip
  end

  ##
  # It returns the public IP address of the machine.
  def public_ip
    raise KeyError, 'empty public_ip, something wrong' if @in_public_ip.nil? || @in_public_ip.empty?

    @in_public_ip
  end

  ##
  # Verifies the private interface instance variable. Raises an error if it's empty.
  def private_interface
    raise KeyError, 'empty private_interface, something wrong' if @in_private_interface.nil? || @in_private_interface.empty?

    @in_private_interface
  end

  ##
  # Verifies the public interface instance variable. Raises an error if it's empty.
  def public_interface
    raise KeyError, 'empty public_interface, something wrong' if @in_public_interface.nil? || @in_public_interface.empty?

    @in_public_interface
  end

  ##
  # Verifies the os_family instance variable. Raises an error if it's empty.
  def os_family
    raise KeyError, 'empty os_family, something wrong' if @in_os_family.nil? || @in_os_family.empty?

    @in_os_family
  end

  ##
  # Verifies the os_version instance variable. Raises an error if it's empty.
  def os_version
    raise KeyError, 'empty os_version, something wrong' if @in_os_version.nil? || @in_os_version.empty?

    @in_os_version
  end

  ##
  # It runs a command, and returns the output, error, and exit code.
  #
  # Args:
  #   cmd: The command to run.
  #   runs_in_container: Whether the command should be run in the the container or in the host. Defaults to true.
  #   separated_results: Whether the results should be stored separately. Defaults to false.
  #   check_errors: Whether to check for errors or not. Defaults to true.
  #   timeout: The timeout to be used, in seconds. Defaults to 250 or the value of the DEFAULT_TIMEOUT environment variable.
  #   user: The user to be used to run the command. Defaults to root.
  #   successcodes: An array with the values to be accepted as success codes from the command run.
  #   buffer_size: The maximum buffer size in bytes. Defaults to 65536.
  #   verbose: Whether to log the output of the command in case of success. Defaults to false.
  def run(cmd, runs_in_container: true, separated_results: false, check_errors: true, timeout: DEFAULT_TIMEOUT, user: 'root', successcodes: [0], buffer_size: 65_536, verbose: false)
    cmd_prefixed = @in_has_mgrctl && runs_in_container ? "mgrctl exec -i '#{cmd.gsub(/'/, '\'"\'"\'')}'" : cmd
    run_local(cmd_prefixed, separated_results: separated_results, check_errors: check_errors, timeout: timeout, user: user, successcodes: successcodes, buffer_size: buffer_size, verbose: verbose)
  end

  ##
  # It runs a command, and returns the output, error, and exit code.
  #
  # Args:
  #   cmd: The command to run.
  #   separated_results: Whether the results should be stored separately. Defaults to false.
  #   check_errors: Whether to check for errors or not. Defaults to true.
  #   timeout: The timeout to be used, in seconds. Defaults to 250 or the value of the DEFAULT_TIMEOUT environment variable.
  #   user: The user to be used to run the command. Defaults to root.
  #   successcodes: An array with the values to be accepted as success codes from the command run.
  #   buffer_size: The maximum buffer size in bytes. Defaults to 65536.
  #   verbose: Whether to log the output of the command in case of success. Defaults to false.
  def run_local(cmd, separated_results: false, check_errors: true, timeout: DEFAULT_TIMEOUT, user: 'root', successcodes: [0], buffer_size: 65_536, verbose: false)
    if separated_results
      out, err, _lo, _rem, code = test_and_store_results_separately(cmd, user, timeout, buffer_size)
    else
      out, _lo, _rem, code = test_and_store_results_together(cmd, user, timeout, buffer_size)
    end
    raise ScriptError, "FAIL: #{cmd} returned status code = #{code}.\nOutput:\n#{out}" if check_errors && !successcodes.include?(code)

    $stdout.puts "#{cmd} returned status code = #{code}.\nOutput:\n'#{out}'" if verbose
    if separated_results
      [out, err, code]
    else
      [out, code]
    end
  end

  ##
  # It runs a command until it succeeds or times out.
  #
  # Args:
  #   cmd: The command to run.
  def run_until_ok(cmd, timeout: DEFAULT_TIMEOUT, runs_in_container: true)
    repeat_until_timeout(timeout: timeout, report_result: true) do
      result, code = run(cmd, check_errors: false, runs_in_container: runs_in_container)
      return [result, code] if code.zero?

      sleep 2
      result
    end
  end

  ##
  # It runs a command until it fails, or until it times out.
  #
  # Args:
  #   cmd: The command to run.
  def run_until_fail(cmd, timeout: DEFAULT_TIMEOUT, runs_in_container: true)
    repeat_until_timeout(timeout: timeout, report_result: true) do
      result, code = run(cmd, check_errors: false, runs_in_container: runs_in_container)
      return [result, code] if code.nonzero?

      sleep 2
      result
    end
  end

  ##
  # It waits until the process is no longer running.
  #
  # Args:
  #   process: The name of the process to wait for.
  def wait_while_process_running(process)
    repeat_until_timeout(report_result: true) do
      result, code = run("pgrep -x #{process} >/dev/null", check_errors: false)
      return [result, code] if code.nonzero?

      sleep 2
      result
    end
  end

  ##
  # Copy a local file to a remote node.
  # Handles copying to the server container if possible
  #
  # Args:
  #   local_file: The path to the file to copy
  #   remote_file: The path in the destination
  #   user: The owner of the file
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

  ##
  # Copy a remote file to a local one
  # Handles copying from the server container if possible
  #
  # Args:
  #   remote_file: The path in the destination
  #   local_file: The path to the file to copy
  #   user: The owner of the file
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

  ##
  # Check if a file exists on a node.
  # Handles checking in server container if possible.
  #
  # Args:
  #   file: The path to check on the node.
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

  ##
  # Check if a folder exists on a node.
  # Handles checking in server container if possible.
  #
  # Args:
  #   file: The path to check on the node.
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

  ##
  # Delete a file on a node.
  # Handles checking in server container if possible.
  #
  # Args:
  #   file: The path of the file to delete on the node.
  def file_delete(file)
    if @in_has_mgrctl
      _out, code = run_local("mgrctl exec -- 'rm #{file}'", check_errors: false)
    else
      _out, _local, _remote, code = test_and_store_results_together("rm #{file}", 'root', 500)
    end
    code
  end

  ##
  # Delete a folder on a node.
  # Handles checking in server container if possible.
  #
  # Args:
  #   folder: The path of the folder to delete on the node.
  def folder_delete(folder)
    if @in_has_mgrctl
      _out, code = run_local("mgrctl exec -- 'rm -rf #{folder}'", check_errors: false)
    else
      _out, _local, _remote, code = test_and_store_results_together("rm -rf #{folder}", 'root', 500)
    end
    code
  end
end
