# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Given /^I am on the errata page$/ do
  step "I am authorized"
  within(:xpath, "//header") do
    find_link(debrand_string("Errata")).click
  end
end

Given /^I am on the "([^"]*)" errata Details page$/ do |arg1|
  step "I am on the errata page"
    step "I follow \"All\" in the left menu"
    step "I follow \"#{arg1}\""
end

Then /^I should see an update in the list$/ do
  fail if not has_xpath?("//form/div/div/div/table/tbody/tr/td/a")
end
