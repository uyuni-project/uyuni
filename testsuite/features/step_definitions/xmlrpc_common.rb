# Copyright 2015-2019 SUSE LLC
# Licensed under the terms of the MIT license.

require 'json'
require 'xmlrpc/client'
require 'socket'

rpctest = XMLRPCChannelTest.new($proxy ? $proxy.ip : $server.ip)
systest = XMLRPCSystemTest.new($proxy ? $proxy.ip : $server.ip)

# system namespace

Given(/^I am logged in via XML\-RPC system as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  systest.login(luser, password)
end

Given(/^I am logged in via XML\-RPC system with the feature's user$/) do
  systest.login($username, $password)
end

When(/^I call system\.list_systems\(\), I should get a list of them$/) do
  # This also assumes the test is called *after* the regular test.
  servers = systest.list_systems
  assert(servers.!empty?, "Expect: 'number of system' > 0, but found only '#{servers.length}' servers")
end

When(/^I call system\.bootstrap\(\) on host "([^"]*)" and salt\-ssh "([^"]*)"$/) do |host, salt_ssh_enabled|
  system_name = get_system_name(host)
  salt_ssh = (salt_ssh_enabled == 'enabled')
  akey = salt_ssh ? '1-SUSE-SSH-DEV-x86_64' : '1-SUSE-DEV-x86_64'
  result = systest.bootstrap_system(system_name, akey, salt_ssh)
  assert(result == 1, 'Bootstrap return code not equal to 1.')
end

When(/^I call system\.bootstrap\(\) on unknown host, I should get an XML-RPC fault with code -1$/) do
  exception_thrown = false
  begin
    systest.bootstrap_system('imprettysureidontexist', '', false)
  rescue XMLRPC::FaultException => fault
    exception_thrown = true
    assert(fault.faultCode == -1, 'Fault code must be == -1.')
  end
  assert(exception_thrown, 'Exception must be thrown for non-existing host.')
end

When(/^I call system\.bootstrap\(\) on a salt minion with saltSSH = true, \
but with activation key with Default contact method, I should get an XML-RPC fault with code -1$/) do
  exception_thrown = false
  begin
    systest.bootstrap_system($minion.full_hostname, '1-SUSE-DEV-x86_64', true)
  rescue XMLRPC::FaultException => fault
    exception_thrown = true
    assert(fault.faultCode == -1, 'Fault code must be == -1.')
  end
  assert(exception_thrown, 'Exception must be thrown for non-compatible activation keys.')
end

When(/^I schedule a highstate for "([^"]*)" via XML\-RPC$/) do |host|
  system_name = get_system_name(host)
  node_id = retrieve_server_id(system_name)
  now = DateTime.now
  date_high = XMLRPC::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  systest.schedule_apply_highstate(node_id, date_high, false)
end

When(/^I unsubscribe "([^"]*)" from configuration channel "([^"]*)"$/) do |host1, channel|
  system_name1 = get_system_name(host1)
  node_id1 = retrieve_server_id(system_name1)
  systest.remove_channels([ node_id1 ], [ channel ])
end

When(/^I unsubscribe "([^"]*)" and "([^"]*)" from configuration channel "([^"]*)"$/) do |host1, host2, channel|
  steps %(
      When I unsubscribe "#{host1}" from configuration channel "#{channel}"
      And I unsubscribe "#{host2}" from configuration channel "#{channel}"
  )
end

When(/^I create a System Record$/) do
  dev = { 'name' => 'eth0', 'ip' => '1.1.1.1', 'mac' => '00:22:22:77:EE:CC', 'dnsname' => 'testserver.example.com' }
  systest.create_system_record('testserver', 'fedora_kickstart_profile_upload', '', 'my test server', [dev])
end

When(/^I logout from XML\-RPC system namespace$/) do
  systest.logout
end

# user namespace

CREATE_USER_PASSWORD = 'die gurke'.freeze

