# Copyright (c) 2015-2022 SUSE LLC
# Licensed under the terms of the MIT license.

require 'json'
require 'xmlrpc/client'
require 'socket'

# system namespace

Given(/^I am logged in via XML\-RPC system as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @system_api = XMLRPCSystemTest.new($server.full_hostname)
  @system_api.login(luser, password)
end

When(/^I call system\.list_systems\(\), I should get a list of them$/) do
  # This also assumes the test is called *after* the regular test.
  servers = @system_api.list_systems
  assert(servers.!empty?, "Expect: 'number of system' > 0, but found only '#{servers.length}' servers")
end

When(/^I call system\.bootstrap\(\) on host "([^"]*)" and salt\-ssh "([^"]*)"$/) do |host, salt_ssh_enabled|
  system_name = get_system_name(host)
  salt_ssh = (salt_ssh_enabled == 'enabled')
  akey = salt_ssh ? '1-SUSE-SSH-KEY-x86_64' : '1-SUSE-KEY-x86_64'
  result = @system_api.bootstrap_system(system_name, akey, salt_ssh)
  assert(result == 1, 'Bootstrap return code not equal to 1.')
end

When(/^I call system\.bootstrap\(\) on unknown host, I should get an XML-RPC fault with code -1$/) do
  exception_thrown = false
  begin
    @system_api.bootstrap_system('imprettysureidontexist', '', false)
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
    @system_api.bootstrap_system($minion.full_hostname, '1-SUSE-KEY-x86_64', true)
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
  @system_api.schedule_apply_highstate(node_id, date_high, false)
end

When(/^I unsubscribe "([^"]*)" from configuration channel "([^"]*)"$/) do |host1, channel|
  system_name1 = get_system_name(host1)
  node_id1 = retrieve_server_id(system_name1)
  @system_api.remove_channels([ node_id1 ], [ channel ])
end

When(/^I unsubscribe "([^"]*)" and "([^"]*)" from configuration channel "([^"]*)"$/) do |host1, host2, channel|
  steps %(
      When I unsubscribe "#{host1}" from configuration channel "#{channel}"
      And I unsubscribe "#{host2}" from configuration channel "#{channel}"
  )
end

When(/^I create a System Record$/) do
  dev = { 'name' => 'eth0', 'ip' => '1.1.1.1', 'mac' => '00:22:22:77:EE:CC', 'dnsname' => 'testserver.example.com' }
  @system_api.create_system_record('testserver', 'fedora_kickstart_profile_upload', '', 'my test server', [dev])
end

When(/^I logout from XML\-RPC system namespace$/) do
  @system_api.logout
end

# user namespace

CREATE_USER_PASSWORD = 'die gurke'.freeze

Given(/^I am logged in via XML\-RPC user as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @user_api = XMLRPCUserTest.new($server.full_hostname)
  @user_api.login(luser, password)
end

When(/^I call user\.list_users\(\)$/) do
  @users = @user_api.get_users
end

Then(/^I should get at least user "([^"]*)"$/) do |luser|
  assert_includes(@users.map { |u| u['login'] }, luser)
end

When(/^I call user\.get_details\(\) on user "([^"]*)"$/) do |luser|
  @roles = @user_api.get_user_roles(luser)
end

Then(/^I should see at least one role that matches "([^"]*)" suffix$/) do |sfx|
  refute(@roles.find_all { |el| el =~ /#{sfx}/ }.empty?)
end

When(/^I call user\.create\(sid, login, pwd, name, lastname, email\) with login "([^"]*)"$/) do |luser|
  refute(@user_api.create_user(luser, CREATE_USER_PASSWORD) != 1)
end

Then(/^when I call user\.list_users\(\), I should see a user "([^"]*)"$/) do |luser|
  steps %{
    When I call user.list_users()
    Then I should get at least user "#{luser}"
  }
end

