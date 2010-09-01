#
# Common "Then" phrases
#

Then /^I should see a "([^"]*)" text$/ do |arg1|
  page.should have_content(arg1)
end

Then /^I should see a "([^"]*)" link$/ do |arg1|
  find_link(arg1).visible?
end

Then /^I should see a "([^"]*)" link in "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\"]") do
    find_link(arg1).visible?
  end
end


Then /^I should see "([^"]*)"$/ do |arg1|
  page.should have_content(arg1)
end

Then /^I should see "([^"]*)" as checked$/ do |arg1|
  has_checked_field?(arg1)
end

Then /^the current path is "([^"]*)"$/ do |arg1|
  (current_path == arg1)
end

#
# Common "When" phrases
#

When /^I check "([^"]*)"$/ do |arg1|
  check(arg1)
end

When /^I select "([^"]*)"$/ do |arg1|
  pending # express the regexp above with the code you wish you had
end

When /^I enter "([^"]*)" as "([^"]*)"$/ do |arg1, arg2|
  fill_in arg2, :with => arg1
end

When /^I click on "([^"]*)"$/ do |arg1|
  click_button arg1
  sleep(1)
end

When /^I follow "([^"]*)"$/ do |arg1|
  find_link(arg1).click
  sleep(1)
end

When /^I follow "([^"]*)" in "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\"]") do
    find_link(arg1).click
  end
  sleep(1)
end

When /^I wait for "(\d+)" seconds$/ do |arg1|
  sleep(arg1.to_i)
end



