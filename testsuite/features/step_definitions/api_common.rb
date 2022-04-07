# Copyright 2015-2022 SUSE LLC
# Licensed under the terms of the MIT license.

require 'json'
require 'socket'


$api_test = APITestHTTP.new($server.full_hostname)


## auth namespace

When(/^I am logged in API as user "([^"]*)" and password "([^"]*)"$/) do |user, password|
  $api_test.auth_login(user, password)
end

When(/^I logout from API$/) do
  $api_test.auth_logout
end


## system namespace

Given(/^I want to operate on this "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  $client_id = $api_test.system_search_by_name(system_name).first['id']
  refute_nil($client_id, "Could not find system with hostname #{system_name}")
end

When(/^I call system\.list_systems\(\), I should get a list of them$/) do
  # This also assumes the test is called *after* the regular test.
  servers = $api_test.system_list_systems
  assert(servers.!empty?, "Expect: 'number of system' > 0, but found only '#{servers.length}' servers")
end

When(/^I call system\.bootstrap\(\) on host "([^"]*)" and salt\-ssh "([^"]*)"$/) do |host, salt_ssh_enabled|
  system_name = get_system_name(host)
  salt_ssh = (salt_ssh_enabled == 'enabled')
  akey = salt_ssh ? '1-SUSE-SSH-KEY-x86_64' : '1-SUSE-KEY-x86_64'
  result = $api_test.system_bootstrap_system(system_name, akey, salt_ssh)
  assert(result == 1, 'Bootstrap return code not equal to 1.')
end

When(/^I call system\.bootstrap\(\) on unknown host, I should get an API fault with code -1$/) do
  exception_thrown = false
  begin
    $api_test.system_bootstrap_system('imprettysureidontexist', '', false)
  rescue API::FaultException => fault
    exception_thrown = true
    assert(fault.faultCode == -1, 'Fault code must be == -1.')
  end
  assert(exception_thrown, 'Exception must be thrown for non-existing host.')
end

When(/^I call system\.bootstrap\(\) on a Salt minion with saltSSH = true, \
but with activation key with default contact method, I should get an API fault with code -1$/) do
  exception_thrown = false
  begin
    $api_test.system_bootstrap_system($minion.full_hostname, '1-SUSE-KEY-x86_64', true)
  rescue API::FaultException => fault
    exception_thrown = true
    assert(fault.faultCode == -1, 'Fault code must be == -1.')
  end
  assert(exception_thrown, 'Exception must be thrown for non-compatible activation keys.')
end

When(/^I schedule a highstate for "([^"]*)" via API$/) do |host|
  system_name = get_system_name(host)
  node_id = $api_test.system_retrieve_server_id(system_name)
  now = DateTime.now
  date_high = API::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  $api_test.system_schedule_apply_highstate(node_id, date_high, false)
end

When(/^I unsubscribe "([^"]*)" from configuration channel "([^"]*)"$/) do |host1, channel|
  system_name1 = get_system_name(host1)
  node_id1 = retrieve_server_id(system_name1)
  $api_test.system_remove_channels([ node_id1 ], [ channel ])
end

When(/^I unsubscribe "([^"]*)" and "([^"]*)" from configuration channel "([^"]*)"$/) do |host1, host2, channel|
  steps %(
      When I unsubscribe "#{host1}" from configuration channel "#{channel}"
      And I unsubscribe "#{host2}" from configuration channel "#{channel}"
  )
end

When(/^I create a system record$/) do
  dev = { 'name' => 'eth0', 'ip' => '1.1.1.1', 'mac' => '00:22:22:77:EE:CC', 'dnsname' => 'testserver.example.com' }
  $api_test.system_create_system_record('testserver', 'fedora_kickstart_profile_upload', '', 'my test server', [dev])
end

When(/^I wait for the OpenSCAP audit to finish$/) do
  host = $server.full_hostname
  @sle_id = retrieve_server_id($minion.full_hostname)
  begin
    repeat_until_timeout(message: "process did not complete") do
      scans = $api_test.system_scap_list_xccdf_scans(@sle_id)
      # in the openscap test, we schedule 2 scans
      break if scans.length > 1
    end
  end
