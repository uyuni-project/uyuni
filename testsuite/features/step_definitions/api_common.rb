# Copyright (c) 2015-2024 SUSE LLC
# Licensed under the terms of the MIT license.

### This file contains the definitions for all steps concerning the API.

require 'json'
require 'socket'

## system namespace

Given(/^I want to operate on this "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  $client_id = $api_test.system.search_by_name(system_name).first['id']
  refute_nil($client_id, "Could not find system with hostname #{system_name}")
end

When(/^I call system\.bootstrap\(\) on host "([^"]*)" and salt-ssh "([^"]*)"$/) do |host, salt_ssh_enabled|
  system_name = get_system_name(host)
  salt_ssh = (salt_ssh_enabled == 'enabled')
  akey = salt_ssh ? '1-SUSE-SSH-KEY-x86_64' : '1-SUSE-KEY-x86_64'
  result = $api_test.system.bootstrap_system(system_name, akey, salt_ssh)
  assert(result == 1, 'Bootstrap return code not equal to 1.')
end

When(/^I call system\.bootstrap\(\) on unknown host, I should get an API fault$/) do
  exception_thrown = false
  begin
    $api_test.system.bootstrap_system('imprettysureidontexist', '', false)
  rescue SystemCallError
    exception_thrown = true
  end
  assert(exception_thrown, 'Exception must be thrown for non-existing host.')
end

When(/^I call system\.bootstrap\(\) on a Salt minion with saltSSH = true, but with activation key with default contact method, I should get an API fault$/) do
  exception_thrown = false
  begin
    $api_test.system.bootstrap_system(get_target('sle_minion').full_hostname, '1-SUSE-KEY-x86_64', true)
  rescue SystemCallError
    exception_thrown = true
  end
  assert(exception_thrown, 'Exception must be thrown for non-compatible activation keys.')
end

When(/^I schedule a highstate for "([^"]*)" via API$/) do |host|
  system_name = get_system_name(host)
  node_id = $api_test.system.retrieve_server_id(system_name)
  date_high = $api_test.date_now
  $api_test.system.schedule_apply_highstate(node_id, date_high, false)
end

When(/^I unsubscribe "([^"]*)" from configuration channel "([^"]*)"$/) do |host1, channel|
  system_name1 = get_system_name(host1)
  node_id1 = $api_test.system.retrieve_server_id(system_name1)
  $api_test.system.config.remove_channels([node_id1], [channel])
end

When(/^I create a system record$/) do
  dev = { 'name' => 'eth0', 'ip' => '1.1.1.1', 'mac' => '00:22:22:77:EE:CC', 'dnsname' => 'testserver.example.com' }
  $api_test.system.create_system_record('testserver', 'fedora_kickstart_profile_upload', '', 'my test server', [dev])
end

When(/^I create a system record with name "([^"]*)" and kickstart label "([^"]*)"$/) do |name, label|
  dev = { 'name' => 'eth0', 'ip' => '1.1.1.2', 'mac' => '00:22:22:77:EE:DD', 'dnsname' => 'testserver.example.com' }
  $api_test.system.create_system_record(name, label, '', 'my test server', [dev])
end

When(/^I wait for the OpenSCAP audit to finish$/) do
  @sle_id = $api_test.system.retrieve_server_id(get_target('sle_minion').full_hostname)

  repeat_until_timeout(message: 'Process did not complete') do
    scans = $api_test.system.scap.list_xccdf_scans(@sle_id)
    # in the openscap test, we schedule 2 scans
    break if scans.length > 1
  end
end

## user namespace

When(/^I call user\.list_users\(\)$/) do
  @users = $api_test.user.list_users
end

Then(/^I should get at least user "([^"]*)"$/) do |user|
  assert_includes(@users.map { |u| u['login'] }, user)
end

When(/^I call user\.list_roles\(\) on user "([^"]*)"$/) do |user|
  @roles = $api_test.user.list_roles(user)
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
  refute($api_test.user.create(user, 'JamesBond007', 'Hans', 'Mustermann', 'hans.mustermann@suse.com') != 1)
