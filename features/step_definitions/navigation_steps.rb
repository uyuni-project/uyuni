
#
# Test the current path of the URL
#
Then /^the current path is "([^"]*)"$/ do |arg1|
  fail if not (current_path == arg1)
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

When /^I choose "([^"]*)" from "([^"]*)"$/ do |arg1, arg2|
  choose(arg2, :with => arg1)
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
When /^I follow "([^"]*)" in element "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\"]") do
    find_link(arg1).click
  end
end

When /^I follow "([^"]*)" in the (.+)$/ do |arg1, arg2|
  When "I follow \"#{arg1}\" in element \"#{element_for(arg2)}\""
end


#
# Click on a link which appears inside of <div> with 
# the given "class"
When /^I follow "([^"]*)" in class "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@class=\"#{arg2}\"]") do
    find_link(arg1).click
  end
end
