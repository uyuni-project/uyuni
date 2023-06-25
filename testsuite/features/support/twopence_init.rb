# Copyright (c) 2016-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'require_all'
require 'twopence'
require_all 'features/support'

# Initialize SSH targets from environment variables
raise 'Server IP address or domain name variable empty' if ENV['SERVER'].nil?
warn 'Proxy IP address or domain name variable empty' if ENV['PROXY'].nil?
unless $build_validation
  warn 'Minion IP address or domain name variable empty' if ENV['MINION'].nil?
  warn 'Buildhost IP address or domain name variable empty' if ENV['BUILD_HOST'].nil?
  warn 'Red Hat-like minion IP address or domain name variable empty' if ENV['RHLIKE_MINION'].nil?
  warn 'Debian-like minion IP address or domain name variable empty' if ENV['DEBLIKE_MINION'].nil?
  warn 'SSH minion IP address or domain name variable empty' if ENV['SSH_MINION'].nil?
  warn 'PXE boot MAC address variable empty' if ENV['PXEBOOT_MAC'].nil?
  warn 'KVM server minion IP address or domain name variable empty' if ENV['VIRTHOST_KVM_URL'].nil?
  warn 'Nested VM hostname empty' if ENV['MIN_NESTED'].nil?
  warn 'Nested VM MAC address empty' if ENV['MAC_MIN_NESTED'].nil?
end

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

$nodes = [$localhost, $server, $proxy, $kvm_server]

