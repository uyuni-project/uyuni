# COPYRIGHT 2015 SUSE LLC
Given /^I am logged in via XML\-RPC\/cve audit as user "([^"]*)" and password "([^"]*)"$/ do |luser, password|
  @rpctest = XMLRPCCVEAuditTest.new(ENV["TESTHOST"])
  @rpctest.login(luser, password)
end

Given /^channel data has already been updated$/ do
  assert_equals(@rpctest.populateCVEServerChannels(), 1)
end

When /^I call audit.listSystemsByPatchStatus with CVE identifier "([^\"]*)"$/ do |cve_identifier|
  @result_list = @rpctest.listSystemsByPatchStatus(cve_identifier) || []
end

Then /^I should get status "([^\"]+)" for system "([0-9]+)"$/ do |status, system|
  @result = @result_list.select {|item| item['system_id'] == system.to_i}
  refute_empty(@result)
  @result = @result[0]
  assert_equal(status, @result['patch_status'])
end

Then /^I should get status "([^\"]+)" for this client$/ do |status|
  step "I should get status \"#{status}\" for system \"#{client_system_id_to_i}\""
end

Then /^I should get the sles11-sp3-updates channel$/ do
  arch = `uname -m`
  arch.chomp!
  if arch != "x86_64"
    channel = "sles11-sp3-updates-i586-channel"
  else
    channel = "sles11-sp3-updates-x86_64-channel"
  end
  $stderr.puts "result: #{@result}"
  assert(@result["channel_labels"].include?(channel))
end

Then /^I should get the "([^"]*)" patch$/ do |patch|
  $stderr.puts "result: #{@result}"
  assert(@result["errata_advisories"].include?(patch))
end

Then /^I logout from XML\-RPC\/cve audit namespace\.$/ do
  assert(@rpctest.logout())
end
