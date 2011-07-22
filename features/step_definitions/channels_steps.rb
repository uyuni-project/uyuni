# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Initial step for channel testing
#
Given /^I am testing channels$/ do
  Given 'I am authorized as "admin" with password "admin"'
end

When /^I check "([^"]*)" in the list$/ do |arg1|
  within(:xpath, "//form/table/tbody/tr[.//a[contains(.,'#{arg1}')]]") do
    find(:xpath, "//input[@type='checkbox']").set(true)
  end
end

When /^I check "([^"]*)" text in the list$/ do |arg1|
  within(:xpath, "//form/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
    find(:xpath, "//input[@type='checkbox']").set(true)
  end
end

Then /^I should see package "([^"]*)"$/ do |package|
  fail if not has_xpath?("//form/table/tbody/tr/td/a[contains(.,'#{package}')]")
end

Given /^I am on the manage software channels page$/ do
  Given 'I am authorized as "testing" with password "testing"'
  within(:xpath, "//div[@id=\"mainNavWrap\"]") do
    find_link("Channels").click
  end
  And "I follow \"Manage Software Channels\" in the left menu"
end

When /^I choose "([^"]*)" for "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//form/table/tbody/tr[.//a[contains(.,'#{arg2}')]]") do
    find(:xpath, '//select').select(arg1)
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
  Given 'I am authorized as "admin" with password "admin"'
  When 'I follow "Channels"'
  When "I follow \"#{arg2}\""
  When 'I follow "Packages"'
  Then "I should see package \"#{arg1}\""
end

Then /^I should see a "([^"]*)" text in the "([^"]*)" column$/ do |arg1, arg2|
  within(:xpath, "//table/tbody/tr[.//th[contains(.,'#{arg2}')]]") do
    find("td", :text => "#{arg1}")
  end
end

