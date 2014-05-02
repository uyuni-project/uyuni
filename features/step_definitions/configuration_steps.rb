# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Initial step for channel testing
#
Given /^I am testing configuration$/ do
  step 'I am authorized'
  step "I follow \"Configuration\""
end

When /^I change the local file "([^"]*)" to "([^"]*)"$/ do |file, content|
    fail if not File.exists?(file)
    fail if File.open(file, "w").write(content) <= 0
end

Then /^I should see a table line with "([^"]*)", "([^"]*)", "([^"]*)"$/ do |arg1, arg2, arg3|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
      fail if not find_link("#{arg2}")
      fail if not find_link("#{arg3}")
  end
end

Then /^I should see a table line with "([^"]*)", "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
      fail if not find_link("#{arg2}")
  end
end

Then /^On this client the File "([^"]*)" should exists$/ do |arg1|
   fail if not File.exists?(arg1)
end

Then /^On this client the File "([^"]*)" should have the content "([^"]*)"$/ do |filename, content|
    fail if not File.exists?(filename)
    fail if not File.read(filename).include?(content)
end

When /^I enable all actions$/ do
   $out = `rhn-actions-control --enable-all`
   if ! $?.success?
     raise "Execute command failed: #{$!}: #{$out}"
   end
end