end

When(/^I call user\.add_role\(\) on "([^"]*)" with the role "([^"]*)"$/) do |user, role|
  refute($api_test.user.add_role(user, role) != 1)
end

When(/^I delete user "([^"]*)"$/) do |user|
  $api_test.user.delete(user)
end

When(/^I make sure "([^"]*)" is not present$/) do |user|
  $api_test.user.list_users
           .map { |u| u['login'] }
           .select { |l| l == user }
           .each { $api_test.user.delete(user) }
end

When(/^I call user\.remove_role\(\) on "([^"]*)" with the role "([^"]*)"$/) do |luser, rolename|
  refute($api_test.user.remove_role(luser, rolename) != 1)
end

## channel namespace

When(/^I create a repo with label "([^"]*)" and url$/) do |label|
  url = "http://#{get_target('server').full_hostname}/pub/AnotherRepo/"
  assert($api_test.channel.software.create_repo(label, url))
end

When(/^I associate repo "([^"]*)" with channel "([^"]*)"$/) do |repo_label, channel_label|
  assert($api_test.channel.software.associate_repo(channel_label, repo_label))
end

When(/^I create the following channels:$/) do |table|
  channels = table.hashes
  channels.each do |ch|
    assert_equal(1, $api_test.channel.software.create(ch['LABEL'], ch['NAME'], ch['SUMMARY'], ch['ARCH'], ch['PARENT']))
  end
end

When(/^I delete the software channel with label "([^"]*)"$/) do |label|
  assert_equal(1, $api_test.channel.software.delete(label))
end

When(/^I delete the repo with label "([^"]*)"$/) do |label|
  assert_equal(1, $api_test.channel.software.remove_repo(label))
end

Then(/^something should get listed with a call of listSoftwareChannels$/) do
  assert_equal(false, $api_test.channel.get_software_channels_count < 1)
end

Then(/^"([^"]*)" should get listed with a call of listSoftwareChannels$/) do |label|
  assert($api_test.channel.verify_channel(label))
end

Then(/^"([^"]*)" should not get listed with a call of listSoftwareChannels$/) do |label|
  assert_equal(false, $api_test.channel.verify_channel(label))
end

Then(/^"([^"]*)" should be the parent channel of "([^"]*)"$/) do |parent, child|
  assert($api_test.channel.software.parent_channel?(child, parent))
end

Then(/^channel "([^"]*)" should have attribute "([^"]*)" that is a date$/) do |label, attr|
  ret = $api_test.channel.software.get_details(label)
  assert(ret)
  assert $api_test.date?(ret[attr])
end

Then(/^channel "([^"]*)" should not have attribute "([^"]*)"$/) do |label, attr|
  ret = $api_test.channel.software.get_details(label)
  assert(ret)
  assert_equal(false, ret.key?(attr))
end

Then(/^channel "([^"]*)" should be (enabled|disabled) on "([^"]*)"$/) do |channel, state, host|
  node = get_target(host)
  system_id = get_system_id(node)

  channels = $api_test.channel.software.list_system_channels(system_id)

  assert_equal(state == 'enabled', channels.include?(channel))
end

Then(/^"(\d+)" channels should be enabled on "([^"]*)"$/) do |count, host|
  node = get_target(host)
  system_id = get_system_id(node)

  channels = $api_test.channel.software.list_system_channels(system_id)

  assert_equal(count, channels.size)
end

