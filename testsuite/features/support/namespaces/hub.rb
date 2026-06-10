# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

# Hub namespace
class NamespaceHub
  # Initializes the Hub XMLRPC client
  #
  # @param hub_host [String] The hostname of the Hub server
  def initialize(hub_host)
    @hub_host = hub_host
    @client = nil
    @session_key = nil
    @server_ids = []
  end

  attr_reader :session_key, :server_ids

  # Authenticates with the Hub using auto-connect mode
  #
  # @param username [String] Admin username
  # @param password [String] Admin password
  # @return [Hash] Login response containing SessionKey
  def login_with_autoconnect(username, password)
    protocol = $debug_mode ? 'http://' : 'https://'
    @client = XMLRPC::Client.new2("#{protocol}#{@hub_host}/hub/rpc/api", nil, DEFAULT_TIMEOUT)
    response = @client.call('hub.loginWithAutoconnectMode', username, password)
    @session_key = response['SessionKey']
    response
  rescue XMLRPC::FaultException => e
    raise SystemCallError, "Hub API login failed: #{e.message}"
  end

  # Lists all server IDs registered with the Hub
  #
  # @return [Array<Integer>] List of server IDs
  def list_server_ids
    @server_ids = @client.call('hub.listServerIds', @session_key)
  rescue XMLRPC::FaultException => e
    raise SystemCallError, "Hub API listServerIds failed: #{e.message}"
  end

  # Calls multicast system.list_systems across all registered servers
  #
  # @return [Hash] Response with Successful and Failed keys
  def multicast_system_list
    @client.call('multicast.system.list_systems', @session_key, @server_ids)
  rescue XMLRPC::FaultException => e
    raise SystemCallError, "Hub API multicast call failed: #{e.message}"
  end

  # Logs out from the Hub API
  def logout
    @client.call('hub.logout', @session_key)
    @session_key = nil
  rescue XMLRPC::FaultException => e
    raise SystemCallError, "Hub API logout failed: #{e.message}"
  end
end
