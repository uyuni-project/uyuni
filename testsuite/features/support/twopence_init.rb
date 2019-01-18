# Copyright (c) 2016-2018 SUSE-LINUX
# Licensed under the terms of the MIT license.

require 'twopence'

# Initialize SSH targets from environment variables
# If there is no proxy, don't define $PROXY environment variable
raise 'Server IP address or domain name variable empty' if ENV['SERVER'].nil?
warn 'Proxy IP address or domain name variable empty' if ENV['PROXY'].nil?
raise 'Client IP address or domain name variable empty' if ENV['CLIENT'].nil?
raise 'Minion IP address or domain name variable empty' if ENV['MINION'].nil?
warn 'CentOS minion IP address or domain name variable empty' if ENV['CENTOSMINION'].nil?
warn 'SSH minion IP address or domain name variable empty' if ENV['SSHMINION'].nil?

# Define twopence objects
$client = Twopence.init("ssh:#{ENV['CLIENT']}")
$proxy = Twopence.init("ssh:#{ENV['PROXY']}") if ENV['PROXY']
$server = Twopence.init("ssh:#{ENV['SERVER']}")
$minion = Twopence.init("ssh:#{ENV['MINION']}")
$ceos_minion = Twopence.init("ssh:#{ENV['CENTOSMINION']}") if ENV['CENTOSMINION']
$ssh_minion = Twopence.init("ssh:#{ENV['SSHMINION']}") if ENV['SSHMINION']

# Lavanda library module extension
# Look at support/lavanda.rb for more details
nodes = [$server, $proxy, $client, $minion, $ceos_minion, $ssh_minion]
nodes.each do |node|
  next if node.nil?

  node.extend(LavandaBasic)
end

# Initialize hostname
nodes.each do |node|
  next if node.nil?

  hostname, _local, _remote, code = node.test_and_store_results_together('hostname', 'root', 500)
  raise 'Cannot get hostname for node' if code.nonzero? || hostname.empty?
  node.init_hostname(hostname)

  fqdn, _local, _remote, code = node.test_and_store_results_together('hostname -f', 'root', 500)
  raise 'No fully qualified domain name for node' if code.nonzero? || fqdn.empty?
  node.init_full_hostname(fqdn)

  puts "Determined hostname #{hostname.strip} and fqdn #{fqdn.strip}"
end

# Initialize IP address or domain name
nodes.each do |node|
  next if node.nil?

  node.init_ip(node.full_hostname)
end

# This function is used to get one of the nodes based on its type
def get_target(host)
  nodes_map = {
    'server' => $server,
    'proxy' => $proxy,
    'ceos-minion' => $ceos_minion,
    'ceos-ssh-minion' => $ceos_minion,
    'ceos-client' => $ceos_minion,
    'ssh-minion' => $ssh_minion,
    'sle-minion' => $minion,
    'sle-client' => $client,
    'sle-migrated-minion' => $client
  }
  node = nodes_map[host]
  raise 'Invalid target' if node.nil?
  node
end

# This function gets the system name, as displayed in systems list
def get_system_name(host)
  node = get_target(host)
  system_name = node.full_hostname
  system_name
end

# This function tests whether a file exists on a node
def file_exists?(node, file)
  _out, _local, _remote, code = node.test_and_store_results_together("test -f #{file}", 'root', 500)
  code.zero?
end

# This function deletes a file from a node
def file_delete(node, file)
  _out, _local, _remote, code = node.test_and_store_results_together("rm  #{file}", 'root', 500)
  code
end

# This function extracts a file from a node
def file_extract(node, remote_file, local_file)
  code, _remote = node.extract_file(remote_file, local_file, 'root', false)
  code
end

# This function injects a file into a node
def file_inject(node, local_file, remote_file)
  code, _remote = node.inject_file(local_file, remote_file, 'root', false)
  code
end

# Other global variables
$sle15_minion = minion_is_sle15
$mirror = ENV['MIRROR']
