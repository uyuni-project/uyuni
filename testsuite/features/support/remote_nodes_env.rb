# Copyright (c) 2016-2023 SUSE LLC.
# SPDX-License-Identifier: MIT

require 'require_all'
require_relative 'remote_node'

# QAM and Build Validation pipelines will provide a json file including all custom (MI) repositories
custom_repos_path = "#{File.dirname(__FILE__)}/../upload_files/custom_repositories.json"

# Raise a warning if any of these environment variables is missing
raise ArgumentError, 'Server IP address or domain name variable empty' if ENV['SERVER'].nil?

warn 'Proxy IP address or domain name variable empty' if ENV['PROXY'].nil?
unless File.exist?(custom_repos_path)
  warn 'Minion IP address or domain name variable empty' if ENV['MINION'].nil?
  warn 'Buildhost IP address or domain name variable empty' if ENV['BUILD_HOST'].nil?
  warn 'Red Hat-like minion IP address or domain name variable empty' if ENV['RHLIKE_MINION'].nil?
  warn 'Debian-like minion IP address or domain name variable empty' if ENV['DEBLIKE_MINION'].nil?
  warn 'SSH minion IP address or domain name variable empty' if ENV['SSH_MINION'].nil?
  warn 'PXE boot MAC address variable empty' if ENV['PXEBOOT_MAC'].nil?
end

# Dictionaries to obtain host or node from the RemoteNode objects
$node_by_host = {}
$host_by_node = {}

# Preserve FQDN before initialization
$named_nodes = {}

# Define SCC credentials through the environment variable
if ENV['SCC_CREDENTIALS']
  scc_username, scc_password = ENV['SCC_CREDENTIALS'].split('|')
  $scc_credentials = !scc_username.to_s.empty? && !scc_password.to_s.empty?
end

# Get the RemoteNode passing the host (includes lazy initialization)
def get_target(host, refresh: false)
  node = $node_by_host[host]
  node = RemoteNode.new(host) if node.nil? || refresh == true
  node
end
