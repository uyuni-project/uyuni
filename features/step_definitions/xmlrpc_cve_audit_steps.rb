rpctest = nil 
result_list = []
result = nil

Given /^I am logged in via XML\-RPC\/cve audit as user "([^"]*)" and password "([^"]*)"$/ do |luser, password|
  rpctest = XMLRPCCVEAuditTest.new(ENV["TESTHOST"])
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

Then /^I should get the sles11-sp3-updates channel$/ do
  arch = `uname -m`
  arch.chomp!
  if arch != "x86_64"
    channel = "sles11-sp3-updates-i586-channel"
  else
    channel = "sles11-sp3-updates-x86_64-channel"
  end
  $stderr.puts "result: #{result}"
  fail if result["channel_labels"].include?(channel) == false
end

Then /^I should get the "([^"]*)" patch$/ do |patch|
  $stderr.puts "result: #{result}"
  fail if result["errata_advisories"].include?(patch) == false
end

Then /^I logout from XML\-RPC\/cve audit namespace\.$/ do
  fail if not rpctest.logout()
end
