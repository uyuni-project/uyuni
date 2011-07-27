rpcApiTester = XMLRPCApiTest.new(ENV["TESTHOST"])

#
# Steps
#
Given /^I am logged in via XML\-RPC\/api as user "([^"]*)" and password "([^"]*)"$/ do |luser, password|
  fail if not rpcApiTester.login(luser, password)
end

When /^I call getApiNamespaces, I should get (\d+) namespaces$/ do |apiCount|
  fail if apiCount.to_i != rpcApiTester.getCountOfApiNamespaces()
end

When /^I call getVersion, I should get "([^"]*)" as result$/ do |version|
  fail if not rpcApiTester.getVersion() == version
end

When /^I call systemVersion, I should get "([^"]*)" as result$/ do |version|
  fail if not rpcApiTester.systemVersion() == version
end

When /^I call getApiNamespaceCallList, I should get (\d+) available api calls$/ do |apiCount|
  fail if apiCount.to_i != rpcApiTester.getCountOfApiNamespaceCallList()
end

When /^I call getApiCallList, I should get (\d+) available groups$/ do |groupCount|
  fail if groupCount.to_i != rpcApiTester.getCountOfApiCallListGroups()
end
