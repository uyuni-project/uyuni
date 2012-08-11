# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Then /^I should see something$/ do
  fail if not page.has_content?('Sign In')
  fail if not page.has_content?('About')
end

#
# Test for a text in the whole page
#
Then /^I should see a "([^"]*)" text$/ do |arg1|
  fail if not page.has_content?(debrand_string(arg1))
end

#
# Test for a text not allowed in the whole page
#
Then /^I should not see a "([^"]*)" text$/ do |arg1|
  fail if page.has_content?(arg1)
end

#
# Test for a visible link in the whole page
#
Then /^I should see a "([^"]*)" link$/ do |arg1|
  fail if not find_link(debrand_string(arg1)).visible?
end

#
# Validate link is gone
#
Then /^I should not see a "([^"]*)" link$/ do |arg1|
  fail if not has_no_link?(arg1)
end


Then /^I should see a "([^"]*)" button$/ do |arg1|
  fail if not find_button(arg1).visible?
end

#
# Test for a visible link inside of a <div> with the attribute
# "class" or "id" of the given name
#
Then /^I should see a "([^"]*)" link in element "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\" or @class=\"#{arg2}\"]") do
    fail if not find_link(debrand_string(arg1)).visible?
  end
end

Then /^I should see a "([^"]*)" link in the (.+)$/ do |arg1, arg2|
  Then "I should see a \"#{arg1}\" link in element \"#{element_for(arg2)}\""
end

Then /^I should see a "([^"]*)" link in list "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//ul[@id=\"#{arg2}\" or @class=\"#{arg2}\"]") do
    fail if not find_link(arg1).visible?
  end
end

Then /^I should see a "([^"]*)" button in "([^"]*)" form$/ do |arg1, arg2|
  within(:xpath, "//form[@id='#{arg2}']") do
    fail if not find_button(arg1)
  end
end

#
# Test if an option is selected
#
Then /^Option "([^"]*)" is selected as "([^"]*)"$/ do |arg1, arg2|
  fail if not has_select?(arg2, :selected => arg1)
end

#
# Test if a checkbox is checked
#
Then /^I should see "([^"]*)" as checked$/ do |arg1|
  fail if not has_checked_field?(arg1)
end

#
# Test if a checkbox is unchecked
#
Then /^I should see "([^"]*)" as unchecked$/ do |arg1|
  fail if not has_unchecked_field?(arg1)
end

#
# Test if a checkbox is disabled
#
Then /^the "([^\"]*)" checkbox should be disabled$/ do |arg1|
  fail if field_labeled(arg1)['disabled'].nil?
end

Then /^I should see "([^"]*)" in field "([^"]*)"$/ do |arg1, arg2|
  fail if not page.has_field?(arg2, :with => arg1)
end

Then /^I should see a "([^"]*)" element in "([^"]*)" form$/ do |arg1, arg2|
   within(:xpath, "//form[@id=\"#{arg2}\"] | //form[@name=\"#{arg2}\"]") do
    fail if not find_field(arg1).visible?
  end
end

Then /^"([^"]*)" is installed$/ do |package|
   output = `rpm -q #{package} 2>&1`
   if ! $?.success?
       raise "exec rpm failed (Code #{$?}): #{$!}: #{output}"
   end
end

When /^I check "([^"]*)" in the list$/ do |arg1|
  within(:xpath, "//form/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
    find(:xpath, ".//input[@type='checkbox']").set(true)
  end
end

Then /^The table should have a column named "([^"]+)"$/ do |arg1|
  find(:xpath, "//form/table/thead[.//th[contains(.,'#{arg1}')]] | //form/div/table/thead[.//th[contains(.,'#{arg1}')]]")
end