Then(/^"(\d+)" channels with prefix "([^"]*)" should be enabled on "([^"]*)"$/) do |count, prefix, host|
  node = get_target(host)
  system_id = get_system_id(node)

  channels = $api_test.channel.software.list_system_channels(system_id)

  assert_equal(count, channels.select { |channel| channel.start_with?(prefix) }.size)
end

## activationkey namespace

Then(/^I should get some activation keys$/) do
  raise ScriptError if $api_test.activationkey.get_activation_keys_count < 1
end

When(/^I create an activation key with id "([^"]*)", description "([^"]*)" and limit of (\d+)$/) do |id, dscr, limit|
  key = $api_test.activationkey.create(id, dscr, '', limit.to_i)
  raise ScriptError, 'Key creation failed' if key.nil?
  raise ScriptError, 'Bad key name' if key != "1-#{id}"
end

Then(/^I should get the new activation key "([^"]*)"$/) do |activation_key|
  raise ScriptError unless $api_test.activationkey.verify(activation_key)
end

When(/^I delete the activation key "([^"]*)"$/) do |activation_key|
  raise ScriptError unless $api_test.activationkey.delete(activation_key)
  raise ScriptError if $api_test.activationkey.verify(activation_key)
end

When(/^I set the description of the activation key "([^"]*)" to "([^"]*)"$/) do |activation_key, description|
  raise RuntimeError unless $api_test.activationkey.set_details(activation_key, description, '', 10, 'default')
end

Then(/^I get the description "([^"]*)" for the activation key "([^"]*)"$/) do |description, activation_key|
  details = $api_test.activationkey.get_details(activation_key)
  log 'Key details:'
  details.each_pair do |k, v|
    log "  #{k}: #{v}"
  end
  log
  raise ScriptError unless details['description'] == description
end

When(/^I create an activation key including custom channels for "([^"]*)" via API$/) do |client|
  # Create a key with the base channel for this client
  id = description = "#{client}_key"
  base_channel = BASE_CHANNEL_BY_CLIENT[product][client]
  base_channel_label = LABEL_BY_BASE_CHANNEL[product][base_channel]
  key = $api_test.activationkey.create(id, description, base_channel_label, 100)
  raise StandardError, 'Error creating activation key via the API' if key.nil?

  $stdout.puts "Activation key #{key} created" unless key.nil?

  is_ssh_minion = client.include? 'ssh_minion'
  $api_test.activationkey.set_details(key, description, base_channel_label, 100, is_ssh_minion ? 'ssh-push' : 'default')
  entitlements = client.include?('buildhost') ? ['osimage_build_host'] : ''
  $api_test.activationkey.set_entitlement(key, entitlements) unless entitlements.empty?

  # Get the list of child channels for this base channel
  child_channels = $api_test.channel.software.list_child_channels(base_channel_label)

  # Filter out the custom channels
  # This is needed because we might have both a traditional custom channel and a Salt custom channel
  child_channels.reject! { |channel| channel.include? 'custom_channel' }

  # Re-add the desired custom channel
  # This too can go away when we get rid of traditional clients for good
  client.sub! 'ssh_minion', 'minion'
  client.sub! 'buildhost', 'minion'
  client.sub! 'terminal', 'minion'
  custom_channel =
    if client.include? 'alma8'
      'no-appstream-alma-8-result-custom_channel_alma8_minion'
    elsif client.include? 'alma9'
      'no-appstream-alma-9-result-custom_channel_alma9_minion'
    elsif client.include? 'liberty9'
      'no-appstream-liberty-9-result-custom_channel_liberty9_minion'
    elsif client.include? 'oracle9'
      'no-appstream-oracle-9-result-custom_channel_oracle9_minion'
    elsif client.include? 'rocky8'
      'no-appstream-8-result-custom_channel_rocky8_minion'
    elsif client.include? 'rocky9'
      'no-appstream-9-result-custom_channel_rocky9_minion'
    else
      "custom_channel_#{client}"
    end
  child_channels.push(custom_channel)
  $stdout.puts "Child_channels for #{key}: <#{child_channels}>"

  # Add child channels to the key
  $api_test.activationkey.add_child_channels(key, child_channels)
end

## actionchain namespace

When(/^I call actionchain\.create_chain\(\) with chain label "(.*?)"$/) do |label|
  action_id = $api_test.actionchain.create_chain(label)
  refute(action_id < 1)
  $chain_label = label
end

