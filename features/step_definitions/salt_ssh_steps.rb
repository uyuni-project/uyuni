# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

SALT_PACKAGES = "salt salt-minion".freeze

Given(/^no Salt packages are installed on remote "(.*)"$/) do |host|
  if host == "ssh-minion"
    $ssh_minion.run("test -e /usr/bin/zypper && zypper --non-interactive remove -y #{SALT_PACKAGES}", false)
    $ssh_minion.run("rm -Rf /etc/salt", false)
  end
  if host == "centos"
    $ceos_minion.run("test -e /usr/bin/yum && yum -y remove #{SALT_PACKAGES}", false)
  end
end

Given(/^remote minion host is not registered in Spacewalk$/) do
  @rpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
  @rpc.login('admin', 'admin')
  sid = @rpc.listSystems.select { |s| s['name'] == ENV['SSHMINION'] }.map { |s| s['id'] }.first
  @rpc.deleteSystem(sid) if sid
  refute_includes(@rpc.listSystems.map { |s| s['id'] }, ENV['SSHMINION'])
end

Given(/^centos minion is not registered in Spacewalk$/) do
  @rpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
  @rpc.login('admin', 'admin')
  sid = @rpc.listSystems.select { |s| s['name'] == ENV['CENTOSMINION'] }.map { |s| s['id'] }.first
  @rpc.deleteSystem(sid) if sid
  refute_includes(@rpc.listSystems.map { |s| s['id'] }, ENV['CENTOSMINION'])
end

Then(/^I enter remote ssh-minion hostname as "(.*?)"$/) do |hostname|
  step %(I enter "#{ENV['SSHMINION']}" as "#{hostname}")
end

Then(/^I should see remote ssh-minion hostname as link$/) do
  step %(I should see a "#{ENV['SSHMINION']}" link)
end

Then(/^I should see centos ssh-minion hostname as link$/) do
  step %(I should see a "#{ENV['CENTOSMINION']}" link)
end

Then(/^I follow centos ssh-minion hostname$/) do
  step %(I follow "#{ENV['CENTOSMINION']}")
end

Then(/^I follow remote ssh-minion hostname$/) do
  step %(I follow "#{ENV['SSHMINION']}")
end