end

When(/^I refresh the packages on traditional "([^"]*)" through API$/) do |host|
  node = get_target(host)
  node_id = retrieve_server_id(node.full_hostname)
  now = DateTime.now
  date_schedule_now = API::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)

  id_refresh = $api_test.system_schedule_package_refresh(node_id, date_schedule_now)
  node.run('rhn_check -vvv')
  wait_action_complete(id_refresh, timeout: 600)
end

When(/^I run a script on traditional "([^"]*)" through API$/) do |host|
  node = get_target(host)
  node_id = retrieve_server_id(node.full_hostname)
  now = DateTime.now
  date_schedule_now = API::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)
  script = "#! /usr/bin/bash \n uptime && ls"

  id_script = $api_test.system_schedule_script_run(node_id, 'root', 'root', 500, script, date_schedule_now)
  node.run('rhn_check -vvv')
  wait_action_complete(id_script)
end

When(/^I reboot traditional "([^"]*)" through API$/) do |host|
  node = get_target(host)
  node_id = retrieve_server_id(node.full_hostname)
  now = DateTime.now
  date_schedule_now = API::DateTime.new(now.year, now.month, now.day, now.hour, now.min, now.sec)

  $api_test.system_schedule_reboot(node_id, date_schedule_now)
  node.run('rhn_check -vvv')
  reboot_timeout = 400
  check_shutdown(node.full_hostname, reboot_timeout)
  check_restart(node.full_hostname, node, reboot_timeout)

  @schedule_api.list_failed_actions().each do |action|
    systems = @schedule_api.list_failed_systems(action['id'])
    raise if systems.all? { |system| system['server_id'] == node_id }
  end
end


## user namespace

When(/^I call user\.list_users\(\)$/) do
  @users = $api_test.user_list_users
end

Then(/^I should get at least user "([^"]*)"$/) do |user|
  assert_includes(@users.map { |u| u['login'] }, user)
end

When(/^I call user\.list_roles\(\) on user "([^"]*)"$/) do |user|
  @roles = $api_test.user_list_roles(user)
end

Then(/^I should get at least one role that matches "([^"]*)" suffix$/) do |suffix|
  refute(@roles.find_all { |el| el =~ /#{suffix}/ }.empty?)
end

Then(/^I should get role "([^"]*)"$/) do |rolename|
  assert_includes(@roles, rolename)
end

Then(/^I should not get role "([^"]*)"$/) do |rolename|
  refute_includes(@roles, rolename)
end

When(/^I call user\.create\(\) with login "([^"]*)"$/) do |user|
  refute($api_test.user_create(user, 'JamesBond007', 'Hans', 'Mustermann', 'hans.mustermann@suse.com') != 1)
end

When(/^I call user\.add_role\(\) on "([^"]*)" with the role "([^"]*)"$/) do |user, role|
  refute($api_test.user_add_role(user, role) != 1)
end

When(/^I delete user "([^"]*)"$/) do |user|
  $api_test.user_delete(user)
end

When(/^I make sure "([^"]*)" is not present$/) do |user|
  $api_test.user_list_users
    .map { |u| u['login'] }
    .select { |l| l == user }
    .each { $api_test.user_delete(user) }
end

When(/^I call user\.remove_role\(\) on "([^"]*)" with the role "([^"]*)"$/) do |luser, rolename|
  refute($api_test.user_remove_role(luser, rolename) != 1)
end


## channel namespace

When(/^I create a repo with label "([^"]*)" and url$/) do |label|
  url = "http://#{$server.full_hostname}/pub/AnotherRepo/"
  assert($api_test.channel_create_repo(label, url))
end

When(/^I associate repo "([^"]*)" with channel "([^"]*)"$/) do |repo_label, channel_label|
  assert($api_test.channel_associate_repo(channel_label, repo_label))
end

When(/^I create the following channels:$/) do |table|
  channels = table.hashes
  channels.each do |ch|
    assert_equal(1,
      $api_test.channel_create(
        ch['LABEL'], ch['NAME'], ch['SUMMARY'], ch['ARCH'], ch['PARENT']
      )
    )
  end
end

When(/^I delete the software channel with label "([^"]*)"$/) do |label|
  assert_equal(1, $api_test.channel_delete(label))
