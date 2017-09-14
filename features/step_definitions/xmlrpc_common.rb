# COPYRIGHT 2015 SUSE LLC
require 'json'
require 'xmlrpc/client'
require 'socket'

rpctest = XMLRPCChannelTest.new(ENV['TESTHOST'])
systest = XMLRPCSystemTest.new(ENV['TESTHOST'])
servers = []
rabbit = nil

Given(/^I am logged in via XML\-RPC\/system as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  systest.login(luser, password)
end

When(/^I call system\.listSystems\(\), I should get a list of them\.$/) do
  # This also assumes the test is called *after* the regular test.
  servers = systest.listSystems
  assert(servers.!empty?, "Expect: 'number of system' > 0, but found only '#{servers.length}' servers")
  rabbit = servers[0]
end

When(/^I call system\.bootstrap\(\) on host "(.*?)" and saltSSH "(.*?)", \
a new system should be bootstraped\.$/) do |host, salt_ssh_enabled|
  salt_ssh = (salt_ssh_enabled == 'enabled')
  node = get_target(host)
  result = systest.bootstrapSystem(node.full_hostname, '', salt_ssh)
  assert(result == 1, 'Bootstrap return code not equal to 1.')
end

When(/^I call system\.bootstrap\(\) on unknown host, I should get an XMLRPC fault with code -1.$/) do
  exception_thrown = false
  begin
    systest.bootstrapSystem('imprettysureidontexist', '', false)
  rescue XMLRPC::FaultException => fault
    exception_thrown = true
    assert(fault.faultCode == -1, 'Fault code must be == -1.')
  end
  assert(exception_thrown, 'Exception must be thrown for non-existing host.')
end

When(/^I call system\.bootstrap\(\) on a salt minion with saltSSH = true, \
but with activation key with Default contact method, I should get an XMLRPC fault with code -1\.$/) do
  exception_thrown = false
  begin
    systest.bootstrapSystem($minion.full_hostname, '1-SUSE-DEV-x86_64', true)
  rescue XMLRPC::FaultException => fault
    exception_thrown = true
    assert(fault.faultCode == -1, 'Fault code must be == -1.')
  end
  assert(exception_thrown, 'Exception must be thrown for non-compatible activation keys.')
end

When(/^I call system\.createSystemRecord\(\) with sys_name "([^"]*)", ks_label "([^"]*)", ip "([^"]*)", mac "([^"]*)"$/) do |sys_name, ks_lab, ip, mac|
  systest.createSystemRecord(sys_name, ks_lab, ip, mac)
end

Then(/^I logout from XML\-RPC\/system\.$/) do
  systest.logout
end

# xmlrpc_user tests
CREATE_USER_PASSWORD = 'die gurke'.freeze

Given(/^I am logged in via XML\-RPC\/user as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @rpc = XMLRPCUserTest.new(ENV['TESTHOST'])
  @rpc.login(luser, password)
end

When(/^I call user\.listUsers\(\)$/) do
  @users = @rpc.getUsers
end

Then(/^I should get at least user "([^"]*)"$/) do |luser|
  assert_includes(@users.map { |u| u['login'] }, luser)
end

When(/^I call user\.getDetails\(\) on user "([^"]*)"$/) do |luser|
  @roles = @rpc.getUserRoles(luser)
end

Then(/^I should see at least one role that matches "([^"]*)" suffix$/) do |sfx|
  refute(@roles.find_all { |el| el =~ /#{sfx}/ }.empty?)
end

When(/^I call user\.create\(sid, login, pwd, name, lastname, email\) with login "([^"]*)"$/) do |luser|
  refute(@rpc.createUser(luser, CREATE_USER_PASSWORD) != 1)
end

Then(/^when I call user\.listUsers\(\), I should see a user "([^"]*)"$/) do |luser|
  steps %{
    When I call user.listUsers()
    Then I should get at least user "#{luser}"
  }
end

