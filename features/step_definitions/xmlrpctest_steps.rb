# COPYRIGHT 2017 SUSE LLC
require "xmlrpc/client"

rpc_api_tester = XMLRPCApiTest.new(ENV["TESTHOST"])
scdrpc = XMLRPCScheduleTest.new(ENV['TESTHOST'])
#
# Steps
#
Given(/^I am logged in via XML\-RPC\/api as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  assert(rpc_api_tester.login(luser, password))
end

When(/^I call getApiNamespaces, I should get (\d+) namespaces$/) do |apiCount|
  assert_equal(apiCount.to_i, rpc_api_tester.getCountOfApiNamespaces)
end

When(/^I call getVersion, I should get "([^"]*)" as result$/) do |version|
  assert(rpc_api_tester.getVersion.include? version)
end

When(/^I call systemVersion, I should get "([^"]*)" as result$/) do |version|
  assert(rpc_api_tester.systemVersion.include? version)
end

When(/^I call getApiNamespaceCallList, I should get (\d+) available api calls$/) do |apiCount|
  assert_equal(apiCount.to_i, rpc_api_tester.getCountOfApiNamespaceCallList)
end

When(/^I call getApiCallList, I should get (\d+) available groups$/) do |groupCount|
  assert_equal(groupCount.to_i, rpc_api_tester.getCountOfApiCallListGroups)
end

Then(/^there should be no failed scheduled actions$/) do
  scdrpc.login('admin', 'admin')
  assert_empty(scdrpc.listFailedActions)
end
# cve audit

Given(/^I am logged in via XML\-RPC\/cve audit as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @rpctest = XMLRPCCVEAuditTest.new(ENV["TESTHOST"])
  @rpctest.login(luser, password)
end

Given(/^channel data has already been updated$/) do
  assert_equals(@rpctest.populateCVEServerChannels, 1)
end

When(/^I call audit.listSystemsByPatchStatus with CVE identifier "([^\"]*)"$/) do |cve_identifier|
  @result_list = @rpctest.listSystemsByPatchStatus(cve_identifier) || []
end

Then(/^I should get status "([^\"]+)" for system "([0-9]+)"$/) do |status, system|
  @result = @result_list.select { |item| item['system_id'] == system.to_i }
  refute_empty(@result)
  @result = @result[0]
  assert_equal(status, @result['patch_status'])
end

Then(/^I should get status "([^\"]+)" for this client$/) do |status|
  step "I should get status \"#{status}\" for system \"#{client_system_id_to_i}\""
end

Then(/^I should get the test-channel$/) do
  arch = `uname -m`
  arch.chomp!
  channel = if arch != "x86_64"
              "test-channel-i586"
            else
              "test-channel-x86_64"
            end
  $stderr.puts "result: #{@result}"
  assert(@result["channel_labels"].include?(channel))
end

Then(/^I should get the "([^"]*)" patch$/) do |patch|
  $stderr.puts "result: #{@result}"
  assert(@result["errata_advisories"].include?(patch))
end

Then(/^I logout from XML\-RPC\/cve audit namespace\.$/) do
  assert(@rpctest.logout)
end
