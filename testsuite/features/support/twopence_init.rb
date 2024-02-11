# Copyright (c) 2016-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'twopence'
require_relative 'lavanda'

# Retrieve and set OS Family and Version of a node
def process_os_family_and_version(host, fqdn, hostname, node)
  $stdout.puts "Host '#{host}' is alive with determined hostname #{hostname.strip} and FQDN #{fqdn.strip}" unless $build_validation
  os_version, os_family = get_os_version(node)
  node.init_os_family(os_family)
  node.init_os_version(os_version)
  node
end

# Obtain the Public IP for a node
def client_public_ip(node)
  raise NotImplementedError, "Cannot resolve node for host '#{host}'" if node.nil?

  %w[br0 eth0 eth1 ens0 ens1 ens2 ens3 ens4 ens5 ens6].each do |dev|
    output, code = node.run_local("ip address show dev #{dev} | grep 'inet '", check_errors: false)
    next unless code.zero?

    node.init_public_interface(dev)
    return '' if output.empty?

    return output.split[1].split('/')[0]
  end
  raise ArgumentError, "Cannot resolve public ip of #{host}"
end

# Retrieve and set private and public IPs of a node
def process_private_and_public_ip(host, node)
  if (ADDRESSES.key? host) && !$private_net.nil?
    node.init_private_ip(net_prefix + ADDRESSES[host])
    node.init_private_interface('eth1')
  end

  ip = client_public_ip node
  node.init_public_ip(ip) unless ip.empty?
  node
end

def initialize_server(host, node)
  # Remove /etc/motd, or any output from node.run will contain the content of /etc/motd
  node.run('rm -f /etc/motd && touch /etc/motd')
  _out, code = node.run('which mgrctl', check_errors: false)
  node.init_has_mgrctl if code.zero?

  fqdn, code = node.run('sed -n \'s/^java.hostname *= *\(.\+\)$/\1/p\' /etc/rhn/rhn.conf')
  raise StandardError, "Cannot connect to get FQDN for '#{$named_nodes[node.hash]}'. Response code: #{code}, local: #{local}, remote: #{remote}" if code.nonzero?
  raise StandardError, "No FQDN for '#{$named_nodes[node.hash]}'. Response code: #{code}" if fqdn.empty?

  node.init_full_hostname(fqdn)
  node.init_hostname(fqdn.split('.')[0])

  node = process_os_family_and_version(host, fqdn, node.hostname, node)
  node = process_private_and_public_ip(host, node)

  $node_by_host[host] = node
  $host_by_node[node] = host
  node
end

# Initialize a Twopence node through its host (additionally it will setup some handy maps)
def twopence_init(host)
  puts "Initializing a twopence node for '#{host}'."
  raise(NotImplementedError, "Host #{host} is not defined as a valid host in the Test Framework.") unless ENV_VAR_BY_HOST.key? host

  unless ENV.key? ENV_VAR_BY_HOST[host]
    warn "Host #{host} is not defined as environment variable."
    return
  end

  target = "ssh:#{ENV.fetch(ENV_VAR_BY_HOST[host], nil)}"
  node = Twopence.init(target)
  raise LoadError, "Twopence node #{host} initialization has failed." if node.nil?

  $named_nodes[node.hash] = target.split(':')[1]

  # Lavanda library module extension
  # Look at support/lavanda.rb for more details
  node.extend(LavandaBasic)

  return initialize_server(host, node) if host == 'server'

  # Initialize hostname
  hostname, local, remote, code = node.test_and_store_results_together('hostname', 'root', 500)

  # special handling for nested VMs since they will only be created later in the test suite
  # we to a late hostname initialization in a special step for those
  unless hostname.empty? || host == 'salt_migration_minion'
    raise StandardError, "Cannot connect to get hostname for '#{$named_nodes[node.hash]}'. Response code: #{code}, local: #{local}, remote: #{remote}" if code.nonzero? || remote.nonzero? || local.nonzero?
    raise StandardError, "No hostname for '#{$named_nodes[node.hash]}'. Response code: #{code}" if hostname.empty?

    node.init_hostname(hostname)

    fqdn, local, remote, code = node.test_and_store_results_together('hostname -f', 'root', 500)
    raise StandardError, "Cannot connect to get FQDN for '#{$named_nodes[node.hash]}'. Response code: #{code}, local: #{local}, remote: #{remote}" if code.nonzero? || remote.nonzero? || local.nonzero?
    raise StandardError, "No FQDN for '#{$named_nodes[node.hash]}'. Response code: #{code}" if fqdn.empty?

    node.init_full_hostname(fqdn)

    node = process_os_family_and_version(host, fqdn, hostname, node)
    node = process_private_and_public_ip(host, node)
  end

  $node_by_host[host] = node
  $host_by_node[node] = host
  node
end
