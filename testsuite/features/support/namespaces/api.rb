# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# API namespace
class NamespaceApi
  # Initializes a new instance of the NamespaceApi class.
  #
  # @param api_test [Object] The test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  # Returns the amount of API namespaces.
  #
  # @return [Integer] The count of API namespaces.
  def get_count_of_api_namespaces
    namespaces = @test.call('api.getApiNamespaces', sessionKey: @test.token)
    namespaces.nil? ? 0 : namespaces.length
  end

  # Returns the amount of available API calls.
  #
  # @return [Integer] The count of API calls.
  def get_count_of_api_call_list_groups
    call_list = @test.call('api.getApiCallList', sessionKey: @test.token)
    call_list.nil? ? 0 : call_list.length
  end

  # Returns the count of the number of API calls in the API namespace call list.
  #
  # @return [Integer] The count of API calls in the API namespace call list.
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