When(/^I call actionchain\.list_chains\(\) if label "(.*?)" is there$/) do |label|
  assert_includes($api_test.actionchain.list_chains, label)
end

When(/^I delete the action chain$/) do
  $api_test.actionchain.delete_chain($chain_label)
end

When(/^I delete an action chain, labeled "(.*?)"$/) do |label|
  $api_test.actionchain.delete_chain(label)
end

When(/^I delete all action chains$/) do
  $api_test.actionchain.list_chains.each do |label|
    log "Delete chain: #{label}"
    $api_test.actionchain.delete_chain(label)
  end
end

# Renaming chain
Then(/^I call actionchain\.rename_chain\(\) to rename it from "(.*?)" to "(.*?)"$/) do |old_label, new_label|
  $api_test.actionchain.rename_chain(old_label, new_label)
end

Then(/^there should be a new action chain with the label "(.*?)"$/) do |label|
  assert_includes($api_test.actionchain.list_chains, label)
end

Then(/^there should be no action chain with the label "(.*?)"$/) do |label|
  refute_includes($api_test.actionchain.list_chains, label)
end

Then(/^no action chain with the label "(.*?)"$/) do |label|
  refute_includes($api_test.actionchain.list_chains, label)
end

# Schedule scenario
When(/^I call actionchain\.add_script_run\(\) with the script "(.*?)"$/) do |script|
  refute($api_test.actionchain.add_script_run($client_id, $chain_label, 'root', 'root', 300, "#!/bin/bash\n#{script}") < 1)
end

Then(/^I should be able to see all these actions in the action chain$/) do
  actions = $api_test.actionchain.list_chain_actions($chain_label)
  refute_nil(actions)
  log 'Running actions:'
  actions.each do |action|
    log "\t- #{action['label']}"
  end
end

# Reboot
When(/^I call actionchain\.add_system_reboot\(\)$/) do
  refute($api_test.actionchain.add_system_reboot($client_id, $chain_label) < 1)
end

