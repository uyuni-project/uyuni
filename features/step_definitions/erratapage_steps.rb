# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Given /^I am on the Errata page$/ do
  Given "I am authorized"
  within(:xpath, "//div[@id=\"mainNavWrap\"]") do
    find_link("Patches").click
  end
end

When /^I check test channel$/ do
  within(:xpath, "//form/table/tbody/tr[contains(.,'Test Base Channel')]") do
    find(:xpath, "//input[@type='checkbox']").set(true)
  end
end

When /^I check "([^"]*)" erratum$/ do |arg1|
  within(:xpath, "//form/table/tbody/tr[.//a[contains(.,'"+arg1+"')]]") do
    find(:xpath, "//input[@type='checkbox']").set(true)
  end
end

When /^I search for "([^"]*)"$/ do |arg1|
  within(:xpath, "//form/div/div/table/tbody/tr[contains(.,'Search For')]") do
    fill_in "search_string", :with => arg1
    click_button "Search"
  end
end
