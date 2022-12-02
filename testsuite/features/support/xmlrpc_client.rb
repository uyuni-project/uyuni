# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'

# Wrapper class for XML-RPC client library
class XmlrpcClient
  def initialize(host)
    puts 'Activating XML-RPC API'
    protocol = $debug_mode ? 'http://' : 'https://'
    @xmlrpc_client = XMLRPC::Client.new2(protocol + host + '/rpc/api', nil, DEFAULT_TIMEOUT)
  end

  def call(name, params)
    # TODO: What happens if params.nil?
    @xmlrpc_client.call(name, *params.values)
  end
end
