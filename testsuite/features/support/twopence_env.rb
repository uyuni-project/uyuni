# Copyright (c) 2016-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'require_all'
require 'twopence'
require_relative 'twopence_init'

# Raise a warning if any of these environment variables is missing
raise 'Server IP address or domain name variable empty' if ENV['SERVER'].nil?

warn 'Proxy IP address or domain name variable empty' if ENV['PROXY'].nil?
unless $build_validation
  warn 'Traditional client IP address or domain name variable empty' if ENV['CLIENT'].nil?
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

# Dictionaries to obtain host or node from the Twopence objects
$node_by_host = {}
$host_by_node = {}

# Preserve FQDN before initialization
$named_nodes = {}

# Define SCC credentials through the environment variable
if ENV['SCC_CREDENTIALS']
  scc_username, scc_password = ENV['SCC_CREDENTIALS'].split('|')
  $scc_credentials = !scc_username.to_s.empty? && !scc_password.to_s.empty?
end

# Get the Twopence node passing the host (includes lazy initialization)
def get_target(host, refresh: false)
  node = $node_by_host[host]
  node = twopence_init(host) if node.nil? || refresh
  node
end
