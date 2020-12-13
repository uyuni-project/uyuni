# Copyright (c) 2016-2020 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'twopence'

# Initialize SSH targets from environment variables
raise 'Server IP address or domain name variable empty' if ENV['SERVER'].nil?
warn 'Proxy IP address or domain name variable empty' if ENV['PROXY'].nil?
warn 'Client IP address or domain name variable empty' if ENV['CLIENT'].nil?
warn 'Minion IP address or domain name variable empty' if ENV['MINION'].nil?
warn 'Buildhost IP address or domain name variable empty' if ENV['BUILD_HOST'].nil?
warn 'CentOS minion IP address or domain name variable empty' if ENV['CENTOSMINION'].nil?
warn 'SSH minion IP address or domain name variable empty' if ENV['SSHMINION'].nil?
warn 'PXE boot MAC address variable empty' if ENV['PXEBOOT_MAC'].nil?
warn 'KVM server minion IP address or domain name variable empty' if ENV['VIRTHOST_KVM_URL'].nil?
warn 'XEN server minion IP address or domain name variable empty' if ENV['VIRTHOST_XEN_URL'].nil?

# Preserve FQDN before initialization
$named_nodes = {}

def twopence_init(target)
  init_target = Twopence.init(target)
  $named_nodes[init_target.hash] = target.split(':')[1]
  init_target
end

# Define common twopence objects
$localhost = twopence_init("ssh:#{ENV['HOSTNAME']}") unless $debug_mode
$proxy = twopence_init("ssh:#{ENV['PROXY']}") if ENV['PROXY']
$server = twopence_init("ssh:#{ENV['SERVER']}")
$kvm_server = twopence_init("ssh:#{ENV['VIRTHOST_KVM_URL']}") if ENV['VIRTHOST_KVM_URL'] && ENV['VIRTHOST_KVM_PASSWORD']
$xen_server = twopence_init("ssh:#{ENV['VIRTHOST_XEN_URL']}") if ENV['VIRTHOST_XEN_URL'] && ENV['VIRTHOST_XEN_PASSWORD']

$nodes = [$localhost, $server, $proxy, $kvm_server, $xen_server]

if $qam_test
  # Define twopence objects for QAM environment
  $sle11sp4_minion = twopence_init("ssh:#{ENV['SLE11SP4_MINION']}") if ENV['SLE11SP4_MINION']
  $sle11sp4_ssh_minion = twopence_init("ssh:#{ENV['SLE11SP4_SSHMINION']}") if ENV['SLE11SP4_SSHMINION']
  $sle11sp4_client = twopence_init("ssh:#{ENV['SLE11SP4_CLIENT']}") if ENV['SLE11SP4_CLIENT']
  $sle12sp4_minion = twopence_init("ssh:#{ENV['SLE12SP4_MINION']}") if ENV['SLE12SP4_MINION']
  $sle12sp4_ssh_minion = twopence_init("ssh:#{ENV['SLE12SP4_SSHMINION']}") if ENV['SLE12SP4_SSHMINION']
  $sle12sp4_client = twopence_init("ssh:#{ENV['SLE12SP4_CLIENT']}") if ENV['SLE12SP4_CLIENT']
  $sle15_minion = twopence_init("ssh:#{ENV['SLE15_MINION']}") if ENV['SLE15_MINION']
  $sle15_ssh_minion = twopence_init("ssh:#{ENV['SLE15_SSHMINION']}") if ENV['SLE15_SSHMINION']
  $sle15_client = twopence_init("ssh:#{ENV['SLE15_CLIENT']}") if ENV['SLE15_CLIENT']
  $sle15sp1_minion = twopence_init("ssh:#{ENV['SLE15SP1_MINION']}") if ENV['SLE15SP1_MINION']
  $sle15sp1_ssh_minion = twopence_init("ssh:#{ENV['SLE15SP1_SSHMINION']}") if ENV['SLE15SP1_SSHMINION']
  $sle15sp1_client = twopence_init("ssh:#{ENV['SLE15SP1_CLIENT']}") if ENV['SLE15SP1_CLIENT']
  $ceos6_minion = twopence_init("ssh:#{ENV['CENTOS6_MINION']}") if ENV['CENTOS6_MINION']
  $ceos6_ssh_minion = twopence_init("ssh:#{ENV['CENTOS6_SSHMINION']}") if ENV['CENTOS6_SSHMINION']
  $ceos6_client = twopence_init("ssh:#{ENV['CENTOS6_CLIENT']}") if ENV['CENTOS6_CLIENT']
  $ceos7_minion = twopence_init("ssh:#{ENV['CENTOS7_MINION']}") if ENV['CENTOS7_MINION']
  $ceos7_ssh_minion = twopence_init("ssh:#{ENV['CENTOS7_SSHMINION']}") if ENV['CENTOS7_SSHMINION']
  $ceos7_client = twopence_init("ssh:#{ENV['CENTOS7_CLIENT']}") if ENV['CENTOS7_CLIENT']
  $ubuntu1604_minion = twopence_init("ssh:#{ENV['UBUNTU1604_MINION']}") if ENV['UBUNTU1604_MINION']
  $ubuntu1604_ssh_minion = twopence_init("ssh:#{ENV['UBUNTU1604_SSHMINION']}") if ENV['UBUNTU1604_SSHMINION']
  $ubuntu1804_minion = twopence_init("ssh:#{ENV['UBUNTU1804_MINION']}") if ENV['UBUNTU1804_MINION']
  $ubuntu1804_ssh_minion = twopence_init("ssh:#{ENV['UBUNTU1804_SSHMINION']}") if ENV['UBUNTU1804_SSHMINION']
  $ubuntu2004_minion = twopence_init("ssh:#{ENV['UBUNTU2004_MINION']}") if ENV['UBUNTU2004_MINION']
  $ubuntu2004_ssh_minion = twopence_init("ssh:#{ENV['UBUNTU2004_SSHMINION']}") if ENV['UBUNTU2004_SSHMINION']
  # As we share core features for QAM and QA environments, we share also those vm twopence objects
  $minion = $sle12sp4_minion
  $ssh_minion = $sle12sp4_ssh_minion
  $client = $sle12sp4_client
  $ceos_minion = $ceos6_ssh_minion
  $ubuntu_minion = $ubuntu1804_minion
  $nodes += [$sle11sp4_minion, $sle11sp4_ssh_minion, $sle11sp4_client,
             $sle12sp4_minion, $sle12sp4_ssh_minion, $sle12sp4_client,
             $sle15_minion, $sle15_ssh_minion, $sle15_client,
             $sle15sp1_minion, $sle15sp1_ssh_minion, $sle15sp1_client,
             $ceos6_minion, $ceos6_ssh_minion, $ceos6_client,
             $ceos7_minion, $ceos7_ssh_minion, $ceos7_client,
             $ubuntu1604_ssh_minion, $ubuntu1604_minion,
             $ubuntu1804_ssh_minion, $ubuntu1804_minion,
             $ubuntu2004_ssh_minion, $ubuntu2004_minion,
             $client, $minion, $ceos_minion, $ubuntu_minion, $ssh_minion]
