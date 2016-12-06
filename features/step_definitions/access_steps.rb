# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Given(/^I am not authorized$/) do
  visit Capybara.app_host
  fail unless find_button('Sign In').visible?
end

When(/^I go to the home page$/) do
  visit Capybara.app_host
end

Given(/^I access the host the first time$/) do
  visit Capybara.app_host
  # fail if not page.has_content?("Create Spacewalk Administrator")
  fail unless page.has_content?("Create SUSE Manager Administrator")
end

Then(/^I should be able to login$/) do
    (0..10).each do |i|
        visit Capybara.app_host
        if page.has_content?('Welcome to SUSE Manager')
            break
        end
        sleep(5)
    end
end
