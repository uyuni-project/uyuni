# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

When(/^I click on "([^"]+)" for "([^"]+)"$/) do |arg1, arg2|
  within(:xpath, "//section") do
    within(:xpath, "//table/tbody/tr[.//a[contains(.,'#{arg2}')]]") do
      find_link(arg1).click
    end
  end
end