else
  # Define twopence objects for QA environment
  $minion = twopence_init("ssh:#{ENV['MINION']}") if ENV['MINION']
  $build_host = twopence_init("ssh:#{ENV['BUILD_HOST']}") if ENV['BUILD_HOST']
  $ssh_minion = twopence_init("ssh:#{ENV['SSHMINION']}") if ENV['SSHMINION']
  $client = twopence_init("ssh:#{ENV['CLIENT']}") if ENV['CLIENT']
  $ceos_minion = twopence_init("ssh:#{ENV['CENTOSMINION']}") if ENV['CENTOSMINION']
  $ubuntu_minion = twopence_init("ssh:#{ENV['UBUNTUMINION']}") if ENV['UBUNTUMINION']
  $nodes += [$client, $minion, $build_host, $ceos_minion, $ubuntu_minion, $ssh_minion]
end

# Lavanda library module extension
# Look at support/lavanda.rb for more details
$nodes.each do |node|
  next if node.nil?

  node.extend(LavandaBasic)
end

# Initialize hostname
$nodes.each do |node|
  next if node.nil?

  hostname, _local, _remote, code = node.test_and_store_results_together('hostname', 'root', 500)
  raise "Cannot connect to get hostname for '#{$named_nodes[node.hash]}'. Response code: #{code}" if code.nonzero?
  raise "No hostname for '#{$named_nodes[node.hash]}'. Response code: #{code}" if hostname.empty?
  node.init_hostname(hostname)

  fqdn, _local, _remote, code = node.test_and_store_results_together('hostname -f', 'root', 500)
  raise "Cannot connect to get FQDN for '#{$named_nodes[node.hash]}'. Response code: #{code}" if code.nonzero?
  raise "No FQDN for '#{$named_nodes[node.hash]}'. Response code: #{code}" if fqdn.empty?
  node.init_full_hostname(fqdn)

  puts "Host '#{$named_nodes[node.hash]}' is alive with determined hostname #{hostname.strip} and FQDN #{fqdn.strip}" unless $qam_test
end

# Initialize IP address or domain name
$nodes.each do |node|
  next if node.nil?

  node.init_ip(node.full_hostname)
end

# This function is used to get one of the nodes based on its type
def get_target(host)
  node = $node_by_host[host]
  raise 'Invalid target' if node.nil?
  node
end

