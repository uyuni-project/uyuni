# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "api" namespace
class NamespaceApi
  def initialize(api_test)
    @test = api_test
  end

  def get_version
    @test.call('api.getVersion')
  end

  def system_version
    @test.call('api.systemVersion')
  end

  def get_count_of_api_namespaces
    namespaces = @test.call('api.getApiNamespaces', sessionKey: @test.token)
    namespaces.nil? ? 0 : namespaces.length
  end

  # list all available api calls grouped by namespace
  def get_count_of_api_call_list_groups
    call_list = @test.call('api.getApiCallList', sessionKey: @test.token)
    call_list.nil? ? 0 : call_list.length
  end

  def get_count_of_api_namespace_call_list
    count = 0
    namespaces = @test.call('api.getApiNamespaces', sessionKey: @test.token)
    namespaces.each do |ns|
      call_list = @test.call('api.getApiNamespaceCallList', sessionKey: @test.token, namespace: ns[0])
      count += call_list.length unless call_list.nil?
    end
    count
  end
end
