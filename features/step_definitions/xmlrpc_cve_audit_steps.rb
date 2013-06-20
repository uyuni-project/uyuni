rpctest = XMLRPCCVEAuditTest.new(ENV["TESTHOST"])
result_list = []
result = nil

Given /^I am logged in via XML\-RPC\/cve audit as user "([^"]*)" and password "([^"]*)"$/ do |luser, password|
  rpctest.login(luser, password)
end

Given /^channel data has already been updated$/ do
  fail if rpctest.populateCVEServerChannels() != 1
end

When /^I call audit.listSystemsByPatchStatus with CVE identifier "([^\"]*)"$/ do |cve_identifier|
  result_list = rpctest.listSystemsByPatchStatus(cve_identifier)
end

Then /^I should get status "([^\"]+)" for system "([0-9]+)"$/ do |status, system|
  result = result_list.select {|result| result["system_id"] == system.to_i}

  fail if result.empty?
  result = result[0]
  fail if result["patch_status"] != status
end

Then /^I should get channel "([0-9]+)"?$/ do |channel|
  fail if result["channel_ids"].include?(channel.to_i) == false
end

Then /^I should get patch "([0-9]+)"?$/ do |patch|
  fail if result["errata_ids"].include?(patch.to_i) == false
end

Then /^I logout from XML\-RPC\/cve audit namespace\.$/ do
  fail if not rpctest.logout()
end
