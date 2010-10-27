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

Then /^I should see package "([^"]*)"$/ do |package|
  within(:xpath, "//form/table/tbody/tr") do
    fail if not find_link(package)
  end
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
    find('//select').select(arg1)
  end
end
