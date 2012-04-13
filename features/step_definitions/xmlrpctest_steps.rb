rpcApiTester = XMLRPCApiTest.new(ENV["TESTHOST"])

#
# Steps
#
Given /^I am logged in via XML\-RPC\/api as user "([^"]*)" and password "([^"]*)"$/ do |luser, password|
  fail if not rpcApiTester.login(luser, password)
end

When /^I call getApiNamespaces, I should get (\d+) namespaces$/ do |apiCount|
  #$stderr.puts "namespaces #{rpcApiTester.getCountOfApiNamespaces()}"
  fail if apiCount.to_i != rpcApiTester.getCountOfApiNamespaces()
end

When /^I call getVersion, I should get "([^"]*)" as result$/ do |version|
  fail if not rpcApiTester.getVersion() == version
end

When /^I call systemVersion, I should get "([^"]*)" as result$/ do |version|
  fail if not rpcApiTester.systemVersion() == version
end

When /^I call getApiNamespaceCallList, I should get (\d+) available api calls$/ do |apiCount|
  #$stderr.puts "API count #{rpcApiTester.getCountOfApiNamespaceCallList()}"
  fail if apiCount.to_i != rpcApiTester.getCountOfApiNamespaceCallList()
end

When /^I call getApiCallList, I should get (\d+) available groups$/ do |groupCount|
  #$stderr.puts "available groups: #{rpcApiTester.getCountOfApiCallListGroups()}"
  fail if groupCount.to_i != rpcApiTester.getCountOfApiCallListGroups()
end
