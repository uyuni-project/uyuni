# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'

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

  # Calls unicast system.list_systems for one specific peripheral server ID.
  # Unlike multicast, unicast takes a single server ID (not a list) and returns
  # the underlying system.list_systems response as-is, not a Successful/Failed map.
  #
  # @param server_id [Integer] The server ID to query
  # @return [Array<Hash>] The peripheral's system.list_systems response
  def unicast_system_list(server_id)
    @client.call('unicast.system.list_systems', @session_key, server_id)
  rescue XMLRPC::FaultException => e
    raise SystemCallError, "Hub API unicast call failed: #{e.message}"
  end

  # Returns an XMLRPC client pointed at the hub's own /rpc/api (standard SUMA API, not hub gateway)
  #
  # @return [XMLRPC::Client]
  def direct_api_client
    protocol = $debug_mode ? 'http://' : 'https://'
    XMLRPC::Client.new2("#{protocol}#{@hub_host}/rpc/api", nil, DEFAULT_TIMEOUT)
  end
end