When(/^I call user\.add_role\(\) on "([^"]*)" with the role "([^"]*)"$/) do |luser, role|
  refute(@user_api.add_role(luser, role) != 1)
end

Then(/^I should see "([^"]*)" when I call user\.list_roles\(\) with "([^"]*)"$/) do |rolename, luser|
  assert_includes(@user_api.get_user_roles(luser), rolename)
end

When(/^I delete user "([^"]*)"$/) do |luser|
  @user_api.delete_user(luser)
end

Given(/^I make sure "([^"]*)" is not present$/) do |luser|
  @user_api.get_users
           .map { |u| u['login'] }
           .select { |l| l == luser }
           .each { @user_api.delete_user(luser) }
end

When(/^I call user\.remove_role\(\) against uid "([^"]*)" with the role "([^"]*)"$/) do |luser, rolename|
  refute(@user_api.del_role(luser, rolename) != 1)
end

Then(/^I shall not see "([^"]*)" when I call user\.list_roles\(\) with "([^"]*)" uid$/) do |rolename, luser|
  refute_includes(@user_api.get_user_roles(luser), rolename)
end

Then(/^I logout from XML\-RPC user namespace$/) do
  assert(@user_api.logout)
end

# channel namespace

Given(/^I am logged in via XML\-RPC channel as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @channel_api = XMLRPCChannelTest.new($server.full_hostname)
  assert(@channel_api.login(luser, password))
end

When(/^I create a repo with label "([^"]*)" and url$/) do |label|
  url = "http://#{$server.full_hostname}/pub/AnotherRepo/"
  assert(@channel_api.create_repo(label, url))
end

When(/^I associate repo "([^"]*)" with channel "([^"]*)"$/) do |repo_label, channel_label|
  assert(@channel_api.associate_repo(channel_label, repo_label))
end

When(/^I create the following channels:$/) do |table|
  channels = table.hashes
  channels.each do |ch|
    assert_equal(1,
      @channel_api.create(
        ch['LABEL'], ch['NAME'], ch['SUMMARY'], ch['ARCH'], ch['PARENT']
      )
    )
  end
end

When(/^I delete the software channel with label "([^"]*)"$/) do |label|
  assert_equal(1, @channel_api.delete(label))
end

When(/^I delete the repo with label "([^"]*)"$/) do |label|
  assert_equal(1, @channel_api.delete_repo(label))
end

Then(/^something should get listed with a call of listSoftwareChannels$/) do
  assert_equal(false, @channel_api.get_software_channels_count < 1)
end

Then(/^"([^"]*)" should get listed with a call of listSoftwareChannels$/) do |label|
  assert(@channel_api.verify_channel(label))
end

Then(/^"([^"]*)" should not get listed with a call of listSoftwareChannels$/) do |label|
  assert_equal(false, @channel_api.verify_channel(label))
end

Then(/^"([^"]*)" should be the parent channel of "([^"]*)"$/) do |parent, child|
  assert(@channel_api.is_parent_channel(child, parent))
end

Then(/^channel "([^"]*)" should have attribute "([^"]*)" from type "([^"]*)"$/) do |label, attr, type|
  ret = @channel_api.get_channel_details(label)
  assert(ret)
  assert_equal(type, ret[attr].class.to_s)
end

Then(/^channel "([^"]*)" should not have attribute "([^"]*)"$/) do |label, attr|
  ret = @channel_api.get_channel_details(label)
  assert(ret)
  assert_equal(false, ret.key?(attr))
end

# activationkey namespace

key = nil

Given(/^I am logged in via XML\-RPC activationkey as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @activation_key_api = XMLRPCActivationKeyTest.new($server.full_hostname)
  raise unless @activation_key_api.login(luser, password)
end

When(/^I create an AK with id "([^"]*)", description "([^"]*)" and limit of (\d+)$/) do |id, dscr, limit|
  key = @activation_key_api.create_key(id, dscr, '', limit)
  raise if key.nil?
end

Then(/^I should get it listed with a call of listActivationKeys$/) do
  raise unless @activation_key_api.verify_key(key)
end

When(/^I call listActivationKeys I should get some$/) do
  raise if @activation_key_api.get_activation_keys_count < 1
end

Then(/^I should get key deleted$/) do
  raise unless @activation_key_api.delete_key(key)
  raise if @activation_key_api.verify_key(key)
end

When(/^I add config channels "([^"]*)" to a newly created key$/) do |channel_name|
  raise if @activation_key_api.add_config_channels(key, [channel_name]) < 1
end

When(/^I call activationkey\.set_details\(\) to the key setting as description "([^"]*)"$/) do |description|
  raise unless @activation_key_api.set_details(key, description, '', 10, 'default')
