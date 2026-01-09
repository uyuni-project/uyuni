# Copyright (c) 2025 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'net/scp'
require 'net/ssh'
require 'openssl'
require 'stringio'

Net::SSH::Transport::Algorithms::ALGORITHMS.each_value { |algs| algs.reject! { |a| a.match(/^ecd(sa|h)-sha2/) } }
Net::SSH::KnownHosts::SUPPORTED_TYPE.reject! { |t| t.match(/^ecd(sa|h)-sha2/) }

# This method is used to execute a command on a remote host using SSH and return the output of the command.
#
# @param [String] command The command to execute on the remote host.
# @param [String] host The hostname or IP address of the remote host.
# @param [Integer] port The port to connect to on the remote host.
# @param [Integer] timeout The timeout to use when connecting to the remote host.
# @param [Integer] buffer_size The buffer size to use when connecting to the remote host.
# @return [Array] An array containing the stdout, stderr, and exit code of the command.
def ssh_command(command, host, port: 22, timeout: DEFAULT_TIMEOUT, buffer_size: 65_536)
  stdout = ''
  stderr = ''
  exit_code = -1
  begin
    Timeout.timeout(timeout) do # Enforce timeout on the entire SSH operation
      Net::SSH.start(host, nil, port: port, verify_host_key: :never, timeout: timeout, keepalive: true, max_pkt_size: buffer_size, config: true) do |ssh|
        stdout, stderr, exit_code = ssh_exec!(ssh, command, timeout: DEFAULT_TIMEOUT)
      end
    end
  rescue Timeout::Error
    puts "SSH operation timed out after #{timeout} seconds."
  rescue Net::SSH::ConnectionTimeout, Errno::ECONNREFUSED, Errno::EHOSTUNREACH
    puts "Unable to reach the SSH server at #{host}:#{port}"
  rescue Net::SSH::AuthenticationFailed
    puts "Authentication failed for user #{user} on #{host}"
  end

  [stdout, stderr, exit_code]
end

# This method is used to upload a file on a remote host from the test-runner (aka controller) using SCP and return the output of the command.
#
# @param [String] local_path The path to the file to be uploaded.
# @param [String] remote_path The path to the destination file.
# @param [String] host The hostname or IP address of the remote host.
# @param [Integer] port The port to connect to on the remote host.
# @param [Integer] timeout The timeout to use when connecting to the remote host.
# @param [Integer] buffer_size The buffer size to use when connecting to the remote host.
def scp_upload_command(local_path, remote_path, host, port: 22, timeout: DEFAULT_TIMEOUT, buffer_size: 65_536)
  begin
    Net::SSH.start(host, nil, port: port, verify_host_key: :never, keepalive: true, timeout: timeout, max_pkt_size: buffer_size, config: true) do |ssh|
      ssh.scp.upload! local_path, remote_path
    end
  rescue Net::SSH::ConnectionTimeout, Errno::ECONNREFUSED
    # The connection times out or is refused
  end
end

# This method is used to download a file from a remote host to the test-runner (aka controller) using SCP and return the output of the command.

# @param [String] remote_path The path of the file to be downloaded.
# @param [String] local_path The path to the destination file.
# @param [String] host The hostname or IP address of the remote host.
# @param [Integer] port The port to connect to on the remote host.
# @param [Integer] timeout The timeout to use when connecting to the remote host.
# @param [Integer] buffer_size The buffer size to use when connecting to the remote host.
def scp_download_command(remote_path, local_path, host, port: 22, timeout: DEFAULT_TIMEOUT, buffer_size: 65_536)
  begin
    Net::SSH.start(host, nil, port: port, verify_host_key: :never, keepalive: true, timeout: timeout, max_pkt_size: buffer_size, config: true) do |ssh|
      ssh.scp.download! remote_path, local_path
    end
  rescue Net::SSH::ConnectionTimeout, Errno::ECONNREFUSED
    # The connection times out or is refused
  end
end

# This helper method executes a command on an SSH session and returns the output.
# It's an internal helper used by ssh_command.
#
# @param [Net::SSH::Connection::Session] ssh The SSH session object.
# @param [String] command The command to execute on the remote host.
# @param [Integer] timeout The timeout to use when waiting for the command to complete.
# @return [Array] An array containing the stdout, stderr, and exit code of the command.
def ssh_exec!(ssh, command, timeout: 10)
  stdout = ''
  stderr = ''
  exit_code = -1

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

    Timeout.timeout(timeout) do
      channel.wait
    end
  end

  begin
    ssh.loop
  rescue IOError
    puts "The remote node #{ssh.host} has been disconnected."
  rescue Net::SSH::Disconnect
    puts 'The SSH session was unexpectedly terminated.'
  end

  [stdout, stderr, exit_code]
end

# This helper method generates dummy CA certificate
#
# @param [String] file name, the CA certificate will be stored there.
# @param [String] CA certificate subject.
def generate_dummy_cacert(filename, subject = '/DC=localdomain/DC=localhost/CN=dummy CA')
  begin
    root_key = OpenSSL::PKey::RSA.new(2048)
    root_ca = OpenSSL::X509::Certificate.new
    root_ca.public_key = root_key.public_key
    # RFC 5280, "v3" certificate
    root_ca.version = 2
    root_ca.serial = 1
    root_ca.subject = OpenSSL::X509::Name.parse(subject)
    root_ca.issuer = root_ca.subject
    root_ca.not_before = Time.now
    # 10 days
    root_ca.not_after = root_ca.not_before + (60 * 60 * 24 * 10)
    ef = OpenSSL::X509::ExtensionFactory.new
    ef.subject_certificate = root_ca
    ef.issuer_certificate = root_ca
    root_ca.add_extension(ef.create_extension('basicConstraints', 'CA:TRUE', true))
    root_ca.add_extension(ef.create_extension('keyUsage', 'keyCertSign, cRLSign', true))
    root_ca.sign(root_key, OpenSSL::Digest.new('SHA256'))
    File.write(filename, root_ca.to_pem)
  rescue StandardError => e
    # issues to generate certificate
    puts e.message
  end
end

# This helper method returns dummy CA certificate data
#
# @param [String] file name, it the CA certificate will be stored there.
# @return [String] dummy CA certificate
def get_dummy_cacert(filename)
  begin
    certificate = File.read(filename)
  rescue StandardError => e
    # issues to read certificate file
    puts e.message
  end
  certificate
end
