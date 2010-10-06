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