end

When(/^I delete the repo with label "([^"]*)"$/) do |label|
  assert_equal(1, $api_test.channel_delete_repo(label))
end

Then(/^something should get listed with a call of listSoftwareChannels$/) do
  assert_equal(false, $api_test.channel_get_software_channels_count < 1)
end

Then(/^"([^"]*)" should get listed with a call of listSoftwareChannels$/) do |label|
  assert($api_test.channel_verify_channel(label))
end

Then(/^"([^"]*)" should not get listed with a call of listSoftwareChannels$/) do |label|
  assert_equal(false, $api_test.channel_verify_channel(label))
end

Then(/^"([^"]*)" should be the parent channel of "([^"]*)"$/) do |parent, child|
  assert($api_test.channel_is_parent_channel(child, parent))
end

Then(/^channel "([^"]*)" should have attribute "([^"]*)" from type "([^"]*)"$/) do |label, attr, type|
  ret = $api_test.channel_get_channel_details(label)
  assert(ret)
  assert_equal(type, ret[attr].class.to_s)
end

Then(/^channel "([^"]*)" should not have attribute "([^"]*)"$/) do |label, attr|
  ret = $api_test.channel_get_channel_details(label)
  assert(ret)
  assert_equal(false, ret.key?(attr))
end


## activationkey namespace

key = nil

When(/^I create an activation key with id "([^"]*)", description "([^"]*)" and limit of (\d+)$/) do |id, dscr, limit|
  key = @activationkey_api.create_key(id, dscr, '', limit)
  raise if key.nil?
end

Then(/^I should get it listed with a call of activation_key\.list_activation_keys\(\)$/) do
  raise unless @activationkey_api.verify_key(key)
end

When(/^I call activation_key\.list_activation_keys\(\) I should get some$/) do
  raise if @activationkey_api.get_activation_keys_count < 1
end

When(/^I delete the activation key$/) do
  raise unless @activationkey_api.delete_key(key)
  raise if @activationkey_api.verify_key(key)
end

When(/^I add config channels "([^"]*)" to a newly created key$/) do |channel_name|
  raise if @activationkey_api.add_config_channels(key, [channel_name]) < 1
end

When(/^I call activation_key\.set_details\(\) to the key setting as description "([^"]*)"$/) do |description|
  raise unless @activationkey_api.set_details(key, description, '', 10, 'default')
end

Then(/^I have to see them by calling activationkey\.get_details\(\) having as description "([^"]*)"$/) do |description|
  details = @activationkey_api.get_details(key)
  log "Key info for the key details['key']:"
  details.each_pair do |k, v|
    log "  #{k}:#{v}"
  end
  raise unless details['description'] == description
end

# rubocop:disable Metrics/BlockLength
When(/^I create an activation key including custom channels for "([^"]*)" via API$/) do |client|
  # Create a key with the base channel for this client
  id = description = "#{client}_key"
  base_channel = LABEL_BY_BASE_CHANNEL[BASE_CHANNEL_BY_CLIENT[client]]
  key = @activationkey_api.create_key(id, description, base_channel, 100)
  raise if key.nil?

  is_ssh_minion = client.include? 'ssh_minion'
  @activationkey_api.set_details(key, description, base_channel, 100, is_ssh_minion ? 'ssh-push' : 'default')

  # Get the list of child channels for this base channel
  child_channels = $api_test.channel_list_child_channels(base_channel)

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
    @activationkey_api.add_child_channels(key, selected_child_channels)
  rescue API::FaultException => err
    log "The selected child channels can not be included: #{selected_child_channels}. Error: #{err}"
  end
end
# rubocop:enable Metrics/BlockLength


## actionchain namespace

# Listing chains
When(/^I call action_chain.create_chain\(\) with chain label "(.*?)"$/) do |label|
  action_id = @actionchain_api.create_chain(label)
  refute(action_id < 1)
  $chain_label = label
end

When(/^I call action_chain\.list_chains\(\) if label "(.*?)" is there$/) do |label|
  assert_includes(@actionchain_api.list_chains, label)
end

