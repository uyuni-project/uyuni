# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Initial step for channel testing
#
Given /^I am testing channels$/ do
  step %[I am authorized as "admin" with password "admin"]
end

Then /^I should see package "([^"]*)"$/ do |package|
  fail if not has_xpath?("//div[@class=\"table-responsive\"]/table/tbody/tr/td/a[contains(.,'#{package}')]")
end

Given /^I am on the manage software channels page$/ do
  step %[I am authorized as "testing" with password "testing"]
  within(:xpath, "//header") do
    find_link("Channels").click
  end
  step %[I follow "Manage Software Channels" in the left menu]
end

When /^I choose "([^"]*)" for "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//a[contains(.,'#{arg2}')]]") do
    find(:xpath, './/select').select(arg1)
  end
end

When /^I push package "([^"]*)" into "([^"]*)" channel$/ do |arg1, arg2|
  srvurl = "http://#{ENV['TESTHOST']}/APP"
  command = "rhnpush --server=#{srvurl} -u admin -p admin --nosig -c #{arg2} #{arg1}"
  output = `#{command} 2>&1`
  if ! $?.success?
    raise "rhnpush failed '#{command}' #{$!}: #{output}"
  end
end

Then /^I should see package "([^"]*)" in channel "([^"]*)"$/ do |arg1, arg2|
  steps %[
    When I am authorized as "admin" with password "admin"
    And I follow "Channels"
    And I follow "#{arg2}"
    And I follow "Packages"
    Then I should see package "#{arg1}"
  ]
end

Then /^I should see a "([^"]*)" text in the "([^"]*)" column$/ do |arg1, arg2|
  within(:xpath, "//*[@class=\"details\"]/table/tbody/tr[.//th[contains(.,'#{arg2}')]]") do
    find("td", :text => "#{arg1}")
  end
end