if $build_validation
  # Define twopence objects for Build Validation environment
  $sle12sp4_minion = twopence_init("ssh:#{ENV['SLE12SP4_MINION']}") if ENV['SLE12SP4_MINION']
  $sle12sp4_ssh_minion = twopence_init("ssh:#{ENV['SLE12SP4_SSHMINION']}") if ENV['SLE12SP4_SSHMINION']
  $sle12sp5_minion = twopence_init("ssh:#{ENV['SLE12SP5_MINION']}") if ENV['SLE12SP5_MINION']
  $sle12sp5_ssh_minion = twopence_init("ssh:#{ENV['SLE12SP5_SSHMINION']}") if ENV['SLE12SP5_SSHMINION']
  $sle15sp1_minion = twopence_init("ssh:#{ENV['SLE15SP1_MINION']}") if ENV['SLE15SP1_MINION']
  $sle15sp1_ssh_minion = twopence_init("ssh:#{ENV['SLE15SP1_SSHMINION']}") if ENV['SLE15SP1_SSHMINION']
  $sle15sp2_minion = twopence_init("ssh:#{ENV['SLE15SP2_MINION']}") if ENV['SLE15SP2_MINION']
  $sle15sp2_ssh_minion = twopence_init("ssh:#{ENV['SLE15SP2_SSHMINION']}") if ENV['SLE15SP2_SSHMINION']
  $sle15sp3_minion = twopence_init("ssh:#{ENV['SLE15SP3_MINION']}") if ENV['SLE15SP3_MINION']
  $sle15sp3_ssh_minion = twopence_init("ssh:#{ENV['SLE15SP3_SSHMINION']}") if ENV['SLE15SP3_SSHMINION']
  $sle15sp4_minion = twopence_init("ssh:#{ENV['SLE15SP4_MINION']}") if ENV['SLE15SP4_MINION']
  $sle15sp4_ssh_minion = twopence_init("ssh:#{ENV['SLE15SP4_SSHMINION']}") if ENV['SLE15SP4_SSHMINION']
  $sle15sp5_minion = twopence_init("ssh:#{ENV['SLE15SP5_MINION']}") if ENV['SLE15SP5_MINION']
  $sle15sp5_ssh_minion = twopence_init("ssh:#{ENV['SLE15SP5_SSHMINION']}") if ENV['SLE15SP5_SSHMINION']
  $slemicro51_minion = twopence_init("ssh:#{ENV['SLEMICRO51_MINION']}") if ENV['SLEMICRO51_MINION']
  $slemicro51_ssh_minion = twopence_init("ssh:#{ENV['SLEMICRO51_SSHMINION']}") if ENV['SLEMICRO51_SSHMINION']
  $slemicro52_minion = twopence_init("ssh:#{ENV['SLEMICRO52_MINION']}") if ENV['SLEMICRO52_MINION']
  $slemicro52_ssh_minion = twopence_init("ssh:#{ENV['SLEMICRO52_SSHMINION']}") if ENV['SLEMICRO52_SSHMINION']
  $slemicro53_minion = twopence_init("ssh:#{ENV['SLEMICRO53_MINION']}") if ENV['SLEMICRO53_MINION']
  $slemicro53_ssh_minion = twopence_init("ssh:#{ENV['SLEMICRO53_SSHMINION']}") if ENV['SLEMICRO53_SSHMINION']
  $slemicro54_minion = twopence_init("ssh:#{ENV['SLEMICRO54_MINION']}") if ENV['SLEMICRO54_MINION']
  $slemicro54_ssh_minion = twopence_init("ssh:#{ENV['SLEMICRO54_SSHMINION']}") if ENV['SLEMICRO54_SSHMINION']
  $alma9_minion = twopence_init("ssh:#{ENV['ALMA9_MINION']}") if ENV['ALMA9_MINION']
  $alma9_ssh_minion = twopence_init("ssh:#{ENV['ALMA9_SSHMINION']}") if ENV['ALMA9_SSHMINION']
  $centos7_minion = twopence_init("ssh:#{ENV['CENTOS7_MINION']}") if ENV['CENTOS7_MINION']
  $centos7_ssh_minion = twopence_init("ssh:#{ENV['CENTOS7_SSHMINION']}") if ENV['CENTOS7_SSHMINION']
  $liberty9_minion = twopence_init("ssh:#{ENV['LIBERTY9_MINION']}") if ENV['LIBERTY9_MINION']
  $liberty9_ssh_minion = twopence_init("ssh:#{ENV['LIBERTY9_SSHMINION']}") if ENV['LIBERTY9_SSHMINION']
  $oracle9_minion = twopence_init("ssh:#{ENV['ORACLE9_MINION']}") if ENV['ORACLE9_MINION']
  $oracle9_ssh_minion = twopence_init("ssh:#{ENV['ORACLE9_SSHMINION']}") if ENV['ORACLE9_SSHMINION']
  $rhel9_minion = twopence_init("ssh:#{ENV['RHEL9_MINION']}") if ENV['RHEL9_MINION']
  $rhel9_ssh_minion = twopence_init("ssh:#{ENV['RHEL9_SSHMINION']}") if ENV['RHEL9_SSHMINION']
  $rocky8_minion = twopence_init("ssh:#{ENV['ROCKY8_MINION']}") if ENV['ROCKY8_MINION']
  $rocky8_ssh_minion = twopence_init("ssh:#{ENV['ROCKY8_SSHMINION']}") if ENV['ROCKY8_SSHMINION']
  $rocky9_minion = twopence_init("ssh:#{ENV['ROCKY9_MINION']}") if ENV['ROCKY9_MINION']
  $rocky9_ssh_minion = twopence_init("ssh:#{ENV['ROCKY9_SSHMINION']}") if ENV['ROCKY9_SSHMINION']
  $ubuntu1804_minion = twopence_init("ssh:#{ENV['UBUNTU1804_MINION']}") if ENV['UBUNTU1804_MINION']
  $ubuntu1804_ssh_minion = twopence_init("ssh:#{ENV['UBUNTU1804_SSHMINION']}") if ENV['UBUNTU1804_SSHMINION']
  $ubuntu2004_minion = twopence_init("ssh:#{ENV['UBUNTU2004_MINION']}") if ENV['UBUNTU2004_MINION']
  $ubuntu2004_ssh_minion = twopence_init("ssh:#{ENV['UBUNTU2004_SSHMINION']}") if ENV['UBUNTU2004_SSHMINION']
  $ubuntu2204_minion = twopence_init("ssh:#{ENV['UBUNTU2204_MINION']}") if ENV['UBUNTU2204_MINION']
  $ubuntu2204_ssh_minion = twopence_init("ssh:#{ENV['UBUNTU2204_SSHMINION']}") if ENV['UBUNTU2204_SSHMINION']
  $debian10_minion = twopence_init("ssh:#{ENV['DEBIAN10_MINION']}") if ENV['DEBIAN10_MINION']
  $debian10_ssh_minion = twopence_init("ssh:#{ENV['DEBIAN10_SSHMINION']}") if ENV['DEBIAN10_SSHMINION']
  $debian11_minion = twopence_init("ssh:#{ENV['DEBIAN11_MINION']}") if ENV['DEBIAN11_MINION']
  $debian11_ssh_minion = twopence_init("ssh:#{ENV['DEBIAN11_SSHMINION']}") if ENV['DEBIAN11_SSHMINION']
  $opensuse154arm_minion = twopence_init("ssh:#{ENV['OPENSUSE154ARM_MINION']}") if ENV['OPENSUSE154ARM_MINION']
  $opensuse154arm_ssh_minion = twopence_init("ssh:#{ENV['OPENSUSE154ARM_SSHMINION']}") if ENV['OPENSUSE154ARM_SSHMINION']
  $opensuse155arm_minion = twopence_init("ssh:#{ENV['OPENSUSE155ARM_MINION']}") if ENV['OPENSUSE155ARM_MINION']
  $opensuse155arm_ssh_minion = twopence_init("ssh:#{ENV['OPENSUSE155ARM_SSHMINION']}") if ENV['OPENSUSE155ARM_SSHMINION']
  $sle12sp5_buildhost = twopence_init("ssh:#{ENV['SLE12SP5_BUILDHOST']}") if ENV['SLE12SP5_BUILDHOST']
  $sle15sp4_buildhost = twopence_init("ssh:#{ENV['SLE15SP4_BUILDHOST']}") if ENV['SLE15SP4_BUILDHOST']
  $monitoring_server = twopence_init("ssh:#{ENV['MONITORING_SERVER']}") if ENV['MONITORING_SERVER']
  $nodes += [$sle12sp4_minion, $sle12sp4_ssh_minion,
             $sle12sp5_minion, $sle12sp5_ssh_minion,
             $sle15sp1_minion, $sle15sp1_ssh_minion,
             $sle15sp2_minion, $sle15sp2_ssh_minion,
             $sle15sp3_minion, $sle15sp3_ssh_minion,
             $sle15sp4_minion, $sle15sp4_ssh_minion,
             $sle15sp5_minion, $sle15sp5_ssh_minion,
             $slemicro51_minion, $slemicro51_ssh_minion,
             $slemicro52_minion, $slemicro52_ssh_minion,
             $slemicro53_minion, $slemicro53_ssh_minion,
             $slemicro54_minion, $slemicro54_ssh_minion,
             $alma9_minion, $alma9_ssh_minion,
             $centos7_minion, $centos7_ssh_minion,
             $liberty9_minion, $liberty9_ssh_minion,
             $oracle9_minion, $oracle9_ssh_minion,
             $rhel9_minion, $rhel9_ssh_minion,
             $rocky8_minion, $rocky8_ssh_minion,
             $rocky9_minion, $rocky9_ssh_minion,
             $ubuntu1804_minion, $ubuntu1804_ssh_minion,
             $ubuntu2004_minion, $ubuntu2004_ssh_minion,
             $ubuntu2204_minion, $ubuntu2204_ssh_minion,
             $debian10_minion, $debian10_ssh_minion,
             $debian11_minion, $debian11_ssh_minion,
             $opensuse154arm_minion, $opensuse154arm_ssh_minion,
             $opensuse155arm_minion, $opensuse155arm_ssh_minion,
             $sle12sp5_buildhost,
             $sle15sp4_buildhost,
             $monitoring_server]
