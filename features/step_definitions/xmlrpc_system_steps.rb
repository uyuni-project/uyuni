# COPYRIGHT 2015 SUSE LLC

systest = XMLRPCSystemTest.new(ENV['TESTHOST'])
servers = []
rabbit = nil

Given /^I am logged in via XML\-RPC\/system as user "([^"]*)" and password "([^"]*)"$/ do |luser, password|
  systest.login(luser, password)
end

When /^I call system\.listSystems\(\), I should get a list of them\.$/ do
  # This also assumes the test is called *after* the regular test.
  servers = systest.listSystems
  assert(servers.length > 0, "Expect: 'number of system' > 0, but found only '#{servers.length}' servers" )
  rabbit = servers[0]
end

When /^I check a sysinfo by a number of XML\-RPC calls, it just works\. :\-\)$/ do
  assert(rabbit)
  systest.getSysInfo(rabbit)
end

When /^I call system\.createSystemRecord\(\) with sysName "([^"]*)", ksLabel "([^"]*)", ip "([^"]*)", mac "([^"]*)"$/ do |sysName, ksLabel, ip, mac|
  systest.createSystemRecord(sysName, ksLabel, ip, mac)
end

Then /^there is a system record in cobbler named "([^"]*)"$/ do |sysName|
  ct = CobblerTest.new
  assert(ct.is_running, msg: 'cobblerd is not running')
  assert(ct.system_exists(sysName), msg: 'cobbler system record does not exist: ' + sysName)
end

Then /^I logout from XML\-RPC\/system\.$/ do
  systest.logout
end
