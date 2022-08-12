# Copyright (c) 2016-2022 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'require_all'
require 'twopence'
require_all 'features/support'

# Initialize SSH targets from environment variables
raise 'Server IP address or domain name variable empty' if ENV['SERVER'].nil?
warn 'Proxy IP address or domain name variable empty' if ENV['PROXY'].nil?
warn 'Client IP address or domain name variable empty' if ENV['CLIENT'].nil?
warn 'Minion IP address or domain name variable empty' if ENV['MINION'].nil?
warn 'Buildhost IP address or domain name variable empty' if ENV['BUILD_HOST'].nil?
warn 'Red Hat-like minion IP address or domain name variable empty' if ENV['RHLIKE_MINION'].nil?
warn 'Debian-like minion IP address or domain name variable empty' if ENV['DEBLIKE_MINION'].nil?
warn 'SSH minion IP address or domain name variable empty' if ENV['SSH_MINION'].nil?
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

if $build_validation
  # Define twopence objects for Build Validation environment
  $sle12sp4_client = twopence_init("ssh:#{ENV['SLE12SP4_CLIENT']}") if ENV['SLE12SP4_CLIENT']
  $sle12sp4_minion = twopence_init("ssh:#{ENV['SLE12SP4_MINION']}") if ENV['SLE12SP4_MINION']
  $sle12sp4_ssh_minion = twopence_init("ssh:#{ENV['SLE12SP4_SSHMINION']}") if ENV['SLE12SP4_SSHMINION']
  $sle12sp5_client = twopence_init("ssh:#{ENV['SLE12SP5_CLIENT']}") if ENV['SLE12SP5_CLIENT']
  $sle12sp5_minion = twopence_init("ssh:#{ENV['SLE12SP5_MINION']}") if ENV['SLE12SP5_MINION']
  $sle12sp5_ssh_minion = twopence_init("ssh:#{ENV['SLE12SP5_SSHMINION']}") if ENV['SLE12SP5_SSHMINION']
  $sle15_client = twopence_init("ssh:#{ENV['SLE15_CLIENT']}") if ENV['SLE15_CLIENT']
  $sle15_minion = twopence_init("ssh:#{ENV['SLE15_MINION']}") if ENV['SLE15_MINION']
  $sle15_ssh_minion = twopence_init("ssh:#{ENV['SLE15_SSHMINION']}") if ENV['SLE15_SSHMINION']
  $sle15sp1_client = twopence_init("ssh:#{ENV['SLE15SP1_CLIENT']}") if ENV['SLE15SP1_CLIENT']
  $sle15sp1_minion = twopence_init("ssh:#{ENV['SLE15SP1_MINION']}") if ENV['SLE15SP1_MINION']
  $sle15sp1_ssh_minion = twopence_init("ssh:#{ENV['SLE15SP1_SSHMINION']}") if ENV['SLE15SP1_SSHMINION']
  $sle15sp2_client = twopence_init("ssh:#{ENV['SLE15SP2_CLIENT']}") if ENV['SLE15SP2_CLIENT']
  $sle15sp2_minion = twopence_init("ssh:#{ENV['SLE15SP2_MINION']}") if ENV['SLE15SP2_MINION']
  $sle15sp2_ssh_minion = twopence_init("ssh:#{ENV['SLE15SP2_SSHMINION']}") if ENV['SLE15SP2_SSHMINION']
  $sle15sp3_client = twopence_init("ssh:#{ENV['SLE15SP3_CLIENT']}") if ENV['SLE15SP3_CLIENT']
  $sle15sp3_minion = twopence_init("ssh:#{ENV['SLE15SP3_MINION']}") if ENV['SLE15SP3_MINION']
  $sle15sp3_ssh_minion = twopence_init("ssh:#{ENV['SLE15SP3_SSHMINION']}") if ENV['SLE15SP3_SSHMINION']
  $sle15sp4_client = twopence_init("ssh:#{ENV['SLE15SP4_CLIENT']}") if ENV['SLE15SP4_CLIENT']
  $sle15sp4_minion = twopence_init("ssh:#{ENV['SLE15SP4_MINION']}") if ENV['SLE15SP4_MINION']
  $sle15sp4_ssh_minion = twopence_init("ssh:#{ENV['SLE15SP4_SSHMINION']}") if ENV['SLE15SP4_SSHMINION']
  $centos7_client = twopence_init("ssh:#{ENV['CENTOS7_CLIENT']}") if ENV['CENTOS7_CLIENT']
  $centos7_minion = twopence_init("ssh:#{ENV['CENTOS7_MINION']}") if ENV['CENTOS7_MINION']
  $centos7_ssh_minion = twopence_init("ssh:#{ENV['CENTOS7_SSHMINION']}") if ENV['CENTOS7_SSHMINION']
  $rocky8_minion = twopence_init("ssh:#{ENV['ROCKY8_MINION']}") if ENV['ROCKY8_MINION']
  $rocky8_ssh_minion = twopence_init("ssh:#{ENV['ROCKY8_SSHMINION']}") if ENV['ROCKY8_SSHMINION']
  $ubuntu1804_minion = twopence_init("ssh:#{ENV['UBUNTU1804_MINION']}") if ENV['UBUNTU1804_MINION']
  $ubuntu1804_ssh_minion = twopence_init("ssh:#{ENV['UBUNTU1804_SSHMINION']}") if ENV['UBUNTU1804_SSHMINION']
  $ubuntu2004_minion = twopence_init("ssh:#{ENV['UBUNTU2004_MINION']}") if ENV['UBUNTU2004_MINION']
  $ubuntu2004_ssh_minion = twopence_init("ssh:#{ENV['UBUNTU2004_SSHMINION']}") if ENV['UBUNTU2004_SSHMINION']
  $ubuntu2204_minion = twopence_init("ssh:#{ENV['UBUNTU2204_MINION']}") if ENV['UBUNTU2204_MINION']
  $ubuntu2204_ssh_minion = twopence_init("ssh:#{ENV['UBUNTU2204_SSHMINION']}") if ENV['UBUNTU2204_SSHMINION']
  $debian9_minion = twopence_init("ssh:#{ENV['DEBIAN9_MINION']}") if ENV['DEBIAN9_MINION']
  $debian9_ssh_minion = twopence_init("ssh:#{ENV['DEBIAN9_SSHMINION']}") if ENV['DEBIAN9_SSHMINION']
  $debian10_minion = twopence_init("ssh:#{ENV['DEBIAN10_MINION']}") if ENV['DEBIAN10_MINION']
  $debian10_ssh_minion = twopence_init("ssh:#{ENV['DEBIAN10_SSHMINION']}") if ENV['DEBIAN10_SSHMINION']
  $debian11_minion = twopence_init("ssh:#{ENV['DEBIAN11_MINION']}") if ENV['DEBIAN11_MINION']
  $debian11_ssh_minion = twopence_init("ssh:#{ENV['DEBIAN11_SSHMINION']}") if ENV['DEBIAN11_SSHMINION']
  $sle12sp5_buildhost = twopence_init("ssh:#{ENV['SLE12SP5_BUILDHOST']}") if ENV['SLE12SP5_BUILDHOST']
  $sle15sp4_buildhost = twopence_init("ssh:#{ENV['SLE15SP4_BUILDHOST']}") if ENV['SLE15SP4_BUILDHOST']
  $opensuse153arm_minion = twopence_init("ssh:#{ENV['OPENSUSE153ARM_MINION']}") if ENV['OPENSUSE153ARM_MINION']
  $nodes += [$sle12sp4_client, $sle12sp4_minion, $sle12sp4_ssh_minion,
             $sle12sp5_client, $sle12sp5_minion, $sle12sp5_ssh_minion,
             $sle15_client, $sle15_minion, $sle15_ssh_minion,
             $sle15sp1_client, $sle15sp1_minion, $sle15sp1_ssh_minion,
             $sle15sp2_client, $sle15sp2_minion, $sle15sp2_ssh_minion,
             $sle15sp3_client, $sle15sp3_minion, $sle15sp3_ssh_minion,
             $sle15sp4_client, $sle15sp4_minion, $sle15sp4_ssh_minion,
             $centos7_client, $centos7_minion, $centos7_ssh_minion,
             $rocky8_minion, $rocky8_ssh_minion,
             $ubuntu1804_minion, $ubuntu1804_ssh_minion,
             $ubuntu2004_minion, $ubuntu2004_ssh_minion,
             $ubuntu2204_minion, $ubuntu2204_ssh_minion,
             $debian9_minion, $debian9_ssh_minion,
             $debian10_minion, $debian10_ssh_minion,
             $debian11_minion, $debian11_ssh_minion,
             $sle12sp5_buildhost,
             $sle15sp4_buildhost,
             $opensuse153arm_minion]
