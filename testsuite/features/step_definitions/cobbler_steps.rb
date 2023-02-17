# Copyright (c) 2023 SUSE LLC.
# Licensed under the terms of the MIT license.

### This file contains the definitions for all steps concerning cobbler

# Cobbler daemon

Given(/^cobblerd is running$/) do
  ct = CobblerTest.new
  raise 'cobblerd is not running' unless ct.running?
end

# Service

When(/^I restart cobbler on the server$/) do
  $server.run('systemctl restart cobblerd.service')
end

When(/^I restart the spacewalk service$/) do
  $server.run('spacewalk-service restart')
end

When(/^I shutdown the spacewalk service$/) do
  $server.run('spacewalk-service stop')
end

Then(/^the cobbler report should contain "([^"]*)" for "([^"]*)"$/) do |text, host|
  node = get_target(host)
  output, _code = $server.run("cobbler system report --name #{node.full_hostname}:1", check_errors: false)
  raise "Not found:\n#{output}" unless output.include?(text)
end

Then(/^the cobbler report should contain "([^"]*)" for cobbler system name "([^"]*)"$/) do |text, name|
  output, _code = $server.run("cobbler system report --name #{name}", check_errors: false)
  raise "Not found:\n#{output}" unless output.include?(text)
end

# tftp

When(/^I synchronize the tftp configuration on the proxy with the server$/) do
  out, _code = $server.run('cobbler sync')
  raise 'cobbler sync failed' if out.include? 'Push failed'
end

When(/^I copy autoinstall mocked files on server$/) do
  target_dirs = '/var/autoinstall/Fedora_12_i386/images/pxeboot /var/autoinstall/SLES15-SP3-x86_64/DVD1/boot/x86_64/loader /var/autoinstall/mock'
  $server.run("mkdir -p #{target_dirs}")
  base_dir = File.dirname(__FILE__) + '/../upload_files/autoinstall/cobbler/'
  source_dir = '/var/autoinstall/'
  return_codes = []
  return_codes << file_inject($server, base_dir + 'fedora12/vmlinuz', source_dir + 'Fedora_12_i386/images/pxeboot/vmlinuz')
  return_codes << file_inject($server, base_dir + 'fedora12/initrd.img', source_dir + 'Fedora_12_i386/images/pxeboot/initrd.img')
  return_codes << file_inject($server, base_dir + 'mock/empty.xml', source_dir + 'mock/empty.xml')
  return_codes << file_inject($server, base_dir + 'sles15sp3/initrd', source_dir + 'SLES15-SP3-x86_64/DVD1/boot/x86_64/loader/initrd')
  return_codes << file_inject($server, base_dir + 'sles15sp3/linux', source_dir + 'SLES15-SP3-x86_64/DVD1/boot/x86_64/loader/linux')
  raise 'File injection failed' unless return_codes.all?(&:zero?)
end

# systemspage

When(/^I create distro "([^"]*)" as user "([^"]*)" with password "([^"]*)"$/) do |distro, user, pwd|
  ct = CobblerTest.new
  ct.login(user, pwd)
  raise 'distro ' + distro + ' already exists' if ct.distro_exists(distro)

  ct.distro_create(distro, '/var/autoinstall/SLES15-SP3-x86_64/DVD1/boot/x86_64/loader/linux', '/var/autoinstall/SLES15-SP3-x86_64/DVD1/boot/x86_64/loader/initrd')
end

When(/^I trigger cobbler system record$/) do
  # not for SSH-push traditional client
  space = 'spacecmd -u admin -p admin'
  host = $client.full_hostname
  $server.run("#{space} clear_caches")
  out, _code = $server.run("#{space} system_details #{host}")
  unless out.include? 'ssh-push-tunnel'
    # normal traditional client
    steps %(
      Given I am authorized as "testing" with password "testing"
      And I am on the Systems overview page of this "sle_client"
      And I follow "Provisioning"
      And I click on "Create PXE installation configuration"
      And I click on "Continue"
      And I wait until file "/srv/tftpboot/pxelinux.cfg/01-*" contains "ks=" on server
    )
  end
end

Given(/^distro "([^"]*)" exists$/) do |distro|
  ct = CobblerTest.new
  raise 'distro ' + distro + ' does not exist' unless ct.distro_exists(distro)
end

When(/^I create profile "([^"]*)" as user "([^"]*)" with password "([^"]*)"$/) do |arg1, arg2, arg3|
  ct = CobblerTest.new
  ct.login(arg2, arg3)
  raise 'profile ' + arg1 + ' already exists' if ct.profile_exists(arg1)

  ct.profile_create('testprofile', 'testdistro', '/var/autoinstall/mock/empty.xml')
end

When(/^I remove kickstart profiles and distros$/) do
  host = $server.full_hostname
  $api_test.auth.login('admin', 'admin')
  # -------------------------------
  # Cleanup kickstart distros and their profiles, if any.

  # Get all distributions: created from UI or from API.
  distros = $server.run('cobbler distro list')[0].split

  # The name of distros created in the UI has the form: distro_label + suffix
  user_details = $api_test.user.get_details('testing')
  suffix = ":#{user_details['org_id']}:#{user_details['org_name'].delete(' ')}"

  distros_ui = distros.select { |distro| distro.end_with? suffix }.map { |distro| distro.split(':')[0] }
  distros_api = distros.reject { |distro| distro.end_with? suffix }
  distros_ui.each { |distro| $api_test.kickstart.tree.delete_tree_and_profiles(distro) }
  # -------------------------------
  # Remove profiles and distros created with the API.

  # We have already deleted the profiles from the UI; delete all the remaning ones.
  profiles = $server.run('cobbler profile list')[0].split
  profiles.each { |profile| $server.run("cobbler profile remove --name '#{profile}'") }
  distros_api.each { |distro| $server.run("cobbler distro remove --name '#{distro}'") }
  $api_test.auth.logout
end