Given(/^I am logged in via XML\-RPC user as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @rpc = XMLRPCUserTest.new(ENV['SERVER'])
  @rpc.login(luser, password)
end

Given(/^I am logged in via XML\-RPC user with the feature's user$/) do
  @rpc = XMLRPCUserTest.new(ENV['SERVER'])
  @rpc.login($username, $password)
end

When(/^I call user\.list_users\(\)$/) do
  @users = @rpc.get_users
end

Then(/^I should get at least user "([^"]*)"$/) do |luser|
  assert_includes(@users.map { |u| u['login'] }, luser)
end

When(/^I call user\.get_details\(\) on user "([^"]*)"$/) do |luser|
  @roles = @rpc.get_user_roles(luser)
end

Then(/^I should see at least one role that matches "([^"]*)" suffix$/) do |sfx|
  refute(@roles.find_all { |el| el =~ /#{sfx}/ }.empty?)
end

When(/^I call user\.create\(sid, login, pwd, name, lastname, email\) with login "([^"]*)"$/) do |luser|
  refute(@rpc.create_user(luser, CREATE_USER_PASSWORD) != 1)
end

Then(/^when I call user\.list_users\(\), I should see a user "([^"]*)"$/) do |luser|
  steps %{
    When I call user.list_users()
    Then I should get at least user "#{luser}"
  }
end

When(/^I call user\.add_role\(\) on "([^"]*)" with the role "([^"]*)"$/) do |luser, role|
  refute(@rpc.add_role(luser, role) != 1)
end

Then(/^I should see "([^"]*)" when I call user\.list_roles\(\) with "([^"]*)"$/) do |rolename, luser|
  assert_includes(@rpc.get_user_roles(luser), rolename)
end

When(/^I delete user "([^"]*)"$/) do |luser|
  @rpc.delete_user(luser)
end

Given(/^I make sure "([^"]*)" is not present$/) do |luser|
  @rpc.get_users
      .map { |u| u['login'] }
      .select { |l| l == luser }
      .each { @rpc.delete_user(luser) }
end

When(/^I call user\.remove_role\(\) against uid "([^"]*)" with the role "([^"]*)"$/) do |luser, rolename|
  refute(@rpc.del_role(luser, rolename) != 1)
end

Then(/^I shall not see "([^"]*)" when I call user\.list_roles\(\) with "([^"]*)" uid$/) do |rolename, luser|
  refute_includes(@rpc.get_user_roles(luser), rolename)
end

Then(/^I logout from XML\-RPC user namespace$/) do
  assert(@rpc.logout)
end

# channel namespace

Given(/^I am logged in via XML\-RPC channel as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  assert(rpctest.login(luser, password))
end

Given(/^I am logged in via XML\-RPC channel with the feature's user$/) do
  assert(rpctest.login($username, $password))
end

When(/^I create a repo with label "([^"]*)" and url$/) do |label|
  url = "http://#{$server.ip}/pub/AnotherRepo/"
  assert(rpctest.create_repo(label, url))
end

When(/^I associate repo "([^"]*)" with channel "([^"]*)"$/) do |repo_label, channel_label|
  assert(rpctest.associate_repo(channel_label, repo_label))
end

When(/^I create the following channels:$/) do |table|
  channels = table.hashes
  channels.each do |ch|
    assert_equal(1,
      rpctest.create(
        ch['LABEL'], ch['NAME'], ch['SUMMARY'], ch['ARCH'], ch['PARENT']
      )
    )
  end
end

When(/^I delete the software channel with label "([^"]*)"$/) do |label|
  assert_equal(1, rpctest.delete(label))
end

When(/^I delete the repo with label "([^"]*)"$/) do |label|
  assert_equal(1, rpctest.delete_repo(label))
end

Then(/^something should get listed with a call of listSoftwareChannels$/) do
  assert_equal(false, rpctest.get_software_channels_count < 1)
end

Then(/^"([^"]*)" should get listed with a call of listSoftwareChannels$/) do |label|
  assert(rpctest.verify_channel(label))
end

Then(/^"([^"]*)" should not get listed with a call of listSoftwareChannels$/) do |label|
  assert_equal(false, rpctest.verify_channel(label))
