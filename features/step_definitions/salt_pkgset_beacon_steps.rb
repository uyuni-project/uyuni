# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Then(/^I manually install the "([^"]*)" package in the minion$/) do |package|
  if File.exist?("/usr/bin/zypper")
    `zypper --non-interactive install -y #{package}`
  elsif File.exist?("/usr/bin/yum")
    `yum -y install #{package}`
  else
    fail "not found: zypper or yum"
  end
end

Then(/^I manually remove the "([^"]*)" package in the minion$/) do |package|
  if File.exist?("/usr/bin/zypper")
    `zypper --non-interactive remove -y #{package}`
  elsif File.exist?("/usr/bin/yum")
    `yum -y remove #{package}`
  else
    fail "not found: zypper or yum"
  end
end