else
  # Define twopence objects for QA environment
  $client = twopence_init("ssh:#{ENV['CLIENT']}") if ENV['CLIENT']
  $minion = twopence_init("ssh:#{ENV['MINION']}") if ENV['MINION']
  $ssh_minion = twopence_init("ssh:#{ENV['SSH_MINION']}") if ENV['SSH_MINION']
  $rhlike_minion = twopence_init("ssh:#{ENV['RHLIKE_MINION']}") if ENV['RHLIKE_MINION']
  $deblike_minion = twopence_init("ssh:#{ENV['DEBLIKE_MINION']}") if ENV['DEBLIKE_MINION']
  $build_host = twopence_init("ssh:#{ENV['BUILD_HOST']}") if ENV['BUILD_HOST']
  $nodes += [$client, $minion, $ssh_minion, $rhlike_minion, $deblike_minion, $build_host]
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

  STDOUT.puts "Host '#{$named_nodes[node.hash]}' is alive with determined hostname #{hostname.strip} and FQDN #{fqdn.strip}" unless $build_validation
end

# This function is used to get one of the nodes based on its type
def get_target(host)
  node = $node_by_host[host]
  raise 'Invalid target' if node.nil?
  node
end

# This function gets the system name, as displayed in systems list
# * for the usual clients, it is the full hostname, e.g. suma-41-min-sle15.tf.local
# * for the PXE booted clients, it is derived from the branch name, the hardware type,
#   and a fingerprint, e.g. example.Intel-Genuine-None-d6df84cca6f478cdafe824e35bbb6e3b
# rubocop:disable Metrics/MethodLength
def get_system_name(host)
  # If the system is not known, just return the parameter
  system_name = host

  case host
  # The PXE boot minion and the terminals are not directly accessible on the network,
  # therefore they are not represented by a twopence node
  when 'pxeboot_minion'
    output, _code = $server.run('salt-key')
    system_name = output.split.find do |word|
      word =~ /example.Intel-Genuine-None-/ || word =~ /example.pxeboot-/ || word =~ /example.Intel/ || word =~ /pxeboot-/
    end
    system_name = 'pxeboot.example.org' if system_name.nil?
  when 'sle12sp5_terminal'
    output, _code = $server.run('salt-key')
    system_name = output.split.find do |word|
      word =~ /example.sle12sp5terminal-/
    end
    system_name = 'sle12sp5terminal.example.org' if system_name.nil?
  when 'sle15sp4_terminal'
    output, _code = $server.run('salt-key')
    system_name = output.split.find do |word|
      word =~ /example.sle15sp4terminal-/
    end
    system_name = 'sle15sp4terminal.example.org' if system_name.nil?
  else
    begin
      node = get_target(host)
      system_name = node.full_hostname
    rescue RuntimeError => e
      STDOUT.puts e.message
    end
  end
  system_name
