# Copyright (c) 2015-2022 SUSE LLC
# Licensed under the terms of the MIT license.

require 'json'
require 'socket'

$api_test = $product == 'Uyuni' ? ApiTestHttp.new($server.full_hostname) : ApiTestXmlrpc.new($server.full_hostname)

## auth namespace

When(/^I am logged in API as user "([^"]*)" and password "([^"]*)"$/) do |user, password|
  $api_test.auth.login(user, password)
end

When(/^I logout from API$/) do
  $api_test.auth.logout
end

## system namespace

Given(/^I want to operate on this "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  $client_id = $api_test.system.search_by_name(system_name).first['id']
  refute_nil($client_id, "Could not find system with hostname #{system_name}")
end

When(/^I call system\.list_systems\(\), I should get a list of them$/) do
  # This also assumes the test is called *after* the regular test.
  servers = $api_test.system.list_systems
  assert(servers.!empty?, "Expect: 'number of system' > 0, but found only '#{servers.length}' servers")
end

When(/^I call system\.bootstrap\(\) on host "([^"]*)" and salt\-ssh "([^"]*)"$/) do |host, salt_ssh_enabled|
  system_name = get_system_name(host)
  salt_ssh = (salt_ssh_enabled == 'enabled')
  akey = salt_ssh ? '1-ssh_minion_key' : '1-sle_minion_key'
  result = $api_test.system.bootstrap_system(system_name, akey, salt_ssh)
  assert(result == 1, 'Bootstrap return code not equal to 1.')
end

When(/^I call system\.bootstrap\(\) on unknown host, I should get an API fault$/) do
  exception_thrown = false
  begin
    $api_test.system.bootstrap_system('imprettysureidontexist', '', false)
  rescue
    exception_thrown = true
  end
  assert(exception_thrown, 'Exception must be thrown for non-existing host.')
end

When(/^I call system\.bootstrap\(\) on a Salt minion with saltSSH = true, \
but with activation key with default contact method, I should get an API fault$/) do
  exception_thrown = false
  begin
    $api_test.system.bootstrap_system($minion.full_hostname, '1-ssh_minion_key', true)
  rescue
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
  $api_test.system.config.remove_channels([ node_id1 ], [ channel ])
end

When(/^I unsubscribe "([^"]*)" and "([^"]*)" from configuration channel "([^"]*)"$/) do |host1, host2, channel|
  steps %(
      When I unsubscribe "#{host1}" from configuration channel "#{channel}"
      And I unsubscribe "#{host2}" from configuration channel "#{channel}"
  )
end

When(/^I create a system record$/) do
  dev = { 'name' => 'eth0', 'ip' => '1.1.1.1', 'mac' => '00:22:22:77:EE:CC', 'dnsname' => 'testserver.example.com' }
  $api_test.system.create_system_record('testserver', 'fedora_kickstart_profile_upload', '', 'my test server', [dev])
end

When(/^I wait for the OpenSCAP audit to finish$/) do
  @sle_id = $api_test.system.retrieve_server_id($minion.full_hostname)
  begin
    repeat_until_timeout(message: 'Process did not complete') do
      scans = $api_test.system.scap.list_xccdf_scans(@sle_id)
      # in the openscap test, we schedule 2 scans
      break if scans.length > 1
    end
  end
end

When(/^I refresh the packages on traditional "([^"]*)" through API$/) do |host|
  node = get_target(host)
  node_id = $api_test.system.retrieve_server_id(node.full_hostname)
  date_schedule_now = $api_test.date_now

  id_refresh = $api_test.system.schedule_package_refresh(node_id, date_schedule_now)
  node.run('rhn_check -vvv')
  wait_action_complete(id_refresh, timeout: 600)
end

When(/^I run a script on traditional "([^"]*)" through API$/) do |host|
  node = get_target(host)
  node_id = $api_test.system.retrieve_server_id(node.full_hostname)
  date_schedule_now = $api_test.date_now
  script = "#! /usr/bin/bash \n uptime && ls"

  id_script = $api_test.system.schedule_script_run(node_id, 'root', 'root', 500, script, date_schedule_now)
  node.run('rhn_check -vvv')
  wait_action_complete(id_script)
end

When(/^I reboot traditional "([^"]*)" through API$/) do |host|
  node = get_target(host)
  node_id = $api_test.system.retrieve_server_id(node.full_hostname)
  date_schedule_now = $api_test.date_now

  $api_test.system.schedule_reboot(node_id, date_schedule_now)
  node.run('rhn_check -vvv')
  reboot_timeout = 400
  check_shutdown(node.full_hostname, reboot_timeout)
  check_restart(node.full_hostname, node, reboot_timeout)

  $api_test.schedule.list_failed_actions.each do |action|
    systems = $api_test.schedule.list_failed_systems(action['id'])
    raise if systems.all? { |system| system['server_id'] == node_id }
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
  url = "http://#{$server.full_hostname}/pub/AnotherRepo/"
  assert($api_test.channel.software.create_repo(label, url))
end

When(/^I associate repo "([^"]*)" with channel "([^"]*)"$/) do |repo_label, channel_label|
  assert($api_test.channel.software.associate_repo(channel_label, repo_label))
end

When(/^I create the following channels:$/) do |table|
  channels = table.hashes
  channels.each do |ch|
    assert_equal(1,
      $api_test.channel.software.create(
        ch['LABEL'], ch['NAME'], ch['SUMMARY'], ch['ARCH'], ch['PARENT']
      )
    )
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
  asser