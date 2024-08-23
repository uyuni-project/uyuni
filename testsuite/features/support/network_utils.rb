# Copyright (c) 2024 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'net/scp'
require 'net/ssh'
require 'stringio'

Net::SSH::Transport::Algorithms::ALGORITHMS.each_value { |algs| algs.reject! { |a| a.match(/^ecd(sa|h)-sha2/) } }
Net::SSH::KnownHosts::SUPPORTED_TYPE.reject! { |t| t.match(/^ecd(sa|h)-sha2/) }

# This method is used to execute a command on a remote host using SSH and return the output of the command.
#
# @param [String] command The command to execute on the remote host.
# @param [String] host The hostname or IP address of the remote host.
# @param [Integer] port The port to connect to on the remote host.
# @param [String] user The username to use when connecting to the remote host.
# @param [String] password The password to use when connecting to the remote host.
# @param [Integer] timeout The timeout to use when connecting to the remote host.
# @param [Integer] buffer_size The buffer size to use when connecting to the remote host.
# @return [Array] An array containing the stdout, stderr, and exit code of the command.
def ssh_command(command, host, port: 22, user: 'root', password: nil, timeout: DEFAULT_TIMEOUT, buffer_size: 65_536)
  stdout = ''
  stderr = ''
  exit_code = nil

  if password.nil?
    # Not passing :password uses systems ssh keys to authenticate
    Net::SSH.start(host, user, port: port, verify_host_key: :never, timeout: timeout, max_pkt_size: buffer_size) do |ssh|
      stdout, stderr, exit_code = ssh_exec!(ssh, command)
    end
  else
    Net::SSH.start(host, user, port: port, password: password, verify_host_key: :never, timeout: timeout, max_pkt_size: buffer_size) do |ssh|
      stdout, stderr, exit_code = ssh_exec!(ssh, command)
    end
  end

  [stdout, stderr, exit_code]
end

# This method is used to execute a command on a remote host using SSH and return the output of the command.
#
# @param [String] local_path The path to the file to be copied.
# @param [String] remote_path The path to the destination file.
# @param [String] host The hostname or IP address of the remote host.
# @param [Integer] port The port to connect to on the remote host.
# @param [String] user The username to use when connecting to the remote host.
# @param [String] password The password to use when connecting to the remote host.
# @param [Integer] timeout The timeout to use when connecting to the remote host.
# @param [Integer] buffer_size The buffer size to use when connecting to the remote host.
def scp_command(local_path, remote_path, host, port: 22, user: 'root', password: nil, timeout: DEFAULT_TIMEOUT, buffer_size: 65_536)
  if password.nil?
    # Not passing :password uses systems ssh keys to authenticate
    Net::SSH.start(host, user, port: port, verify_host_key: :never, timeout: timeout, max_pkt_size: buffer_size) do |ssh|
      ssh.scp.upload! local_path, remote_path
    end
  else
    Net::SSH.start(host, user, port: port, password: password, verify_host_key: :never, timeout: timeout, max_pkt_size: buffer_size) do |ssh|
      ssh.scp.upload! local_path, remote_path
    end
  end
end

private

def ssh_exec!(ssh, command)
  stdout = ''
  stderr = ''
  exit_code = nil

  ssh.open_channel do |channel|
    channel.exec(command) do |_ch, success|
      raise SystemCallError, 'FAILED: could not execute command (ssh.channel.exec)' unless success

      channel.on_data do |_ch, data|
        stdout += data
      end

      channel.on_extended_data do |_ch, _type, data|
        stderr += data
      end

      channel.on_request('exit-status') do |_ch, data|
        exit_code = data.read_long
      end
    end
  end
  ssh.loop

  [stdout, stderr, exit_code]
end