end
# rubocop:enable Metrics/MethodLength

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
  $net_prefix = $private_net.sub(%r{\.0+/24$}, '.') if $net_prefix.nil? && !$private_net.nil?
  $net_prefix
end

# This function tests whether a file exists on a node
def file_exists?(node, file)
  _out, local, _remote, code = node.test_and_store_results_together("test -f #{file}", 'root', 500)
  code.zero? && local.zero?
end

# This function tests whether a folder exists on a node
def folder_exists?(node, file)
  _out, local, _remote, code = node.test_and_store_results_together("test -d #{file}", 'root', 500)
  code.zero? && local.zero?
end

# This function deletes a file from a node
def file_delete(node, file)
  _out, _local, _remote, code = node.test_and_store_results_together("rm  #{file}", 'root', 500)
  code
end

# This function deletes a file from a node
def folder_delete(node, folder)
  _out, _local, _remote, code = node.test_and_store_results_together("rm -rf #{folder}", 'root', 500)
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
$product_version = product_version
$use_salt_bundle = use_salt_bundle
$pxeboot_mac = ENV['PXEBOOT_MAC']
$pxeboot_image = ENV['PXEBOOT_IMAGE'] || 'sles15sp3o'
$sle12sp5_terminal_mac = ENV['SLE12SP5_TERMINAL_MAC']
$sle15sp4_terminal_mac = ENV['SLE15SP4_TERMINAL_MAC']
$private_net = ENV['PRIVATENET'] if ENV['PRIVATENET']
$mirror = ENV['MIRROR']
$server_http_proxy = ENV['SERVER_HTTP_PROXY'] if ENV['SERVER_HTTP_PROXY']
$no_auth_registry = ENV['NO_AUTH_REGISTRY'] if ENV['NO_AUTH_REGISTRY']
$auth_registry = ENV['AUTH_REGISTRY'] if ENV['AUTH_REGISTRY']
if ENV['SCC_CREDENTIALS']
  scc_username, scc_password = ENV['SCC_CREDENTIALS'].split('|')
  $scc_credentials = !scc_username.to_s.empty? && !scc_password.to_s.empty?
