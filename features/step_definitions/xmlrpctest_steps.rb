# COPYRIGHT 2015 SUSE LLC

rpcApiTester = XMLRPCApiTest.new(ENV["TESTHOST"])

#
# Steps
#
Given /^I am logged in via XML\-RPC\/api as user "([^"]*)" and password "([^"]*)"$/ do |luser, password|
  assert(rpcApiTester.login(luser, password))
end

When /^I call getApiNamespaces, I should get (\d+) namespaces$/ do |apiCount|
  assert_equal(apiCount.to_i, rpcApiTester.getCountOfApiNamespaces())
end

When /^I call getVersion, I should get "([^"]*)" as result$/ do |version|
  fail if not rpcApiTester.getVersion().include? version
end

When /^I call systemVersion, I should get "([^"]*)" as result$/ do |version|
  assert(rpcApiTester.systemVersion().include? version)
end

When /^I call getApiNamespaceCallList, I should get (\d+) available api calls$/ do |apiCount|
  assert_equal(apiCount.to_i, rpcApiTester.getCountOfApiNamespaceCallList())
end

When /^I call getApiCallList, I should get (\d+) available groups$/ do |groupCount|
  assert_equal(groupCount.to_i, rpcApiTester.getCountOfApiCallListGroups())
end
