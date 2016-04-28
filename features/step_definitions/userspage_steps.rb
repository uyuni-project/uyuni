# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Given(/^I am on the Users page$/) do
  steps %(
    Given I am authorized as "admin" with password "admin"
    And I follow "Users"
    )
end

Given(/^I am on the Details page$/) do
  steps %(
    Given I am on the Users page
    And I follow "user1"
    )
end

Then(/^Table row for "([^"]*)" should contain "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//a[contains(.,'#{arg1}')]]") do
    fail if not has_content?(arg2)
  end
end
