# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.
#
# Wrapper for XML-RPC library

require 'xmlrpc/client'

class XmlrpcClient
  def initialize(host)
    @xmlrpc_client = XMLRPC::Client.new2('https://' + host + '/rpc/api', nil, DEFAULT_TIMEOUT)
  end

  def call(name, params)
    @xmlrpc_client.call(name, *params.values)
  end
end