end

Then(/^"([^"]*)" should be the parent channel of "([^"]*)"$/) do |parent, child|
  assert(rpctest.is_parent_channel(child, parent))
end

Then(/^channel "([^"]*)" should have attribute "([^"]*)" from type "([^"]*)"$/) do |label, attr, type|
  ret = rpctest.get_channel_details(label)
  assert(ret)
  assert_equal(type, ret[attr].class.to_s)
end

Then(/^channel "([^"]*)" should not have attribute "([^"]*)"$/) do |label, attr|
  ret = rpctest.get_channel_details(label)
  assert(ret)
  assert_equal(false, ret.key?(attr))
end

# activationkey namespace

acttest = XMLRPCActivationKeyTest.new(ENV['SERVER'])
key = nil

Given(/^I am logged in via XML\-RPC activationkey as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  raise unless acttest.login(luser, password)
end

Given(/^I am logged in via XML\-RPC activationkey with the feature's user$/) do
  raise unless acttest.login($username, $password)
end

When(/^I create an AK with id "([^"]*)", description "([^"]*)" and limit of (\d+)$/) do |id, dscr, limit|
  key = acttest.create_key(id, dscr, limit)
  raise if key.nil?
end

Then(/^I should get it listed with a call of listActivationKeys$/) do
  raise unless acttest.verify_key(key)
end

When(/^I call listActivationKeys I should get some$/) do
  raise if acttest.get_activation_keys_count < 1
end

Then(/^I should get key deleted$/) do
  raise unless acttest.delete_key(key)
  raise if acttest.verify_key(key)
end

When(/^I add config channels "([^"]*)" to a newly created key$/) do |channel_name|
  raise if acttest.add_config_channel(key, channel_name) < 1
end

When(/^I call activationkey\.set_details\(\) to the key$/) do
  raise unless acttest.set_details(key)
end

Then(/^I have to see them by calling activationkey\.get_details\(\)$/) do
  raise unless acttest.get_details(key)
end

# virtualhostmanager namespace

virtualhostmanager = XMLRPCVHMTest.new(ENV['SERVER'])
modules = []
vhms = []
params = {}
detail = {}

Given(/^I am logged in via XML\-RPC virtualhostmanager as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  virtualhostmanager.login(luser, password)
end

Given(/^I am logged in via XML\-RPC virtualhostmanager with the feature's user$/) do
  virtualhostmanager.login($username, $password)
end

When(/^I call virtualhostmanager.list_available_virtual_host_gatherer_modules\(\)$/) do
  modules = virtualhostmanager.list_available_virtual_host_gatherer_modules
end

When(/^I call virtualhostmanager.list_virtual_host_managers\(\)$/) do
  vhms = virtualhostmanager.list_virtual_host_managers
end

When(/^I call virtualhostmanager.get_module_parameters\(\) for "([^"]*)"$/) do |module_name|
  params = virtualhostmanager.get_module_parameters(module_name)
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

When(/^I call virtualhostmanager.get_detail\("([^"]*)"\)$/) do |label|
  detail = virtualhostmanager.get_detail(label)
end

Then(/^"([^"]*)" should be "([^"]*)"$/) do |key1, value1|
  assert(detail.key?(key1), "Expect parameter key '#{key1}', but got only '#{detail}'")
  assert(detail[key1].to_s == value, "Expect value for #{key1} should be '#{value1}, but got '#{detail[key1]}'")
end

Then(/^configs "([^"]*)" should be "([^"]*)"$/) do |key1, value1|
  assert(detail['configs'].key?(key1), "Expect parameter key '#{key1}', but got only '#{detail['configs']}'")
  assert(detail['configs'][key1].to_s == value1, "Expect value for #{key1} should be '#{value1}, but got '#{detail['configs'][key1]}'")
end

Then(/^I logout from XML\-RPC virtualhostmanager namespace$/) do
  virtualhostmanager.logout
end

# actionchain namespace

rpc = XMLRPCActionChain.new(ENV['SERVER'])
syschaintest = XMLRPCSystemTest.new(ENV['SERVER'])
scdrpc = XMLRPCScheduleTest.new(ENV['SERVER'])

# Authenticate
Given(/^I am logged in via XML\-RPC actionchain as user "(.*?)" and password "(.*?)"$/) do |luser, password|
  rpc.login(luser, password)
  syschaintest.login(luser, password)
  scdrpc.login(luser, password)
end

Given(/^I am logged in via XML\-RPC actionchain with the feature's user$/) do
  rpc.login($username, $password)
  syschaintest.login($username, $password)
  scdrpc.login($username, $password)
end

Given(/^I want to operate on this "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  $client_id = syschaintest.search_by_name(system_name).first['id']
  refute_nil($client_id, "Could not find system with hostname #{system_name}")
end

# Listing chains
When(/^I call XML\-RPC createChain with chainLabel "(.*?)"$/) do |label|
  action_id = rpc.create_chain(label)
  refute(action_id < 1)
  $chain_label = label
end

When(/^I call actionchain\.list_chains\(\) if label "(.*?)" is there$/) do |label|
  assert_includes(rpc.list_chains, label)
end

# Deleting chain
Then(/^I delete the action chain$/) do
  begin
    rpc.delete_chain($chain_label)
  rescue XMLRPC::FaultException => e
    raise format('deleteChain: XML-RPC failure, code %s: %s', e.faultCode, e.fault_string)
  end
end

Then(/^I delete an action chain, labeled "(.*?)"$/) do |label|
  begin
    rpc.delete_chain(label)
  rescue XMLRPC::FaultException => e
    raise format('deleteChain: XML-RPC failure, code %s: %s', e.faultCode, e.fault_string)
  end
end

Then(/^I delete all action chains$/) do
  begin
    rpc.list_chains.each do |label|
      puts "Delete chain: #{label}"
      rpc.delete_chain(label)
    end
  rescue XMLRPC::FaultException => e
    raise format('deleteChain: XML-RPC failure, code %s: %s', e.faultCode, e.fault_string)
  end
end

# Renaming chain
Then(/^I call actionchain\.rename_chain\(\) to rename it from "(.*?)" to "(.*?)"$/) do |old_label, new_label|
  rpc.rename_chain(old_label, new_label)
end

Then(/^there should be a new action chain with the label "(.*?)"$/) do |label|
  assert_includes(rpc.list_chains, label)
end

Then(/^there should be an action chain with the label "(.*?)"$/) do |label|
  assert_includes(rpc.list_chains, label)
end

Then(/^there should be no action chain with the label "(.*?)"$/) do |label|
  refute_includes(rpc.list_chains, label)
end

Then(/^no action chain with the label "(.*?)"$/) do |label|
  refute_includes(rpc.list_chains, label)
end

# Schedule scenario
When(/^I call actionchain\.add_script_run\(\) with the script "(.*?)"$/) do |script|
  refute(rpc.add_script_run($client_id, "#!/bin/bash\n" + script, $chain_label) < 1)
end

Then(/^I should be able to see all these actions in the action chain$/) do
  begin
    actions = rpc.list_chain_actions($chain_label)
    refute_nil(actions)
    puts 'Running actions:'
    actions.each do |action|
      puts "\t- " + action['label']
    end
  rescue XMLRPC::FaultException => e
    raise format('Error listChainActions: XML-RPC failure, code %s: %s', e.faultCode, e.fault_string)
  end
end

# Reboot
When(/^I call actionchain\.add_system_reboot\(\)$/) do
  refute(rpc.add_system_reboot($client_id, $chain_label) < 1)
end

# Packages operations
When(/^I call actionchain\.add_package_install\(\)$/) do
  pkgs = syschaintest.list_all_installable_packages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(rpc.add_package_install($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

When(/^I call actionchain\.add_package_removal\(\)$/) do
  pkgs = syschaintest.list_all_installable_packages($client_id)
  refute(rpc.add_package_removal($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

When(/^I call actionchain\.add_package_upgrade\(\)$/) do
  pkgs = syschaintest.list_latest_upgradable_packages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(rpc.add_package_upgrade($client_id, [pkgs[0]['to_package_id']], $chain_label) < 1)
end

When(/^I call actionchain\.add_package_verify\(\)$/) do
  pkgs = syschaintest.list_all_installable_packages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(rpc.add_package_verify($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

# Manage actions within the action chain
When(/^I call actionchain\.remove_action on each action within the chain$/) do
  begin
    actions = rpc.list_chain_actions($chain_label)
    refute_nil(actions)
    actions.each do |action|
      refute(rpc.remove_action($chain_label, action['id']) < 0)
      puts "\t- Removed \"" + action['label'] + '" action'
    end
  rescue XMLRPC::FaultException => e
    raise format('Error remove_action: XML-RPC failure, code %s: %s', e.faultCode, e.fault_string)
  end
end

Then(/^the current action chain should be empty$/) do
  assert_empty(rpc.list_chain_actions($chain_label))
end

# Scheduling the action chain
When(/^I schedule the action chain$/) do
  refute(rpc.schedule_chain($chain_label, DateTime.now) < 0)
end

Then(/^there should be no more my action chain$/) do
  refute_includes(rpc.list_chains, $chain_label)
end

When(/^I wait until there are no more action chains$/) do
  repeat_until_timeout(message: 'Action Chains still present') do
    break if rpc.list_chains.empty?
    sleep 2
  end
end

Then(/^I should see scheduled action, called "(.*?)"$/) do |label|
  assert_includes(label, scdrpc.list_in_progress_actions.map { |a| a['name'] })
end

Then(/^I cancel all scheduled actions$/) do
  actions = scdrpc.list_in_progress_actions.reject do |action|
    action['prerequisite']
  end

  actions.each do |action|
    puts "\t- Try to cancel \"#{action['name']}\" action"
    begin
      scdrpc.cancel_actions([action['id']])
    rescue XMLRPC::FaultException
      scdrpc.list_in_progress_systems(action['id']).each do |system|
        scdrpc.fail_system_action(system['server_id'], action['id'])
      end
    end
    puts "\t- Removed \"#{action['name']}\" action"
  end
end

Then(/^there should be no more any scheduled actions$/) do
  assert_empty(scdrpc.list_in_progress_actions)
end

Then(/^I wait until there are no more scheduled actions$/) do
  repeat_until_timeout(message: 'Scheduled actions still present') do
    break if scdrpc.list_in_progress_actions.empty?
    sleep 2
  end
end

Given(/^I am logged in via XML\-RPC api as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @rpc_api_tester = XMLRPCApiTest.new(ENV['SERVER'])
  assert(@rpc_api_tester.login(luser, password))
end

Given(/^I am logged in via XML\-RPC api with the feature's user$/) do
  @rpc_api_tester = XMLRPCApiTest.new(ENV['SERVER'])
  assert(@rpc_api_tester.login($username, $password))
end

# power management namespace

Given(/^I am logged in via XML\-RPC powermgmt as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @rpctest = XMLRPCPowermanagementTest.new(ENV['SERVER'])
  @rpctest.login(luser, password)
  syschaintest.login(luser, password)
end

Given(/^I am logged in via XML\-RPC powermgmt with the feature's user$/) do
  @rpctest = XMLRPCPowermanagementTest.new(ENV['SERVER'])
  @rpctest.login($username, $password)
  syschaintest.login($username, $password)
end

When(/^I fetch power management values$/) do
  @powermgmt_result = @rpctest.get_details($client_id)
end

Then(/^power management results should have "([^"]*)" for "([^"]*)"$/) do |value, hkey|
  assert_equal(value, @powermgmt_result[hkey])
end

Then(/^I set power management value "([^"]*)" for "([^"]*)"$/) do |value, hkey|
  @rpctest.set_details($client_id, { hkey => value })
end

Then(/^I turn power on$/) do
  @rpctest.power_on($client_id)
end

Then(/^I turn power off$/) do
  @rpctest.power_off($client_id)
end

Then(/^I do power management reboot$/) do
  @rpctest.reboot($client_id)
end

Then(/^the power status is "([^"]*)"$/) do |estat|
  stat = @rpctest.get_status($client_id)
  assert(stat) if estat == 'on'
  assert(!stat) if estat == 'off'
end

# cveaudit namespace

Given(/^I am logged in via XML\-RPC cve audit as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @rpctest = XMLRPCCVEAuditTest.new(ENV['SERVER'])
  @rpctest.login(luser, password)
end

Given(/^I am logged in via XML\-RPC cve audit with the feature's user$/) do
  @rpctest = XMLRPCCVEAuditTest.new(ENV['SERVER'])
  @rpctest.login($username, $password)
end

Given(/^channel data has already been updated$/) do
  assert_equals(@rpctest.populate_cveserver_channels, 1)
end

When(/^I call audit.list_systems_by_patch_status with CVE identifier "([^\"]*)"$/) do |cve_identifier|
  @result_list = @rpctest.list_systems_by_patch_status(cve_identifier) || []
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

Then(/^I should get the test channel$/) do
  arch = `uname -m`
  arch.chomp!
  channel = if arch != 'x86_64'
              'test-channel-i586'
            else
              'test-channel-x86_64'
            end
  $stderr.puts "result: #{@result}"
  assert(@result['channel_labels'].include?(channel))
end

Then(/^I should get the "([^"]*)" patch$/) do |patch|
  $stderr.puts "result: #{@result}"
  assert(@result['errata_advisories'].include?(patch))
end

Then(/^I logout from XML\-RPC cve audit namespace$/) do
  assert(@rpctest.logout)
end

# configchannel namespace

cfgtest = XMLRPCConfigChannelTest.new(ENV['SERVER'])

Given(/^I am logged in via XML\-RPC configchannel as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  cfgtest.login(luser, password)
end

Given(/^I am logged in via XML\-RPC configchannel with the feature's user$/) do
  cfgtest.login($username, $password)
end

Then(/^channel "([^"]*)" should exist$/) do |channel|
  assert_equal(1, cfgtest.channel_exists(channel))
end

Then(/^channel "([^"]*)" should contain file "([^"]*)"$/) do |channel, file|
  result = cfgtest.list_files(channel)
  assert_equal(1, result.count { |item| item['path'] == file })
end

Then(/^"([^"]*)" should be subscribed to channel "([^"]*)"$/) do |host, channel|
  system_name = get_system_name(host)
  result = cfgtest.list_subscribed_systems(channel)
  assert_equal(1, result.count { |item| item['name'] == system_name })
end

Then(/^"([^"]*)" should not be subscribed to channel "([^"]*)"$/) do |host, channel|
  system_name = get_system_name(host)
  result = cfgtest.list_subscribed_systems(channel)
  assert_equal(0, result.count { |item| item['name'] == system_name })
end

When(/^I add file "([^"]*)" containing "([^"]*)" to channel "([^"]*)"$/) do |file, contents, channel|
  cfgtest.create_or_update_path(channel, file, contents)
end

When(/^I deploy all systems registered to channel "([^"]*)"$/) do |channel|
  cfgtest.deploy_all_systems(channel)
end

When(/^I logout from XML\-RPC configchannel namespace$/) do
  cfgtest.logout
end

When(/^I call system.create_system_profile\(\) with name "([^"]*)" and HW address "([^"]*)"$/) do |name, hw_address|
  profile_id = systest.create_system_profile(name, 'hwAddress' => hw_address)
  refute_nil(profile_id)
end

When(/^I call system\.create_system_profile\(\) with name "([^"]*)" and hostname "([^"]*)"$/) do |name, hostname|
  profile_id = systest.create_system_profile(name, 'hostname' => hostname)
  refute_nil(profile_id)
end

When(/^I call system\.list_empty_system_profiles\(\)$/) do
  $output = systest.list_empty_system_profiles
end

Then(/^"([^"]*)" should be present in the result$/) do |profile_name|
  assert($output.select { |p| p['name'] == profile_name }.count == 1)
end
