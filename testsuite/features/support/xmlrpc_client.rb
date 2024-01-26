# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'

# Wrapper class for XML-RPC client library
class XmlrpcClient
  ##
  # It creates an XMLRPC client object that will be used to communicate with the Spacewalk server
  #
  # Args:
  #   host: The hostname of the Spacewalk server.
  def initialize(host)
    puts 'Activating XML-RPC API'
    protocol = $debug_mode ? 'http://' : 'https://'
    @xmlrpc_client = XMLRPC::Client.new2("#{protocol}#{host}/rpc/api", nil, DEFAULT_TIMEOUT)
  end

  ##
  # It calls a remote method with a list of parameters
  #
  # Args:
  #   name: The name of the method to call.
  #   params: A hash of parameters. The keys are the names of the parameters, and the values are the values of the
  # parameters.
  def call(name, params)
    @xmlrpc_client.call(name, *params.values)
  rescue XMLRPC::FaultException => e
    raise SystemCallError, "API failure: #{e.message}"
  end
end
