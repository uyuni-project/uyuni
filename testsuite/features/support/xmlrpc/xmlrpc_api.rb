# Copyright (c) 2011-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpc_test'

# API Namespace base
class XMLRPCApiTest < XMLRPCBaseTest
  # Get API version
  def version
    @connection.call('api.getVersion')
  end

  # Get System version
  def system_version
    @connection.call('api.systemVersion')
  end

  # Get amount of API namespaces
  def count_of_api_namespaces
    namespaces = @connection.call('api.getApiNamespaces', @sid)
    count = 0
    count = namespaces.length unless namespaces.nil?
    count
  end

  # Test lists all available api calls grouped by namespace.
  def count_of_api_call_list_groups
    call_list = @connection.call('api.getApiCallList', @sid)
    count = 0
    count = call_list.length unless call_list.nil?
    count
  end

  # Get amount of API endpoints
  def count_of_api_namespace_call_list
    count = 0
    namespaces = @connection.call('api.getApiNamespaces', @sid)
    puts("    Spaces found: #{namespaces.length}")
    namespaces.each do |ns|
      print("      Analyzing #{ns[0]}... ")
      call_list = @connection.call('api.getApiNamespaceCallList', @sid, ns[0])
      if call_list.nil?
        puts('Failed')
      else
        count += call_list.length
        puts('Done')
      end
    end
    count
  end
end
