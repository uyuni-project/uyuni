# COPYRIGHT 2015 SUSE LLC

rpc_api_tester = XMLRPCApiTest.new(ENV["TESTHOST"])

#
# Steps
#
Given(/^I am logged in via XML\-RPC\/api as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  assert(rpc_api_tester.login(luser, password))
end

When(/^I call getApiNamespaces, I should get (\d+) namespaces$/) do |apiCount|
  assert_equal(apiCount.to_i, rpc_api_tester.getCountOfApiNamespaces())
end

When(/^I call getVersion, I should get "([^"]*)" as result$/) do |version|
  assert(rpc_api_tester.getVersion().include? version)
end

When(/^I call systemVersion, I should get "([^"]*)" as result$/) do |version|
  assert(rpc_api_tester.systemVersion().include? version)
end

When(/^I call getApiNamespaceCallList, I should get (\d+) available api calls$/) do |apiCount|
  assert_equal(apiCount.to_i, rpc_api_tester.getCountOfApiNamespaceCallList())
end

When(/^I call getApiCallList, I should get (\d+) available groups$/) do |groupCount|
  assert_equal(groupCount.to_i, rpc_api_tester.getCountOfApiCallListGroups())
end
