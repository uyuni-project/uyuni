systest = XMLRPCSystemTest.new(ENV['TESTHOST'])
servers = []
rabbit = nil


Given /^I am logged in via XML\-RPC\/system as user "([^"]*)" and password "([^"]*)"$/ do |luser, password|
  systest.login(luser, password)
end

When /^I call system\.listSystems\(\), I should get a list of them\.$/ do
  # This also assumes the test is called *after* the regular test.
  servers = systest.listSystems()
  fail if servers.length < 1
  rabbit = servers[0]
end

When /^I check a sysinfo by a number of XML\-RPC calls, it just works\. :\-\)$/ do
  fail if (rabbit == nil or !systest.getSysInfo(rabbit))
end

Then /^I logout from XML\-RPC\/system\.$/ do
  systest.logout()
end
