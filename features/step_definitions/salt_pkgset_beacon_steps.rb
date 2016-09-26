# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Then(/^I manually install the "([^"]*)" package in the minion$/) do |package|
  if file_exist($minion, "/usr/bin/zypper").zero?
    cmd = "zypper --non-interactive install -y #{package}"
  elsif file_exist($minion, "/usr/bin/yum").zero?
    cmd = "yum -y install #{package}"
  else
    fail "not found: zypper or yum"
  end
  $minion.run(cmd, false)
end

Then(/^I manually remove the "([^"]*)" package in the minion$/) do |package|
  if file_exist($minion, "/usr/bin/zypper").zero?
    cmd = "zypper --non-interactive remove -y #{package}"
  elsif file_exist($minion, "/usr/bin/yum").zero?
    cmd = "yum -y remove #{package}"
  else
    fail "not found: zypper or yum"
  end
  $minion.run(cmd, false)
end