When(/^I call user\.addRole\(\) on "([^"]*)" with the role "([^"]*)"$/) do |luser, role|
  refute(@rpc.addRole(luser, role) != 1)
end

Then(/^I should see "([^"]*)" when I call user\.listRoles\(\) with "([^"]*)"$/) do |rolename, luser|
  assert_includes(@rpc.getUserRoles(luser), rolename)
end

Then(/^I logout from XML\-RPC\/user namespace\.$/) do
  assert(@rpc.logout)
end

When(/^I delete user "([^"]*)"$/) do |luser|
  @rpc.deleteUser(luser)
end

Given(/^I make sure "([^"]*)" is not present$/) do |luser|
  @rpc.getUsers
      .map { |u| u['login'] }
      .select { |l| l == luser }
      .each { @rpc.deleteUser(luser) }
end

When(/^I call user\.removeRole\(\) against uid "([^"]*)" with the role "([^"]*)"$/) do |luser, rolename|
  refute(@rpc.delRole(luser, rolename) != 1)
end

Then(/^I shall not see "([^"]*)" when I call user\.listRoles\(\) with "([^"]*)" uid$/) do |rolename, luser|
  refute_includes(@rpc.getUserRoles(luser), rolename)
end

Given(/^I am logged in via XML\-RPC\/channel as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  assert(rpctest.login(luser, password))
end

When(/^I create a repo with label "([^"]*)" and url$/) do |label|
  url = "http://#{$server_ip}/pub/AnotherRepo/"
  assert(rpctest.createRepo(label, url))
end

When(/^I associate repo "([^"]*)" with channel "([^"]*)"$/) do |repo_label, channel_label|
  assert(rpctest.associateRepo(channel_label, repo_label))
end

When(/^I create the following channels:/) do |table|
  channels = table.hashes
  channels.each do |ch|
    assert_equal(
      rpctest.create(
        ch['LABEL'], ch['NAME'], ch['SUMMARY'], ch['ARCH'], ch['PARENT']
      ), 1
    )
  end
end

When(/^I delete the software channel with label "([^"]*)"$/) do |label|
  assert_equal(rpctest.delete(label), 1)
end

When(/^I delete the repo with label "([^"]*)"$/) do |label|
  assert_equal(rpctest.deleteRepo(label), 1)
end

Then(/^something should get listed with a call of listSoftwareChannels$/) do
  assert_equal(rpctest.getSoftwareChannelsCount < 1, false)
end

Then(/^"([^"]*)" should get listed with a call of listSoftwareChannels$/) do |label|
  assert(rpctest.verifyChannel(label))
end

Then(/^"([^"]*)" should not get listed with a call of listSoftwareChannels$/) do |label|
  assert_equal(rpctest.verifyChannel(label), false)
end

Then(/^"([^"]*)" should be the parent channel of "([^"]*)"$/) do |parent, child|
  assert(rpctest.isParentChannel(child, parent))
end

Then(/^channel "([^"]*)" should have attribute "([^"]*)" from type "([^"]*)"$/) do |label, attr, type|
  ret = rpctest.getChannelDetails(label)
  assert(ret)
  assert_equal(ret[attr].class.to_s, type)
end

