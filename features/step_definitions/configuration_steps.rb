# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Initial step for channel testing
#
Given /^I am testing configuration$/ do
  Given 'I am authorized as "admin" with password "admin"'
  Given "I follow \"Configuration\""
end

Then /^I should see a table line with "([^"]*)", "([^"]*)", "([^"]*)"$/ do |arg1, arg2, arg3|
#"/etc/mgr-test-file.cnf", "New Test Channel", "1 system"
  within(:xpath, "//form/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
      fail if not find_link("#{arg2}")
      fail if not find_link("#{arg3}")
  end
end

Then /^On this client the File "([^"]*)" should exists$/ do |arg1|
   fail if not File.exists?(arg1)
end

Then /^On this client the File "([^"]*)" should have the content "([^"]*)"$/ do |filename, content|
    fail if not File.exists?(filename)
    fail if not File.read(filename).include?(content)
end