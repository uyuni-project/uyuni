# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

When(/^I perform a nagios check patches$/) do
  command = "/usr/lib/nagios/plugins/check_suma_patches #{$client_hostname} > /tmp/nagios.out"
  $server.run(command, false, 600, 'root')
end

When(/^I perform a nagios check last event$/) do
  command = "/usr/lib/nagios/plugins/check_suma_lastevent #{$client_hostname} > /tmp/nagios.out"
  $server.run(command, false, 600, 'root')
end

When(/^I perform an invalid nagios check patches$/) do
  command = "/usr/lib/nagios/plugins/check_suma_patches does.not.exist > /tmp/nagios.out"
  $server.run(command, false, 600, 'root')
end

Then(/^I should see WARNING: 1 patch pending$/) do
  command = "grep \"WARNING: 1 patch(es) pending\" /tmp/nagios.out"
  $server.run(command, true, 600, 'root')
end

Then(/^I should see Completed: OpenSCAP xccdf scanning scheduled by testing$/) do
  command = "grep \"Completed: OpenSCAP xccdf scanning scheduled by testing\" /tmp/nagios.out"
  $server.run(command, true, 600, 'root')
end

Then(/^I should see an unknown system message$/) do
  command = "grep -i \"^Unknown system:.*does.not.exist\" /tmp/nagios.out 2>&1"
  $server.run(command, true, 600, 'root')
end
