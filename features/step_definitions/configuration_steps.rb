# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.
#
# Initial step for channel testing
#
Given(/^I am testing configuration$/) do
  steps %(
    Given I am authorized
    And I follow "Configuration"
    )
end

When(/^I change the local file "([^"]*)" to "([^"]*)"$/) do |filename, content|
    $client.run("echo \"#{content}\" > #{filename}", true, 600, 'root')
end

Then(/^I should see a table line with "([^"]*)", "([^"]*)", "([^"]*)"$/) do |arg1, arg2, arg3|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
      fail unless find_link("#{arg2}")
      fail unless find_link("#{arg3}")
  end
end

Then(/^I should see a table line with "([^"]*)", "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
      fail unless find_link("#{arg2}")
  end
end

Then(/^On this client the File "([^"]*)" should exists$/) do |arg1|
    $client.run("test -f #{arg1}", true)
end

Then(/^On this client the File "([^"]*)" should have the content "([^"]*)"$/) do |filename, content|
    $client.run("test -f #{filename}")
    $client.run("grep #{content} #{filename}")
end

When(/^I enable all actions$/) do
   $client.run("rhn-actions-control --enable-all", true, 600, 'root')
end