Then(/^channel "([^"]*)" should not have attribute "([^"]*)"$/) do |label, attr|
  ret = rpctest.getChannelDetails(label)
  assert(ret)
  assert_equal(ret.key?(attr), false)
end

# activation key test xmlrpc
acttest = XMLRPCActivationKeyTest.new(ENV['TESTHOST'])
key = nil

Given(/^I am logged in via XML\-RPC\/activationkey as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  raise unless acttest.login(luser, password)
end

When(/^I create an AK with id "([^"]*)", description "([^"]*)" and limit of (\d+)$/) do |id, dscr, limit|
  key = acttest.createKey(id, dscr, limit)
  raise if key.nil?
end

Then(/^I should get it listed with a call of listActivationKeys\.$/) do
  raise unless acttest.verifyKey(key)
end

When(/^I call listActivationKeys I should get some\.$/) do
  raise if acttest.getActivationKeysCount < 1
end

Then(/^I should get key deleted\.$/) do
  raise unless acttest.deleteKey(key)
  raise if acttest.verifyKey(key)
end

When(/^I add config channels "([^"]*)" to a newly created key$/) do |channel_name|
  raise if acttest.addConfigChannel(key, channel_name) < 1
end

# Details
When(/^I call activationkey\.setDetails\(\) to the key$/) do
  raise unless acttest.setDetails(key)
end

Then(/^I have to see them by calling activationkey\.getDetails\(\)$/) do
  raise unless acttest.getDetails(key)
end

virtualhostmanager = XMLRPCVHMTest.new(ENV['TESTHOST'])
modules = []
vhms = []
params = {}
detail = {}

Given(/^I am logged in via XML\-RPC\/virtualhostmanager as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  virtualhostmanager.login(luser, password)
end

When(/^I call virtualhostmanager.listAvailableVirtualHostGathererModules\(\)$/) do
  modules = virtualhostmanager.listAvailableVirtualHostGathererModules
end

When(/^I call virtualhostmanager.listVirtualHostManagers\(\)$/) do
  vhms = virtualhostmanager.listVirtualHostManagers
end

When(/^I call virtualhostmanager.getModuleParameters\(\) for "([^"]*)"$/) do |module_name|
  params = virtualhostmanager.getModuleParameters(module_name)
end

When(/^I call virtualhostmanager.create\("([^"]*)", "([^"]*)"\) and params from "([^"]*)"$/) do |label, module_name, param_file|
  fd = File.read(File.new(param_file))
  p = JSON.parse(fd)
  r = virtualhostmanager.create(label, module_name, p)
  raise if r != 1
end

When(/^I call virtualhostmanager.delete\("([^"]*)"\)$/) do |label|
  r = virtualhostmanager.delete(label)
  raise if r != 1
end

When(/^I call virtualhostmanager.getDetail\("([^"]*)"\)$/) do |label|
  detail = virtualhostmanager.getDetail(label)
end