end

Then(/^I have to see them by calling activationkey\.get_details\(\) having as description "([^"]*)"$/) do |description|
  details = @activation_key_api.get_details(key)
  log "Key info for the key details['key']:"
  details.each_pair do |k, v|
    log "  #{k}:#{v}"
  end
  raise unless details['description'] == description
end

# rubocop:disable Metrics/BlockLength
When(/^I create an activation key including custom channels for "([^"]*)" via XML-RPC$/) do |client|
  steps %(
    When I am logged in via XML-RPC activationkey as user "admin" and password "admin"
    And I am logged in via XML-RPC channel as user "admin" and password "admin"
  )

  # Create a key with the base channel for this client
  id = description = "#{client}_key"
  base_channel = LABEL_BY_BASE_CHANNEL[BASE_CHANNEL_BY_CLIENT[client]]
  key = @activation_key_api.create_key(id, description, base_channel, 100)
  raise if key.nil?

  is_ssh_minion = client.include? 'ssh_minion'
  @activation_key_api.set_details(key, description, base_channel, 100, is_ssh_minion ? 'ssh-push' : 'default')

  # Get the list of child channels for this base channel
  child_channels = @channel_api.list_child_channels(base_channel)

  # Select all the child channels for this client
  client.sub! 'ssh_minion', 'minion'
  if client.include? 'buildhost'
    selected_child_channels = ["custom_channel_#{client.sub('buildhost', 'minion')}", "custom_channel_#{client.sub('buildhost', 'client')}"]
  elsif client.include? 'terminal'
    selected_child_channels = ["custom_channel_#{client.sub('terminal', 'minion')}", "custom_channel_#{client.sub('terminal', 'client')}"]
  else
    custom_channel = "custom_channel_#{client}"
    selected_child_channels = [custom_channel]
  end
  child_channels.each do |child_channel|
    selected_child_channels.push(child_channel) unless child_channel.include? 'custom_channel'
  end

  begin
    @activation_key_api.add_child_channels(key, selected_child_channels)
  rescue XMLRPC::FaultException => err
    log "The selected child channels can not be included: #{selected_child_channels}. Error: #{err}"
  end
end
# rubocop:enable Metrics/BlockLength

# actionchain namespace

# Auth
Given(/^I am logged in via XML\-RPC actionchain as user "(.*?)" and password "(.*?)"$/) do |luser, password|
  # Authenticate
  @action_chain_api = XMLRPCActionChain.new($server.full_hostname)
  @schedule_api = XMLRPCScheduleTest.new($server.full_hostname)
  @system_api = XMLRPCSystemTest.new($server.full_hostname)
  @action_chain_api.login(luser, password)
  @system_api.login(luser, password)
  @schedule_api.login(luser, password)
end