# Packages operations
When(/^I call actionchain\.add_package_install\(\)$/) do
  pkgs = $api_test.system.list_all_installable_packages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute($api_test.actionchain.add_package_install($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

When(/^I call actionchain\.add_package_removal\(\)$/) do
  pkgs = $api_test.system.list_all_installable_packages($client_id)
  refute($api_test.actionchain.add_package_removal($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

When(/^I call actionchain\.add_package_upgrade\(\)$/) do
  pkgs = $api_test.system.list_latest_upgradable_packages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute($api_test.actionchain.add_package_upgrade($client_id, [pkgs[0]['to_package_id']], $chain_label) < 1)
end

When(/^I call actionchain\.add_package_verify\(\)$/) do
  pkgs = $api_test.system.list_all_installable_packages($client_id)
  refute_nil(pkgs)
  refute_empty(pkgs)
  refute($api_test.actionchain.add_package_verify($client_id, [pkgs[0]['id']], $chain_label) < 1)
end

# Manage actions within the action chain
When(/^I call actionchain\.remove_action\(\) on each action within the chain$/) do
  actions = $api_test.actionchain.list_chain_actions($chain_label)
  refute_nil(actions)
  actions.each do |action|
    refute($api_test.actionchain.remove_action($chain_label, action['id']).negative?)
    log "\t- Removed \"#{action['label']}\" action"
  end
end

Then(/^the current action chain should be empty$/) do
  assert_empty($api_test.actionchain.list_chain_actions($chain_label))
end

# Scheduling the action chain
When(/^I schedule the action chain$/) do
  refute($api_test.actionchain.schedule_chain($chain_label, DateTime.now).negative?)
end

When(/^I wait until there are no more action chains$/) do
  repeat_until_timeout(message: 'Action Chains still present') do
    break if $api_test.actionchain.list_chains.empty?

    $api_test.actionchain.list_chains.each do |label|
      log "Chain still present: #{label}"
    end
    log
    sleep 2
  end
end

## schedule API

Then(/^I should see scheduled action, called "(.*?)"$/) do |label|
  assert_includes($api_test.schedule.list_in_progress_actions.map { |a| a['name'] }, label)
end

Then(/^I cancel all scheduled actions$/) do
  actions =
    $api_test.schedule.list_in_progress_actions.reject do |action|
      action['prerequisite']
    end

  actions.each do |action|
    log "\t- Try to cancel \"#{action['name']}\" action"
    begin
      $api_test.schedule.cancel_actions([action['id']])
    rescue StandardError
      $api_test.schedule.list_in_progress_systems(action['id']).each do |system|
        $api_test.schedule.fail_system_action(system['server_id'], action['id'])
      end
    end
    log "\t- Removed \"#{action['name']}\" action"
  end
end

Then(/^I wait until there are no more scheduled actions$/) do
  repeat_until_timeout(message: 'Scheduled actions still present') do
    break if $api_test.schedule.list_in_progress_actions.empty?

    $api_test.schedule.list_in_progress_actions.each do |action|
      log "Action still in progress: #{action}"
    end
    log
    sleep 2
  end
end

## provisioning.powermanagement namespace

When(/^I fetch power management values$/) do
  @powermgmt_result = $api_test.system.provisioning.powermanagement.get_details($client_id)
end

Then(/^power management results should have "([^"]*)" for "([^"]*)"$/) do |value, hkey|
  assert_equal(value, @powermgmt_result[hkey])
end

Then(/^I set power management value "([^"]*)" for "([^"]*)"$/) do |value, hkey|
  $api_test.system.provisioning.powermanagement.set_details($client_id, { hkey => value })
end

Then(/^I turn power on$/) do
  $api_test.system.provisioning.powermanagement.power_on($client_id)
end

Then(/^I turn power off$/) do
  $api_test.system.provisioning.powermanagement.power_off($client_id)
end

Then(/^I do power management reboot$/) do
  $api_test.system.provisioning.powermanagement.reboot($client_id)
end

Then(/^the power status is "([^"]*)"$/) do |estat|
  stat = $api_test.system.provisioning.powermanagement.get_status($client_id)
  assert(stat) if estat == 'on'
  assert(!stat) if estat == 'off'
end

## audit namespace

When(/^I call audit\.list_systems_by_patch_status\(\) with CVE identifier "([^"]*)"$/) do |cve_identifier|
  @result_list = $api_test.audit.list_systems_by_patch_status(cve_identifier) || []
  log "Result list: #{@result_list}"
end

Then(/^I should get status "([^"]+)" for system "([0-9]+)"$/) do |status, system|
  @result = @result_list.select { |item| item['system_id'] == system.to_i }
  refute_empty(@result)
  @result = @result[0]
  assert_equal(status, @result['patch_status'])
end

Then(/^I should get status "([^"]+)" for "([^"]+)"$/) do |status, host|
  node = get_target(host)
  step %(I should get status "#{status}" for system "#{get_system_id(node)}")
end

Then(/^I should get the "([^"]*)" channel label$/) do |channel_label|
  assert(@result['channel_labels'].include?(channel_label))
end

Then(/^I should get the "([^"]*)" patch$/) do |patch|
  log "result: #{@result}"
  assert(@result['errata_advisories'].include?(patch))
end

## configchannel namespace

Then(/^channel "([^"]*)" should exist$/) do |channel|
  assert_equal(1, $api_test.configchannel.channel_exists(channel))
end

Then(/^channel "([^"]*)" should contain file "([^"]*)"$/) do |channel, file|
  result = $api_test.configchannel.list_files(channel)
  assert_equal(1, result.count { |item| item['path'] == file })
end

Then(/^"([^"]*)" should be subscribed to channel "([^"]*)"$/) do |host, channel|
  system_name = get_system_name(host)
  result = $api_test.configchannel.list_subscribed_systems(channel)
  assert_equal(1, result.count { |item| item['name'] == system_name })
end

Then(/^"([^"]*)" should not be subscribed to channel "([^"]*)"$/) do |host, channel|
  system_name = get_system_name(host)
  result = $api_test.configchannel.list_subscribed_systems(channel)
  assert_equal(0, result.count { |item| item['name'] == system_name })
end

When(/^I create state channel "([^"]*)" via API$/) do |channel|
  $api_test.configchannel.create(channel, channel, channel, 'state')