# This function gets the system name, as displayed in systems list
# * for the usual clients, it is the full hostname, e.g. hmu-centos.tf.local
# * for the PXE booted clients, it is derived from the branch name, the hardware type,
#   and a fingerprint, e.g. example.Intel-Genuine-None-d6df84cca6f478cdafe824e35bbb6e3b
def get_system_name(host)
  # If the system is not known, just return the parameter
  system_name = host

  if host == 'pxeboot_minion'
    # The PXE boot minion is not directly accessible on the network,
    # therefore it is not represented by a twopence node
    output, _code = $server.run('salt-key')
    system_name = output.split.find do |word|
      word =~ /example.Intel-Genuine-None-/ || word =~ /example.pxeboot-/ || word =~ /example.Intel/ || word =~ /pxeboot-/
    end
    system_name = 'pxeboot.example.org' if system_name.nil?
  else
    begin
      node = get_target(host)
      system_name = node.full_hostname
    rescue RuntimeError => e
      puts e.message
    end
  end
  system_name
end

# Get MAC address of system
def get_mac_address(host)
  if host == 'pxeboot_minion'
    mac = ENV['PXEBOOT_MAC']
  else
    node = get_target(host)
    output, _code = node.run('ip link show dev eth1')
    mac = output.split("\n")[1].split[1]
  end
  mac
end

# This function returns the net prefix, caching it
def net_prefix
  $net_prefix = $private_net.sub(%r{\.0+/24$}, '.') if $net_prefix.nil?
  $net_prefix
end

# This function tests whether a file exists on a node
def file_exists?(node, file)
  _out, local, _remote, code = node.test_and_store_results_together("test -f #{file}", 'root', 500)
  code.zero? && local.zero?
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
$product = product
$pxeboot_mac = ENV['PXEBOOT_MAC']
$private_net = ENV['PRIVATENET'] if ENV['PRIVATENET']
$mirror = ENV['MIRROR']
$server_http_proxy = ENV['SERVER_HTTP_PROXY'] if ENV['SERVER_HTTP_PROXY']
$no_auth_registry = ENV['NO_AUTH_REGISTRY'] if ENV['NO_AUTH_REGISTRY']
$auth_registry = ENV['AUTH_REGISTRY'] if ENV['AUTH_REGISTRY']
if ENV['SCC_CREDENTIALS']
  scc_username, scc_password = ENV['SCC_CREDENTIALS'].split('|')
  $scc_credentials = !scc_username.to_s.empty? && !scc_password.to_s.empty?
end
$node_by_host = { 'localhost'             => $localhost,
                  'server'                => $server,
                  'proxy'                 => $proxy,
                  'ceos_minion'           => $ceos_minion,
                  'ceos_ssh_minion'       => $ceos_minion,
                  'ceos_client'           => $ceos_minion,
                  'ubuntu_minion'         => $ubuntu_minion,
                  'ubuntu_ssh_minion'     => $ubuntu_minion,
                  'ssh_minion'            => $ssh_minion,
                  'sle_minion'            => $minion,
                  'sle_ssh_tunnel_minion' => $minion,
                  'build_host'            => $build_host,
                  'sle_client'            => $client,
                  'sle_ssh_tunnel_client' => $client,
                  'kvm_server'            => $kvm_server,
                  'xen_server'            => $xen_server,
                  'sle_migrated_minion'   => $client,
                  'ceos6_minion'          => $ceos6_minion,
                  'ceos6_ssh_minion'      => $ceos6_ssh_minion,
                  'ceos6_client'          => $ceos6_client,
                  'ceos7_minion'          => $ceos7_minion,
                  'ceos7_ssh_minion'      => $ceos7_ssh_minion,
                  'ceos7_client'          => $ceos7_client,
                  'ubuntu1604_minion'     => $ubuntu1604_minion,
                  'ubuntu1604_ssh_minion' => $ubuntu1604_ssh_minion,
                  'ubuntu1804_minion'     => $ubuntu1804_minion,
                  'ubuntu1804_ssh_minion' => $ubuntu1804_ssh_minion,
                  'ubuntu2004_minion'     => $ubuntu2004_minion,
                  'ubuntu2004_ssh_minion' => $ubuntu2004_ssh_minion,
                  'sle11sp4_ssh_minion'   => $sle11sp4_ssh_minion,
                  'sle11sp4_minion'       => $sle11sp4_minion,
                  'sle11sp4_client'       => $sle11sp4_client,
                  'sle12sp4_ssh_minion'   => $sle12sp4_ssh_minion,
                  'sle12sp4_minion'       => $sle12sp4_minion,
                  'sle12sp4_client'       => $sle12sp4_client,
                  'sle15_ssh_minion'      => $sle15_ssh_minion,
                  'sle15_minion'          => $sle15_minion,
                  'sle15_client'          => $sle15_client,
                  'sle15sp1_ssh_minion'   => $sle15sp1_ssh_minion,
                  'sle15sp1_minion'       => $sle15sp1_minion,
                  'sle15sp1_client'       => $sle15sp1_client }
