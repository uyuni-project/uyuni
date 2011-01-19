# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Given /^I am on the groups page$/ do
    Given "I am on the Systems page"
    Given "I follow \"System Groups\" in the left menu"
end

When /^I check this client$/ do
  within(:xpath, "//form/table/tbody/tr[.//a[contains(.,'#{$myhostname}')]]") do
    find(:xpath, "//input[@type='checkbox']").set(true)
  end
end

Then /^I should see this client as a link$/ do
  within(:xpath, "//td[@class='page-content']") do
    fail if not find_link("#{$myhostname}").visible?
  end
end