Given(/^I want to operate on this "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  $client_id = @system_api.search_by_name(system_name).first['id']
  refute_nil($client_id, "Could not find system with hostname #{system_name}")
end

# Listing chains
When(/^I call XML\-RPC createChain with chainLabel "(.*?)"$/) do |label|
  action_id = @action_chain_api.create_chain(label)
  refute(action_id < 1)
  $chain_label = label
end

When(/^I call actionchain\.list_chains\(\) if label "(.*?)" is there$/) do |label|
  assert_includes(@action_chain_api.list_chains, label)
end

# Deleting chain
Then(/^I delete the action chain$/) do
  begin
    @action_chain_api.delete_chain($chain_label)
  rescue XMLRPC::FaultException => e
    raise format('deleteChain: XML-RPC failure, code %s: %s', e.faultCode, e.faultString)
  end
end

Then(/^I delete an action chain, labeled "(.*?)"$/) do |label|
  begin
    @action_chain_api.delete_chain(label)
  rescue XMLRPC::FaultException => e
    raise format('deleteChain: XML-RPC failure, code %s: %s', e.faultCode, e.faultString)
  end
end

Then(/^I delete all action chains$/) do
  begin
    @action_chain_api.list_chains.each do |label|
      log "Delete chain: #{label}"
      @action_chain_api.delete_chain(label)
    end
  rescue XMLRPC::FaultException => e
    raise format('deleteChain: XML-RPC failure, code %s: %s', e.faultCode, e.faultString)
  end
end

# Renaming chain
Then(/^I call actionchain\.rename_chain\(\) to rename it from "(.*?)" to "(.*?)"$/) do |old_label, new_label|
  @action_chain_api.rename_chain(old_label, new_label)
end

Then(/^there should be a new action chain with the label "(.*?)"$/) do |label|
  assert_includes(@action_chain_api.list_chains, label)
end

Then(/^there should be an action chain with the label "(.*?)"$/) do |label|
  assert_includes(@action_chain_api.list_chains, label)
end

Then(/^there should be no action chain with the label "(.*?)"$/) do |label|
  refute_includes(@action_chain_api.list_chains, label)
end

Then(/^no action chain with the label "(.*?)"$/) do |label|
  refute_includes(@action_chain_api.list_chains, label)
end

# Schedule scenario
When(/^I call actionchain\.add_script_run\(\) with the script "(.*?)"$/) do |script|
  refute(@action_chain_api.add_script_run($client_id, "#!/bin/bash\n" + script, $chain_label) < 1)
end

Then(/^I should be able to see all these actions in the action chain$/) do
  begin
    actions = @action_chain_api.list_chain_actions($chain_label)
    refute_nil(actions)
    log 'Running actions:'
    actions.each do |action|
      log "\t- " + action['label']
    end
  rescue XMLRPC::FaultException => e
    raise format('Error listChainActions: XML-RPC failure, code %s: %s', e.faultCode, e.faultString)
  end
end

# Reboot
When(/^I call actionchain\.add_system_reboot\(\)$/) do
  refute(@action_chain_api.add_system_reboot($client_id, $chain_label) < 1)
end

# Packages operations
When(/^I call actionchain\.add_package_install\(\)$/) do
  pkgs = @system_api.list_all_installable_packages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(@action_chain_api.add_package_install($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

When(/^I call actionchain\.add_package_removal\(\)$/) do
  pkgs = @system_api.list_all_installable_packages($client_id)
  refute(@action_chain_api.add_package_removal($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

When(/^I call actionchain\.add_package_upgrade\(\)$/) do
  pkgs = @system_api.list_latest_upgradable_packages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(@action_chain_api.add_package_upgrade($client_id, [pkgs[0]['to_package_id']], $chain_label) < 1)
end

When(/^I call actionchain\.add_package_verify\(\)$/) do
  pkgs = @system_api.list_all_installable_packages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(@action_chain_api.add_package_verify($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

# Manage actions within the action chain
When(/^I call actionchain\.remove_action on each action within the chain$/) do
  begin
    actions = @action_chain_api.list_chain_actions($chain_label)
    refute_nil(actions)
    actions.each do |action|
      refute(@action_chain_api.remove_action($chain_label, action['id']) < 0)
      log "\t- Removed \"" + action['label'] + '" action'
    end
  rescue XMLRPC::FaultException => e
    raise format('Error remove_action: XML-RPC failure, code %s: %s', e.faultCode, e.faultString)
  end
end

Then(/^the current action chain should be empty$/) do
  assert_empty(@action_chain_api.list_chain_actions($chain_label))
end

# Scheduling the action chain
When(/^I schedule the action chain$/) do
  refute(@action_chain_api.schedule_chain($chain_label, DateTime.now) < 0)
end

When(/^I wait until there are no more action chains$/) do
  repeat_until_timeout(message: 'Action Chains still present') do
    break if @action_chain_api.list_chains.empty?
    @action_chain_api.list_chains.each do |label|
      log "Chain still present: #{label}"
    end
    log
    sleep 2
  end
end

Then(/^I should see scheduled action, called "(.*?)"$/) do |label|
  assert_includes(
    @schedule_api.list_in_progress_actions.map { |a| a['name'] }, label
  )
end

Then(/^I cancel all scheduled actions$/) do
  actions = @schedule_api.list_in_progress_actions.reject do |action|
    action['prerequisite']
  end

  actions.each do |action|
    log "\t- Try to cancel \"#{action['name']}\" action"
    begin
      @schedule_api.cancel_actions([action['id']])
    rescue XMLRPC::FaultException
      @schedule_api.list_in_progress_systems(action['id']).each do |system|
        @schedule_api.fail_system_action(system['server_id'], action['id'])
      end
    end
    log "\t- Removed \"#{action['name']}\" action"
  end
end

Then(/^there should be no more any scheduled actions$/) do
  assert_empty(@schedule_api.list_in_progress_actions)
end

Then(/^I wait until there are no more scheduled actions$/) do
  repeat_until_timeout(message: 'Scheduled actions still present') do
    break if @schedule_api.list_in_progress_actions.empty?
    @schedule_api.list_in_progress_actions.each do |action|
      log "Action still in progress: #{action}"
    end
    sleep 2
  end
end

Given(/^I am logged in via XML\-RPC api as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @rpc_api_tester = XMLRPCApiTest.new($server.full_hostname)
  assert(@rpc_api_tester.login(luser, password))
end

# power management namespace

Given(/^I am logged in via XML\-RPC powermgmt as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @powermanagenent_api = XMLRPCPowermanagementTest.new($server.full_hostname)
  @powermanagenent_api.login(luser, password)
  @system_api = XMLRPCSystemTest.new($server.full_hostname)
  @system_api.login(luser, password)
end

When(/^I fetch power management values$/) do
  @powermgmt_result = @powermanagenent_api.get_details($client_id)
end

Then(/^power management results should have "([^"]*)" for "([^"]*)"$/) do |value, hkey|
  assert_equal(value, @powermgmt_result[hkey])
end

Then(/^I set power management value "([^"]*)" for "([^"]*)"$/) do |value, hkey|
  @powermanagenent_api.set_details($client_id, { hkey => value })
end

Then(/^I turn power on$/) do
  @powermanagenent_api.power_on($client_id)
end

Then(/^I turn power off$/) do
  @powermanagenent_api.power_off($client_id)
end

Then(/^I do power management reboot$/) do
  @powermanagenent_api.reboot($client_id)
end

Then(/^the power status is "([^"]*)"$/) do |estat|
  stat = @powermanagenent_api.get_status($client_id)
  assert(stat) if estat == 'on'
  assert(!stat) if estat == 'off'
end

# cveaudit namespace

Given(/^I am logged in via XML\-RPC cve audit as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @cve_audit_api = XMLRPCCVEAuditTest.new($server.full_hostname)
  @cve_audit_api.login(luser, password)
end

Given(/^channel data has already been updated$/) do
  assert_equals(@cve_audit_api.populate_cveserver_channels, 1)
end

When(/^I call audit.list_systems_by_patch_status with CVE identifier "([^\"]*)"$/) do |cve_identifier|
  @result_list = @cve_audit_api.list_systems_by_patch_status(cve_identifier) || []
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
  STDOUT.puts "result: #{@result}"
  assert(@result['channel_labels'].include?(channel))
end

Then(/^I should get the "([^"]*)" patch$/) do |patch|
  STDOUT.puts "result: #{@result}"
  assert(@result['errata_advisories'].include?(patch))
end

Then(/^I logout from XML\-RPC cve audit namespace$/) do
  assert(@cve_audit_api.logout)
end

# configchannel namespace

Given(/^I am logged in via XML\-RPC configchannel as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  @configuration_channel_api = XMLRPCConfigChannelTest.new($server.full_hostname)
  @configuration_channel_api.login(luser, password)
end

Then(/^channel "([^"]*)" should exist$/) do |channel|
  assert_equal(1, @configuration_channel_api.channel_exists(channel))
end

Then(/^channel "([^"]*)" should contain file "([^"]*)"$/) do |channel, file|
  result = @configuration_channel_api.list_files(channel)
  assert_equal(1, result.count { |item| item['path'] == file })
end

Then(/^"([^"]*)" should be subscribed to channel "([^"]*)"$/) do |host, channel|
  system_name = get_system_name(host)
  result = @configuration_channel_api.list_subscribed_systems(channel)
  assert_equal(1, result.count { |item| item['name'] == system_name })
end

Then(/^"([^"]*)" should not be subscribed to channel "([^"]*)"$/) do |host, channel|
  system_name = get_system_name(host)
  result = @configuration_channel_api.list_subscribed_systems(channel)
  assert_equal(0, result.count { |item| item['name'] == system_name })
end

When(/^I create state channel "([^"]*)" via XML\-RPC$/) do |channel|
  @configuration_channel_api.create_channel(channel, channel, channel, 'state')
end

When(/^I create state channel "([^"]*)" containing "([^"]*)" via XML\-RPC$/) do |channel, contents|
  @configuration_channel_api.create_channel_with_data(channel, channel, channel, 'state', { 'contents' => contents })
end

When(/^I call configchannel.get_file_revision with file "([^"]*)", revision "([^"]*)" and channel "([^"]*)" via XML\-RPC$/) do |file_path, revision, channel|
  @get_file_revision_result = @configuration_channel_api.get_file_revision(channel, file_path, revision.to_i)
end

Then(/^I should get file contents "([^\"]*)"$/) do |contents|
  assert_equal(contents, @get_file_revision_result['contents'])
end

When(/^I add file "([^"]*)" containing "([^"]*)" to channel "([^"]*)"$/) do |file, contents, channel|
  @configuration_channel_api.create_or_update_path(channel, file, contents)
end

When(/^I deploy all systems registered to channel "([^"]*)"$/) do |channel|
  @configuration_channel_api.deploy_all_systems(channel)
end

When(/^I delete channel "([^"]*)" via XML\-RPC((?: without error control)?)$/) do |channel, error_control|
  begin
    @configuration_channel_api.delete_channels([channel])
  rescue XMLRPC::FaultException => e
    raise format('Error delete_channels: XML-RPC failure, code %s: %s', e.faultCode, e.faultString) if error_control.empty?
  end
end

When(/^I logout from XML\-RPC configchannel namespace$/) do
  @configuration_channel_api.logout
end

When(/^I call system.create_system_profile\(\) with name "([^"]*)" and HW address "([^"]*)"$/) do |name, hw_address|
  profile_id = @system_api.create_system_profile(name, 'hwAddress' => hw_address)
  refute_nil(profile_id)
end

When(/^I call system\.create_system_profile\(\) with name "([^"]*)" and hostname "([^"]*)"$/) do |name, hostname|
  profile_id = @system_api.create_system_profile(name, 'hostname' => hostname)
  refute_nil(profile_id)
end

When(/^I call system\.list_empty_system_profiles\(\)$/) do
  $output = @system_api.list_empty_system_profiles
end

Then(/^"([^"]*)" should be present in the result$/) do |profile_name|
  assert($output.select { |p| p['name'] == profile_name }.count == 1)
end
