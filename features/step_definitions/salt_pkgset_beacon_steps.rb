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

Then(/^I click on the css "(.*)" until page does not contain "([^"]*)" text$/) do |css, arg1|
  not_found = false
  begin
    Timeout.timeout(30) do
      loop do
        unless page.has_content?(debrand_string(arg1))
          not_found = true
          break
        end
        find(css).click
      end
    end
  rescue Timeout::Error
    raise "'#{arg1}' cannot be found after several tries"
  end
  fail unless not_found
end
