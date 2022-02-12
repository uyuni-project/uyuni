# Copyright (c) 2011-2017 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpctest'

# api namespace class
class XMLRPCApiTest < XMLRPCBaseTest
  def get_version
    @connection.call('api.getVersion')
  end

  def system_version
    @connection.call('api.systemVersion')
  end

  def get_count_of_api_namespaces
    namespaces = @connection.call('api.getApiNamespaces', @sid)
    count = 0
    count = namespaces.length unless namespaces.nil?
    count
  end

  #
  # Test lists all available api calls grouped by namespace.
  #
  def get_count_of_api_call_list_groups
    call_list = @connection.call('api.getApiCallList', @sid)
    count = 0
    count = call_list.length unless call_list.nil?
    count
  end

  def get_count_of_api_namespace_call_list
    count = 0
    namespaces = @connection.call('api.getApiNamespaces', @sid)
    STDOUT.puts '    Spaces found: ' + namespaces.length.to_s
    namespaces.each do |ns|
      STDOUT.puts '      Analyzing ' + ns[0] + '... '
      call_list = @connection.call('api.getApiNamespaceCallList', @sid, ns[0])
      if !call_list.nil?
        count += call_list.length
        STDOUT.puts 'Done'
      else
        STDOUT.puts 'Failed'
      end
    end
    count
  end
end
