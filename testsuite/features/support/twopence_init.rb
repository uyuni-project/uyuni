require 'twopence'

# Initialize SSH targets from environment variables
# If there is no proxy, don't define $PROXY environment variable
raise 'Server IP address or domain name variable empty' if ENV['TESTHOST'].nil?
warn 'Proxy IP address or domain name variable empty' if ENV['PROXY'].nil?
raise 'Client IP address or domain name variable empty' if ENV['CLIENT'].nil?
raise 'Minion IP address or domain name variable empty' if ENV['MINION'].nil?
raise 'CentOS minion IP address or domain name variable empty' if ENV['CENTOSMINION'].nil?
raise 'SSH minion IP address or domain name variable empty' if ENV['SSHMINION'].nil?

$server_ip = ENV['TESTHOST']
$proxy_ip = ENV['PROXY']
$client_ip = ENV['CLIENT']
$minion_ip = ENV['MINION']
$ceos_minion_ip = ENV['CENTOSMINION']
$ssh_minion_ip = ENV['SSHMINION']

# Define twopence objects
$client = Twopence.init("ssh:#{$client_ip}")
$proxy = Twopence.init("ssh:#{$proxy_ip}") if $proxy_ip
$server = Twopence.init("ssh:#{$server_ip}")
$minion = Twopence.init("ssh:#{$minion_ip}")
$ceos_minion = Twopence.init("ssh:#{$ceos_minion_ip}")
$ssh_minion = Twopence.init("ssh:#{$ssh_minion_ip}")

# Lavanda library module extension
# Look at support/lavanda.rb for more details
$server.extend(LavandaBasic)
$proxy.extend(LavandaBasic) if $proxy
$client.extend(LavandaBasic)
$minion.extend(LavandaBasic)
$ceos_minion.extend(LavandaBasic)
$ssh_minion.extend(LavandaBasic)

# Initialize hostname
nodes = [$server, $proxy, $client, $minion, $ceos_minion, $ssh_minion]
nodes.each do |node|
  next if node.nil?
  hostname, _local, _remote, code = node.test_and_store_results_together('hostname', 'root', 500)
  raise 'Cannot get hostname for node' if code.nonzero?
  node.init_hostname(hostname)
  fqdn, _local, _remote, code = node.test_and_store_results_together('hostname -f', 'root', 500)
  raise 'No fully qualified domain name for node' if code.nonzero?
  node.init_full_hostname(fqdn)
end

# This function is used to get one of the nodes based on its type
def get_target(host)
  nodes_map = {
    'server' => $server,
    'proxy' => $proxy,
    'ceos-minion' => $ceos_minion,
    'ssh-minion' => $ssh_minion,
    'sle-minion' => $minion,
    'sle-client' => $client,
    'ceos-traditional-client' => $ceos_minion,
    'sle-migrated-minion' => $client
  }
  node = nodes_map[host]
  raise 'Invalid target' if node.nil?
  node
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
