# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# API namespace
class NamespaceApi
  ##
  # It initializes the api_test variable.
  #
  # Args:
  #   api_test: This is the test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # Returns the amount of API namespaces.
  def get_count_of_api_namespaces
    namespaces = @test.call('api.getApiNamespaces', sessionKey: @test.token)
    namespaces.nil? ? 0 : namespaces.length
  end

  ##
  # Returns the amount of available API calls.
  def get_count_of_api_call_list_groups
    call_list = @test.call('api.getApiCallList', sessionKey: @test.token)
    call_list.nil? ? 0 : call_list.length
  end

  ##
  # It gets the count of the number of API calls in the API namespace call list.
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
