# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

SALT_PACKAGES = "salt salt-minion".freeze

Given(/^no Salt packages are installed on remote minion host$/) do
  $minion.run("test -e /usr/bin/zypper && zypper --non-interactive remove -y #{SALT_PACKAGES}", false)
  $minion.run("test -e /usr/bin/yum && yum -y remove #{SALT_PACKAGES}", false)
end

Given(/^remote minion host is not registered in Spacewalk$/) do
  @rpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
  @rpc.login('admin', 'admin')
  sid = @rpc.listSystems.select { |s| s['name'] == ENV['MINION'] }.map { |s| s['id'] }.first
  @rpc.deleteSystem(sid) if sid
  refute_includes(@rpc.listSystems.map { |s| s['id'] }, ENV['MINION'])
end

Then(/^I enter remote minion hostname as "(.*?)"$/) do |hostname|
  step %(I enter "#{ENV['MINION']}" as "#{hostname}")
end

Then(/^I should see remote minion hostname as link$/) do
  step %(I should see a "#{ENV['MINION']}" link)
end

Then(/^I follow remote minion hostname$/) do
  step %(I follow "#{ENV['MINION']}")
end
