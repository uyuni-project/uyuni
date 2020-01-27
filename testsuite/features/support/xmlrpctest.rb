# Copyright (c) 2011-2017 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'

def retrieve_server_id(server)
  sysrpc = XMLRPCSystemTest.new(ENV['SERVER'])
  sysrpc.login('admin', 'admin')
  systems = sysrpc.list_systems
  refute_nil(systems)
  server_id = systems
              .select { |s| s['name'] == server }
              .map { |s| s['id'] }.first
  refute_nil(server_id, "client #{server} is not yet registered?")
  server_id
end

# base class from where other inherit
class XMLRPCBaseTest
  def initialize(host)
    @host = host
    # new2(uri, proxy=nil, timeout=nil)
    @connection = XMLRPC::Client.new2('http://' + @host + '/rpc/api', nil, DEFAULT_TIMEOUT)
  end

  #
  # Authenticate against the $HOST
  #
  def login(luser, password)
    @sid = @connection.call('auth.login', luser, password)
  end

  def logout
    @connection.call('auth.logout', @sid)
  end
end