end
$node_by_host = { 'localhost'                 => $localhost,
                  'server'                    => $server,
                  'proxy'                     => $proxy,
                  'sle_client'                => $client,
                  'sle_minion'                => $minion,
                  'ssh_minion'                => $ssh_minion,
                  'rhlike_client'             => $rhlike_minion,
                  'rhlike_minion'             => $rhlike_minion,
                  'deblike_minion'            => $deblike_minion,
                  'build_host'                => $build_host,
                  'kvm_server'                => $kvm_server,
                  'xen_server'                => $xen_server,
                  'sle12sp4_client'           => $sle12sp4_client,
                  'sle12sp4_minion'           => $sle12sp4_minion,
                  'sle12sp4_ssh_minion'       => $sle12sp4_ssh_minion,
                  'sle12sp5_client'           => $sle12sp5_client,
                  'sle12sp5_minion'           => $sle12sp5_minion,
                  'sle12sp5_ssh_minion'       => $sle12sp5_ssh_minion,
                  'sle15_client'              => $sle15_client,
                  'sle15_minion'              => $sle15_minion,
                  'sle15_ssh_minion'          => $sle15_ssh_minion,
                  'sle15sp1_client'           => $sle15sp1_client,
                  'sle15sp1_minion'           => $sle15sp1_minion,
                  'sle15sp1_ssh_minion'       => $sle15sp1_ssh_minion,
                  'sle15sp2_client'           => $sle15sp2_client,
                  'sle15sp2_minion'           => $sle15sp2_minion,
                  'sle15sp2_ssh_minion'       => $sle15sp2_ssh_minion,
                  'sle15sp3_client'           => $sle15sp3_client,
                  'sle15sp3_minion'           => $sle15sp3_minion,
                  'sle15sp3_ssh_minion'       => $sle15sp3_ssh_minion,
                  'sle15sp4_client'           => $sle15sp4_client,
                  'sle15sp4_minion'           => $sle15sp4_minion,
                  'sle15sp4_ssh_minion'       => $sle15sp4_ssh_minion,
                  'centos7_client'            => $centos7_client,
                  'centos7_minion'            => $centos7_minion,
                  'centos7_ssh_minion'        => $centos7_ssh_minion,
                  'rocky8_minion'            => $rocky8_minion,
                  'rocky8_ssh_minion'        => $rocky8_ssh_minion,
                  'ubuntu1804_minion'         => $ubuntu1804_minion,
                  'ubuntu1804_ssh_minion'     => $ubuntu1804_ssh_minion,
                  'ubuntu2004_minion'         => $ubuntu2004_minion,
                  'ubuntu2004_ssh_minion'     => $ubuntu2004_ssh_minion,
                  'ubuntu2204_minion'         => $ubuntu2204_minion,
                  'ubuntu2204_ssh_minion'     => $ubuntu2204_ssh_minion,
                  'debian9_minion'            => $debian9_minion,
                  'debian9_ssh_minion'        => $debian9_ssh_minion,
                  'debian10_minion'           => $debian10_minion,
                  'debian10_ssh_minion'       => $debian10_ssh_minion,
                  'debian11_minion'           => $debian11_minion,
                  'debian11_ssh_minion'       => $debian11_ssh_minion,
                  'sle12sp5_buildhost'        => $sle12sp5_buildhost,
                  'sle15sp4_buildhost'        => $sle15sp4_buildhost,
                  'opensuse153arm_minion'     => $opensuse153arm_minion }

# This is the inverse of `node_by_host`.
$host_by_node = {}
$node_by_host.each do |host, node|
  next if node.nil?

  [host, node].each do |it|
    raise ">>> Either host '#{host}' of node '#{node}' is empty.  Please check" if it == ''
  end

  $host_by_node[node] = host
end

def client_public_ip(host)
  node = $node_by_host[host]
  raise "Cannot resolve node for host '#{host}'" if node.nil?

  # For each node that we support we must know which network interface uses (see the case below).
  # Having the IP as an attribute is something useful for the clients.
  # Let's not implement it for nodes where we are likely not need this feature (e.g. ctl).
  not_implemented = [$localhost]
  not_implemented.each do |it|
    return 'NOT_IMPLEMENTED' if node == it
  end

  interface = case host
              when /^deblike/, /^debian11/, /^ubuntu/
                $is_cloud_provider ? 'eth0' : 'ens3'
              when 'kvm_server', 'xen_server'
                'br0'
              else
                'eth0'
              end
  node.init_public_interface(interface)
  output, code = node.run("ip address show dev #{interface} | grep 'inet '")
  raise 'Cannot resolve public ip' unless code.zero?

  output.split[1].split('/')[0]
end

# Initialize IP address or domain name
$nodes.each do |node|
  next if node.nil?
  next if node.is_a?(String) && node.empty?

  host = $host_by_node[node]
  raise "Cannot resolve host for node: '#{node.hostname}'" if host.nil? || host == ''

  if (ADDRESSES.key? host) && !$private_net.nil?
    node.init_private_ip(net_prefix + ADDRESSES[host])
    node.init_private_interface('eth1')
  end

  ip = client_public_ip host
  node.init_public_ip ip
end