else
  # Define twopence objects for QA environment
  $minion = twopence_init("ssh:#{ENV['MINION']}") if ENV['MINION']
  $ssh_minion = twopence_init("ssh:#{ENV['SSH_MINION']}") if ENV['SSH_MINION']
  $rhlike_minion = twopence_init("ssh:#{ENV['RHLIKE_MINION']}") if ENV['RHLIKE_MINION']
  $deblike_minion = twopence_init("ssh:#{ENV['DEBLIKE_MINION']}") if ENV['DEBLIKE_MINION']
  $build_host = twopence_init("ssh:#{ENV['BUILD_HOST']}") if ENV['BUILD_HOST']
  $salt_migration_minion = twopence_init("ssh:#{ENV['MIN_NESTED']}") if ENV['MIN_NESTED']
  $nodes += [$minion, $ssh_minion, $rhlike_minion, $deblike_minion, $build_host, $salt_migration_minion]
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

  hostname, local, remote, code = node.test_and_store_results_together('hostname', 'root', 500)
  # special handling for nested VMs since they will only be crated later in the test suite
  # we to a late hostname initialization in a special step for those
  next if hostname.empty? || node == $salt_migration_minion

  raise "Cannot connect to get hostname for '#{$named_nodes[node.hash]}'. Response code: #{code}, local: #{local}, remote: #{remote}" if code.nonzero? || remote.nonzero? || local.nonzero?
  raise "No hostname for '#{$named_nodes[node.hash]}'. Response code: #{code}" if hostname.empty?
  node.init_hostname(hostname)

  fqdn, local, remote, code = node.test_and_store_results_together('hostname -f', 'root', 500)
  raise "Cannot connect to get FQDN for '#{$named_nodes[node.hash]}'. Response code: #{code}, local: #{local}, remote: #{remote}" if code.nonzero? || remote.nonzero? || local.nonzero?
  raise "No FQDN for '#{$named_nodes[node.hash]}'. Response code: #{code}" if fqdn.empty?
  node.init_full_hostname(fqdn)

  STDOUT.puts "Host '#{$named_nodes[node.hash]}' is alive with determined hostname #{hostname.strip} and FQDN #{fqdn.strip}" unless $build_validation
  os_version, os_family = get_os_version(node)
  node.init_os_family(os_family)
  node.init_os_version(os_version)
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
def get_system_name(host)
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
  when 'containerized_proxy'
    system_name = $proxy.full_hostname.sub('pxy', 'pod-pxy')
  else
    begin
      node = get_target(host)
      system_name = node.full_hostname
    rescue RuntimeError
      # If the node for that host is not defined, just return the host parameter as system_name
      system_name = host
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
$custom_download_endpoint = ENV['CUSTOM_DOWNLOAD_ENDPOINT'] if ENV['CUSTOM_DOWNLOAD_ENDPOINT']
$no_auth_registry = ENV['NO_AUTH_REGISTRY'] if ENV['NO_AUTH_REGISTRY']
$auth_registry = ENV['AUTH_REGISTRY'] if ENV['AUTH_REGISTRY']
if ENV['SCC_CREDENTIALS']
  scc_username, scc_password = ENV['SCC_CREDENTIALS'].split('|')
  $scc_credentials = !scc_username.to_s.empty? && !scc_password.to_s.empty?
