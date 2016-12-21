# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

SALT_PACKAGES = "salt salt-minion".freeze

Given(/^no Salt packages are installed on remote minion host$/) do
  $ssh_minion.run("test -e /usr/bin/zypper && zypper --non-interactive remove -y #{SALT_PACKAGES}", false)
  $ceos_minion.run("test -e /usr/bin/yum && yum -y remove #{SALT_PACKAGES}", false)
end

Given(/^remote minion host is not registered in Spacewalk$/) do
  @rpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
  @rpc.login('admin', 'admin')
  sid = @rpc.listSystems.select { |s| s['name'] == ENV['SSHMINION'] }.map { |s| s['id'] }.first
  @rpc.deleteSystem(sid) if sid
  refute_includes(@rpc.listSystems.map { |s| s['id'] }, ENV['SSHMINION'])
end

Then(/^I enter remote ssh-minion hostname as "(.*?)"$/) do |hostname|
  step %(I enter "#{ENV['SSHMINION']}" as "#{hostname}")
end

Then(/^I should see remote ssh-minion hostname as link$/) do
  step %(I should see a "#{ENV['SSHMINION']}" link)
end

Then(/^I follow remote ssh-minion hostname$/) do
  step %(I follow "#{ENV['SSHMINION']}")
end