Then(/^"([^"]*)" should be "([^"]*)"$/) do |key1, value1|
  assert(detail.key?(key1), "Expect parameter key '#{key1}', but got only '#{detail}'")
  assert(detail[key1].to_s == value, "Expect value for #{key1} should be '#{value1}, but got '#{detail[key1]}'")
end

Then(/^configs "([^"]*)" should be "([^"]*)"$/) do |key1, value1|
  assert(detail['configs'].key?(key1), "Expect parameter key '#{key1}', but got only '#{detail['configs']}'")
  assert(detail['configs'][key1].to_s == value1, "Expect value for #{key1} should be '#{value1}, but got '#{detail['configs'][key1]}'")
end

Then(/^I logout from XML\-RPC\/virtualhostmanager$/) do
  virtualhostmanager.logout
end

rpc = XMLRPCActionChain.new(ENV['TESTHOST'])
syschaintest = XMLRPCSystemTest.new(ENV['TESTHOST'])
scdrpc = XMLRPCScheduleTest.new(ENV['TESTHOST'])

# Auth
Given(/^I am logged in via XML\-RPC\/actionchain as user "(.*?)" and password "(.*?)"$/) do |luser, password|
  # Find target server once.
  unless $client_id
    rpc.login(luser, password)
    syschaintest.login(luser, password)
    scdrpc.login(luser, password)

    servers = syschaintest.listSystems
    refute_nil(servers)

    hostname = $client.full_hostname
    $client_id = servers
                 .select { |s| s['name'] == hostname }
                 .map { |s| s['id'] }.first
    refute_nil($client_id, "Client #{hostname} is not yet registered?")
  end

  # Flush all chains
  rpc.listChains.each do |label|
    rpc.deleteChain(label)
  end
end

# Listing chains
When(/^I call XML\-RPC\/createChain with chainLabel "(.*?)"$/) do |label|
  action_id = rpc.createChain(label)
  refute(action_id < 1)
  $chain_label = label
end

When(/^I call actionchain\.listChains\(\) if label "(.*?)" is there$/) do |label|
  assert_includes(rpc.listChains, label)
end

# Deleting chain
Then(/^I delete the action chain$/) do
  begin
    rpc.deleteChain($chain_label)
  rescue XMLRPC::FaultException => e
    raise format('deleteChain: XML-RPC failure, code %s: %s', e.faultCode, e.faultString)
  end
end

Then(/^I delete an action chain, labeled "(.*?)"$/) do |label|
  begin
    rpc.deleteChain(label)
  rescue XMLRPC::FaultException => e
    raise format('deleteChain: XML-RPC failure, code %s: %s', e.faultCode, e.faultString)
  end
end

# Renaming chain
Then(/^I call actionchain\.renameChain\(\) to rename it from "(.*?)" to "(.*?)"$/) do |old_label, new_label|
  rpc.renameChain(old_label, new_label)
end

Then(/^there should be a new action chain with the label "(.*?)"$/) do |label|
  assert_includes(rpc.listChains, label)
end

Then(/^there should be an action chain with the label "(.*?)"$/) do |label|
  assert_includes(rpc.listChains, label)
end

Then(/^there should be no action chain with the label "(.*?)"$/) do |label|
  refute_includes(rpc.listChains, label)
end

Then(/^no action chain with the label "(.*?)"\.$/) do |label|
  refute_includes(rpc.listChains, label)
end

#
# Schedule scenario
#
When(/^I call actionchain\.addScriptRun\(\) with the script like "(.*?)"$/) do |script|
  refute(rpc.addScriptRun($client_id, script, $chain_label) < 1)
end

Then(/^I should be able to see all these actions in the action chain$/) do
  begin
    actions = rpc.listChainActions($chain_label)
    refute_nil(actions)
    puts 'Running actions:'
    actions.each do |action|
      puts "\t- " + action['label']
    end
  rescue XMLRPC::FaultException => e
    raise format('Error listChainActions: XML-RPC failure, code %s: %s', e.faultCode, e.faultString)
  end
end

# Reboot
When(/^I call actionchain\.addSystemReboot\(\)$/) do
  refute(rpc.addSystemReboot($client_id, $chain_label) < 1)
end

# Packages operations
When(/^I call actionchain\.addPackageInstall\(\)$/) do
  pkgs = syschaintest.listAllInstallablePackages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(rpc.addPackageInstall($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

When(/^I call actionchain\.addPackageRemoval\(\)$/) do
  pkgs = syschaintest.listAllInstallablePackages($client_id)
  refute(rpc.addPackageRemoval($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

When(/^I call actionchain\.addPackageUpgrade\(\)$/) do
  pkgs = syschaintest.listLatestUpgradablePackages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(rpc.addPackageUpgrade($client_id, [pkgs[0]['to_package_id']], $chain_label) < 1)
end

When(/^I call actionchain\.addPackageVerify\(\)$/) do
  pkgs = syschaintest.listAllInstallablePackages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(rpc.addPackageVerify($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

# Manage actions within the action chain
When(/^I call actionchain\.removeAction on each action within the chain$/) do
  begin
    actions = rpc.listChainActions($chain_label)
    refute_nil(actions)
    actions.each do |action|
      refute(rpc.removeAction($chain_label, action['id']) < 0)
      puts "\t- Removed \"" + action['label'] + '" action'
    end
  rescue XMLRPC::FaultException => e
    raise format('Error removeAction: XML-RPC failure, code %s: %s', e.faultCode, e.faultString)
  end
end

Then(/^I should be able to see that the current action chain is empty$/) do
  assert_empty(rpc.listChainActions($chain_label))
end

# Scheduling the action chain
When(/^I schedule the action chain$/) do
  refute(rpc.scheduleChain($chain_label, DateTime.now) < 0)
end

Then(/^there should be no more my action chain$/) do
  refute_includes(rpc.listChains, $chain_label)
end

Then(/^I should see scheduled action, called "(.*?)"$/) do |label|
  assert_includes(
    scdrpc.listInProgressActions.map { |a| a['name'] }, label
  )
end

Then(/^I cancel all scheduled actions$/) do
  scdrpc.listInProgressActions.each do |action|
    # One-by-one, this is against single call in the API on purpose.
    scdrpc.cancelActions([action['id']])
    puts "\t- Removed \"" + action['name'] + '" action'
  end
end

Then(/^there should be no more any scheduled actions$/) do
  assert_empty(scdrpc.listInProgressActions)
end
