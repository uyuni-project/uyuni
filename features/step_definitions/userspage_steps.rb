# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Given /^I am on the Users page$/ do
  step 'I am authorized as "admin" with password "admin"'
  step 'I follow "Users"'
end

Given /^I am on the Details page$/ do
  step "I am on the Users page"
  step 'I follow "user1"'
end

Then /^Table row for "([^"]*)" should contain "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//form/div/div/div/table/tbody/tr[.//a[contains(.,'#{arg1}')]]") do
    fail if not has_content?(arg2)
  end
end
