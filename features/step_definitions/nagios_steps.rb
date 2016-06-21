# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

When(/^I perform a nagios check patches$/) do
  command = "ssh -o StrictHostKeyChecking=no #{ENV['TESTHOST']} /usr/lib/nagios/plugins/check_suma_patches #{$myhostname}"
  output = `#{command} > /tmp/nagios.out`
end

When(/^I perform a nagios check last event$/) do
  command = "ssh -o StrictHostKeyChecking=no #{ENV['TESTHOST']} /usr/lib/nagios/plugins/check_suma_lastevent #{$myhostname}"
  output = `#{command} > /tmp/nagios.out`
end

When(/^I perform an invalid nagios check patches$/) do
  command = "ssh -o StrictHostKeyChecking=no #{ENV['TESTHOST']} /usr/lib/nagios/plugins/check_suma_patches does.not.exist"
  output = `#{command} > /tmp/nagios.out`
end

Then(/^I should see WARNING: 1 patch pending$/) do
  command = "grep \"WARNING: 1 patch(es) pending\" /tmp/nagios.out 2>&1"
  output, local, remote, code = $client.test_and_store_results_together(command, "root", 600)
  if code != 0
    raise "Nagios check patches failed '#{command}' #{$!}: #{output}"
  end
end

Then(/^I should see Completed: OpenSCAP xccdf scanning scheduled by testing$/) do
  command = "grep \"Completed: OpenSCAP xccdf scanning scheduled by testing\" /tmp/nagios.out 2>&1"
  output, local, remote, code = $client.test_and_store_results_together(command, "root", 600)
  if code != 0
    raise "Nagios check last event failed '#{command}' #{$!}: #{output}"
  end
end

Then(/^I should see an unknown system message$/) do
  command = "grep -i \"^Unknown system:.*does.not.exist\" /tmp/nagios.out 2>&1"
  output, local, remote, code = $client.test_and_store_results_together(command, "root", 600)
  if code != 0
     raise "Nagios check patches for nonexisting system failed '#{command}' #{$!}: #{output}"
  end
end
