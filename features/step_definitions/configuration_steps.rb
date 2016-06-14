# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Initial step for channel testing
#
Given(/^I am testing configuration$/) do
  steps %[
    Given I am authorized
    And I follow "Configuration"
  ]
end

When(/^I change the local file "([^"]*)" to "([^"]*)"$/) do |filename, content|
    sshcmd("stat #{filename}")
    sshcmd("echo \"#{content}\" > #{filename}")
end

Then(/^I should see a table line with "([^"]*)", "([^"]*)", "([^"]*)"$/) do |arg1, arg2, arg3|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
      fail if not find_link("#{arg2}")
      fail if not find_link("#{arg3}")
  end
end

Then(/^I should see a table line with "([^"]*)", "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
      fail if not find_link("#{arg2}")
  end
end

Then(/^On this client the File "([^"]*)" should exists$/) do |arg1|
   fail if not File.exist?(arg1)
end

Then(/^On this client the File "([^"]*)" should have the content "([^"]*)"$/) do |filename, content|
    fail if not File.exist?(filename)
    fail if not File.read(filename).include?(content)
end

When(/^I enable all actions$/) do
   output = command = "rhn-actions-control --enable-all"
   sshcmd(command)
   code = sshcmd("echo $?")
   if code != 0
     raise "Execute command failed #{output} !"
   end
end