# Deleting chain
Then(/^I delete the action chain$/) do
  begin
    @actionchain_api.delete_chain($chain_label)
  rescue API::FaultException => e
    raise format('deleteChain: API failure, code %s: %s', e.faultCode, e.faultString)
  end
end

Then(/^I delete an action chain, labeled "(.*?)"$/) do |label|
  begin
    @actionchain_api.delete_chain(label)
  rescue API::FaultException => e
    raise format('deleteChain: API failure, code %s: %s', e.faultCode, e.faultString)
  end
end

Then(/^I delete all action chains$/) do
  begin
    @actionchain_api.list_chains.each do |label|
      log "Delete chain: #{label}"
      @actionchain_api.delete_chain(label)
    end
  rescue API::FaultException => e
    raise format('deleteChain: API failure, code %s: %s', e.faultCode, e.faultString)
  end
end

# Renaming chain
Then(/^I call action_chain\.rename_chain\(\) to rename it from "(.*?)" to "(.*?)"$/) do |old_label, new_label|
  @actionchain_api.rename_chain(old_label, new_label)
end

Then(/^there should be a new action chain with the label "(.*?)"$/) do |label|
  assert_includes(@actionchain_api.list_chains, label)
end

Then(/^there should be an action chain with the label "(.*?)"$/) do |label|
  assert_includes(@actionchain_api.list_chains, label)
end

Then(/^there should be no action chain with the label "(.*?)"$/) do |label|
  refute_includes(@actionchain_api.list_chains, label)
end

Then(/^no action chain with the label "(.*?)"$/) do |label|
  refute_includes(@actionchain_api.list_chains, label)
end

# Schedule scenario
When(/^I call action_chain\.add_script_run\(\) with the script "(.*?)"$/) do |script|
  refute(@actionchain_api.add_script_run($client_id, "#!/bin/bash\n" + script, $chain_label) < 1)
end

Then(/^I should be able to see all these actions in the action chain$/) do
  begin
    actions = @actionchain_api.list_chain_actions($chain_label)
    refute_nil(actions)
    log 'Running actions:'
    actions.each do |action|
      log "\t- " + action['label']
    end
  rescue API::FaultException => e
    raise format('Error listChainActions: API failure, code %s: %s', e.faultCode, e.faultString)
  end
end

# Reboot
When(/^I call action_chain\.add_system_reboot\(\)$/) do
  refute(@actionchain_api.add_system_reboot($client_id, $chain_label) < 1)
end

