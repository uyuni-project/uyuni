#
# Common "Then" phrases
#


#
# Test for a text in the whole page
#
Then /^I should see a "([^"]*)" text$/ do |arg1|
  page.has_content?(arg1)
end

#
# Test for a visible link in the whole page
#
Then /^I should see a "([^"]*)" link$/ do |arg1|
  find_link(arg1).visible?
end

Then /^I should see a "([^"]*)" button$/ do |arg1|
  find_button(arg1).visible?
end

#
# Test for a visible link inside of a <div> with the attribute 
# "class" or "id" of the given name
#
Then /^I should see a "([^"]*)" link in "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\"] | //div[@class=\"#{arg2}\"]") do
    find_link(arg1).visible?
  end
end

#
# Test if a checkbox is checked
#
Then /^I should see "([^"]*)" as checked$/ do |arg1|
  has_checked_field?(arg1)
end

#
# Test the current path of the URL
#
Then /^the current path is "([^"]*)"$/ do |arg1|
  (current_path == arg1)
end


Then /^I should see "([^"]*)" in field "([^"]*)"$/ do |arg1, arg2|
  page.has_field?(arg2, :with => arg1)
end



#
# Common "When" phrases
#

#
# Check a checkbox of the given id
#
When /^I check "([^"]*)"$/ do |arg1|
  check(arg1)
end

When /^I select "([^"]*)" from "([^"]*)"$/ do |arg1, arg2|
  select(arg1, :from => arg2)
end

#
# Enter a text into a textfield
#
When /^I enter "([^"]*)" as "([^"]*)"$/ do |arg1, arg2|
  fill_in arg2, :with => arg1
end

#
# Click on a button
#
When /^I click on "([^"]*)"$/ do |arg1|
  click_button arg1
end

#
# Click on a link
#
When /^I follow "([^"]*)"$/ do |arg1|
  find_link(arg1).click
end

#
# Click on a link which appears inside of <div> with 
# the given "id"
When /^I follow "([^"]*)" in "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\"]") do
    find_link(arg1).click
  end
end

#
# Click on a link which appears inside of <div> with 
# the given "class"
When /^I follow "([^"]*)" in class "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@class=\"#{arg2}\"]") do
    find_link(arg1).click
  end
end


#
# Sleep for X seconds
#
When /^I wait for "(\d+)" seconds$/ do |arg1|
  sleep(arg1.to_i)
end



