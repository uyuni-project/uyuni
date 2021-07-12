# Copyright (c) 2011-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'

# Base API class
class XMLRPCBaseTest
  # Initialize the base class establishing a connection
  def initialize(host)
    @host = host
    # new2(uri, proxy=nil, timeout=nil)
    @connection = XMLRPC::Client.new2("http://#{@host}/rpc/api", nil, DEFAULT_TIMEOUT)
  end

  # Authenticate against the host
  def login(luser, password)
    @sid = @connection.call('auth.login', luser, password)
  end

  # Logout from the host
  def logout
    @connection.call('auth.logout', @sid)
  end
end