# Packages operations
When(/^I call action_chain\.add_package_install\(\)$/) do
  pkgs = $api_test.system_list_all_installable_packages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(@actionchain_api.add_package_install($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

When(/^I call action_chain\.add_package_removal\(\)$/) do
  pkgs = $api_test.system_list_all_installable_packages($client_id)
  refute(@actionchain_api.add_package_removal($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

When(/^I call action_chain\.add_package_upgrade\(\)$/) do
  pkgs = $api_test.system_list_latest_upgradable_packages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(@actionchain_api.add_package_upgrade($client_id, [pkgs[0]['to_package_id']], $chain_label) < 1)
end

When(/^I call action_chain\.add_package_verify\(\)$/) do
  pkgs = $api_test.system_list_all_installable_packages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute(@actionchain_api.add_package_verify($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

# Manage actions within the action chain
When(/^I call action_chain\.remove_action\(\) on each action within the chain$/) do
  begin
    actions = @actionchain_api.list_chain_actions($chain_label)
    refute_nil(actions)
    actions.each do |action|
      refute(@actionchain_api.remove_action($chain_label, action['id']) < 0)
      log "\t- Removed \"" + action['label'] + '" action'
    end
  rescue API::FaultException => e
    raise format('Error remove_action: API failure, code %s: %s', e.faultCode, e.faultString)
  end
end

Then(/^the current action chain should be empty$/) do
  assert_empty(@actionchain_api.list_chain_actions($chain_label))
end

# Scheduling the action chain
When(/^I schedule the action chain$/) do
  refute(@actionchain_api.schedule_chain($chain_label, DateTime.now) < 0)
end

When(/^I wait until there are no more action chains$/) do
  repeat_until_timeout(message: 'Action Chains still present') do
    break if @actionchain_api.list_chains.empty?
    @actionchain_api.list_chains.each do |label|
      log "Chain still present: #{label}"
    end
    log
    sleep 2
  end
end


## schedule API

def wait_action_complete(actionid, timeout: DEFAULT_TIMEOUT)
  repeat_until_timeout(timeout: timeout, message: 'Action was not found among completed actions') do
    list = @schedule_api.list_completed_actions()
    break if list.any? { |a| a['id'] == actionid }
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
    rescue API::FaultException
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
    log
    sleep 2
  end
end


## powermanagement namespace

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


## audit namespace

Given(/^channel data has already been updated$/) do
  assert_equals(@audit_api.populate_cveserver_channels, 1)
end

When(/^I call audit.list_systems_by_patch_status\(\) with CVE identifier "([^\"]*)"$/) do |cve_identifier|
  @result_list = @audit_api.list_systems_by_patch_status(cve_identifier) || []
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


## configchannel namespace

Then(/^channel "([^"]*)" should exist$/) do |channel|
  assert_equal(1, @configchannel_api.channel_exists(channel))
end

Then(/^channel "([^"]*)" should contain file "([^"]*)"$/) do |channel, file|
  result = @configchannel_api.list_files(channel)
  assert_equal(1, result.count { |item| item['path'] == file })
end

Then(/^"([^"]*)" should be subscribed to channel "([^"]*)"$/) do |host, channel|
  system_name = get_system_name(host)
  result = @configchannel_api.list_subscribed_systems(channel)
  assert_equal(1, result.count { |item| item['name'] == system_name })
end

Then(/^"([^"]*)" should not be subscribed to channel "([^"]*)"$/) do |host, channel|
  system_name = get_system_name(host)
  result = @configchannel_api.list_subscribed_systems(channel)
  assert_equal(0, result.count { |item| item['name'] == system_name })
end

When(/^I create state channel "([^"]*)" via API$/) do |channel|
  @configchannel_api.create_channel(channel, channel, channel, 'state')
end

When(/^I create state channel "([^"]*)" containing "([^"]*)" via API$/) do |channel, contents|
  @configchannel_api.create_channel_with_data(channel, channel, channel, 'state', { 'contents' => contents })
end

When(/^I call config_channel.get_file_revision\(\) with file "([^"]*)", revision "([^"]*)" and channel "([^"]*)" via API$/) do |file_path, revision, channel|
  @get_file_revision_result = @configchannel_api.get_file_revision(channel, file_path, revision.to_i)
end

Then(/^I should get file contents "([^\"]*)"$/) do |contents|
  assert_equal(contents, @get_file_revision_result['contents'])
end

When(/^I add file "([^"]*)" containing "([^"]*)" to channel "([^"]*)"$/) do |file, contents, channel|
  @configchannel_api.create_or_update_path(channel, file, contents)
end

When(/^I deploy all systems registered to channel "([^"]*)"$/) do |channel|
  @configchannel_api.deploy_all_systems(channel)
end

When(/^I delete channel "([^"]*)" via API((?: without error control)?)$/) do |channel, error_control|
  begin
    @configchannel_api.delete_channels([channel])
  rescue API::FaultException => e
    raise format('Error delete_channels: API failure, code %s: %s', e.faultCode, e.faultString) if error_control.empty?
  end
end

When(/^I call system.create_system_profile\(\) with name "([^"]*)" and HW address "([^"]*)"$/) do |name, hw_address|
  profile_id = $api_test.system_create_system_profile(name, 'hwAddress' => hw_address)
  refute_nil(profile_id)
end

When(/^I call system\.create_system_profile\(\) with name "([^"]*)" and hostname "([^"]*)"$/) do |name, hostname|
  profile_id = $api_test.system_create_system_profile(name, 'hostname' => hostname)
  refute_nil(profile_id)
end

When(/^I call system\.list_empty_system_profiles\(\)$/) do
  $output = $api_test.system_list_empty_system_profiles
end

Then(/^"([^"]*)" should be present in the result$/) do |profile_name|
  assert($output.select { |p| p['name'] == profile_name }.count == 1)
end

