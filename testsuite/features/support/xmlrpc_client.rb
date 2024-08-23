# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'xmlrpc/client'

# Represents an XMLRPC client object that is used to communicate with the Spacewalk server.
class XmlrpcClient
  # Initializes a new XmlrpcClient object.
  #
  # @param host [String] The hostname of the Spacewalk server.
  def initialize(host)
    puts 'Activating XML-RPC API'
    protocol = $debug_mode ? 'http://' : 'https://'
    @xmlrpc_client = XMLRPC::Client.new2("#{protocol}#{host}/rpc/api", nil, DEFAULT_TIMEOUT)
  end

  # Calls a remote method with a list of parameters.
  #
  # @param name [String] The name of the method to call.
  # @param params [Hash] A hash of parameters.
  #   The keys are the names of the parameters, and the values are the values of the parameters.
  # @return [Object] The result of the method call.
  # @raise [SystemCallError] If there is an API failure.
  def call(name, params)
    begin
      @xmlrpc_client.call(name, *params.values)
    rescue XMLRPC::FaultException => e
      raise SystemCallError, "API failure: #{e.message}"
    end
  end
end