end
$node_by_host = { 'localhost'                 => $localhost,
                  'server'                    => $server,
                  'proxy'                     => $proxy,
                  'containerized_proxy'       => $proxy,
                  'sle_minion'                => $minion,
                  'ssh_minion'                => $ssh_minion,
                  'rhlike_minion'             => $rhlike_minion,
                  'deblike_minion'            => $deblike_minion,
                  'build_host'                => $build_host,
                  'kvm_server'                => $kvm_server,
                  'sle12sp4_minion'           => $sle12sp4_minion,
                  'sle12sp4_ssh_minion'       => $sle12sp4_ssh_minion,
                  'sle12sp5_minion'           => $sle12sp5_minion,
                  'sle12sp5_ssh_minion'       => $sle12sp5_ssh_minion,
                  'sle15sp1_minion'           => $sle15sp1_minion,
                  'sle15sp1_ssh_minion'       => $sle15sp1_ssh_minion,
                  'sle15sp2_minion'           => $sle15sp2_minion,
                  'sle15sp2_ssh_minion'       => $sle15sp2_ssh_minion,
                  'sle15sp3_minion'           => $sle15sp3_minion,
                  'sle15sp3_ssh_minion'       => $sle15sp3_ssh_minion,
                  'sle15sp4_minion'           => $sle15sp4_minion,
                  'sle15sp4_ssh_minion'       => $sle15sp4_ssh_minion,
                  'sle15sp5_minion'           => $sle15sp5_minion,
                  'sle15sp5_ssh_minion'       => $sle15sp5_ssh_minion,
                  'slemicro51_minion'         => $slemicro51_minion,
                  'slemicro51_ssh_minion'     => $slemicro51_ssh_minion,
                  'slemicro52_minion'         => $slemicro52_minion,
                  'slemicro52_ssh_minion'     => $slemicro52_ssh_minion,
                  'slemicro53_minion'         => $slemicro53_minion,
                  'slemicro53_ssh_minion'     => $slemicro53_ssh_minion,
                  'slemicro54_minion'         => $slemicro54_minion,
                  'slemicro54_ssh_minion'     => $slemicro54_ssh_minion,
                  'alma9_minion'              => $alma9_minion,
                  'alma9_ssh_minion'          => $alma9_ssh_minion,
                  'centos7_minion'            => $centos7_minion,
                  'centos7_ssh_minion'        => $centos7_ssh_minion,
                  'liberty9_minion'           => $liberty9_minion,
                  'liberty9_ssh_minion'       => $liberty9_ssh_minion,
                  'oracle9_minion'            => $oracle9_minion,
                  'oracle9_ssh_minion'        => $oracle9_ssh_minion,
                  'rhel9_minion'              => $rhel9_minion,
                  'rhel9_ssh_minion'          => $rhel9_ssh_minion,
                  'rocky8_minion'             => $rocky8_minion,
                  'rocky8_ssh_minion'         => $rocky8_ssh_minion,
                  'rocky9_minion'             => $rocky9_minion,
                  'rocky9_ssh_minion'         => $rocky9_ssh_minion,
                  'ubuntu1804_minion'         => $ubuntu1804_minion,
                  'ubuntu1804_ssh_minion'     => $ubuntu1804_ssh_minion,
                  'ubuntu2004_minion'         => $ubuntu2004_minion,
                  'ubuntu2004_ssh_minion'     => $ubuntu2004_ssh_minion,
                  'ubuntu2204_minion'         => $ubuntu2204_minion,
                  'ubuntu2204_ssh_minion'     => $ubuntu2204_ssh_minion,
                  'debian10_minion'           => $debian10_minion,
                  'debian10_ssh_minion'       => $debian10_ssh_minion,
                  'debian11_minion'           => $debian11_minion,
                  'debian11_ssh_minion'       => $debian11_ssh_minion,
                  'opensuse154arm_minion'     => $opensuse154arm_minion,
                  'opensuse154arm_ssh_minion' => $opensuse154arm_ssh_minion,
                  'opensuse155arm_minion'     => $opensuse155arm_minion,
                  'opensuse155arm_ssh_minion' => $opensuse155arm_ssh_minion,
                  'sle12sp5_buildhost'        => $sle12sp5_buildhost,
                  'sle15sp4_buildhost'        => $sle15sp4_buildhost,
                  'monitoring_server'         => $monitoring_server,
                  'salt_migration_minion'     => $salt_migration_minion }

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

  %w[br0 eth0 eth1 ens0 ens1 ens2 ens3 ens4 ens5 ens6].each do |dev|
    output, code = node.run("ip address show dev #{dev} | grep 'inet '", check_errors: false)
    next unless code.zero?

    node.init_public_interface(dev)
    return '' if output.empty?
    return output.split[1].split('/')[0]
  end
  raise "Cannot resolve public ip of #{host}"
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
  next if ip.empty?
  node.init_public_ip ip
end
