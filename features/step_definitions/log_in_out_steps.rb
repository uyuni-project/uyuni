# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Given(/^I am authorized as "([^"]*)" with password "([^"]*)"$/) do |arg1, arg2|
  visit Capybara.app_host
  fill_in "username", :with => arg1
  fill_in "password", :with => arg2
  click_button "Sign In"
  step %[I should be logged in]
end

Given(/^I am authorized$/) do
  step %[I am authorized as "testing" with password "testing"]
end

When(/^I sign out$/) do
  page.find(:xpath, "//a[@href='/rhn/Logout.do']").click
end

Then(/^I should not be authorized$/) do
  fail if not page.has_no_xpath?("//a[@href='/rhn/Logout.do']")
end

Then(/^I should be logged in$/) do
  fail if not page.has_xpath?("//a[@href='/rhn/Logout.do']")
end

Then(/^I am logged-in$/) do
  fail if not page.find(:xpath, "//a[@href='/rhn/Logout.do']").visible?
  fail if not page.has_content?("You have just created your first SUSE Manager user. To finalize your installation please use the Setup Wizard")
end

When(/^I go to the admin configuration page$/) do
  find_link("Admin").click
  find_link("SUSE Manager Configuration").click
end

When(/^I go to the users page$/) do
  find_link("Users").click
end

When(/^I go to the configuration page$/) do
  find_link("Configuration").click
end