end

When(/^I create state channel "([^"]*)" containing "([^"]*)" via API$/) do |channel, contents|
  $api_test.configchannel.create_with_pathinfo(channel, channel, channel, 'state', { 'contents' => contents })
end

When(/^I call configchannel.get_file_revision\(\) with file "([^"]*)", revision "([^"]*)" and channel "([^"]*)" via API$/) do |file_path, revision, channel|
  @get_file_revision_result = $api_test.configchannel.get_file_revision(channel, file_path, revision.to_i)
end

Then(/^I should get file contents "([^"]*)"$/) do |contents|
  assert_equal(contents, @get_file_revision_result['contents'])
end

When(/^I add file "([^"]*)" containing "([^"]*)" to channel "([^"]*)"$/) do |file, contents, channel|
  $api_test.configchannel.create_or_update_path(channel, file, contents)
end

When(/^I deploy all systems registered to channel "([^"]*)"$/) do |channel|
  $api_test.configchannel.deploy_all_systems(channel)
end

When(/^I delete channel "([^"]*)" via API((?: without error control)?)$/) do |channel, error_control|
  $api_test.configchannel.delete_channels([channel])
rescue StandardError
  raise SystemCallError, 'Error deleting channel' if error_control.empty?
end

When(/^I call system.create_system_profile\(\) with name "([^"]*)" and HW address "([^"]*)"$/) do |name, hw_address|
  profile_id = $api_test.system.create_system_profile(name, 'hwAddress' => hw_address)
  refute_nil(profile_id)
end

When(/^I call system\.create_system_profile\(\) with name "([^"]*)" and hostname "([^"]*)"$/) do |name, hostname|
  profile_id = $api_test.system.create_system_profile(name, 'hostname' => hostname)
  refute_nil(profile_id)
end

When(/^I call system\.list_empty_system_profiles\(\)$/) do
  $output = $api_test.system.list_empty_system_profiles
end

Then(/^"([^"]*)" should be present in the result$/) do |profile_name|
  assert($output.select { |p| p['name'] == profile_name }.count == 1)
end

When(/^I create and modify the kickstart system "([^"]*)" with kickstart label "([^"]*)" and hostname "([^"]*)" via XML-RPC$/) do |name, kslabel, hostname, values|
  # even though it should not happen during a testsuite run, it is useful to know when debugging that
  # this call will raise a SystemCallError if matching systems already exist, the Error message will include a list of the matchings system IDs
  sid = $api_test.system.create_system_profile(name, 'hostname' => hostname)
  $stdout.puts "system_id: #{sid}"

  $api_test.system.create_system_record_with_sid(sid, kslabel)
  # this works only with a 2 column table where the key is in the left column
  variables = values.rows_hash
  $api_test.system.set_variables(sid, variables)
end

When(/^I create a kickstart tree via the API$/) do
  $api_test.kickstart.tree.create_distro('fedora_kickstart_distro_api', '/var/autoinstall/Fedora_12_i386/', 'fake-base-channel-rh-like', 'fedora18')
end

When(/^I create a kickstart tree with kernel options via the API$/) do
  $api_test.kickstart.tree.create_distro_w_kernel_options('fedora_kickstart_distro_kernel_api', '/var/autoinstall/Fedora_12_i386/', 'fake-base-channel-rh-like', 'fedora18', 'self_update=0', 'self_update=1')
end

When(/^I update a kickstart tree via the API$/) do
  $api_test.kickstart.tree.update_distro('fedora_kickstart_distro_api', '/var/autoinstall/Fedora_12_i386/', 'fake-base-channel-rh-like', 'generic_rpm', 'self_update=0', 'self_update=1')
end
