Given /^I am authorized as "([^"]*)" with password "([^"]*)"$/ do |arg1,arg2|
  visit Capybara.app_host
  fill_in "username", :with => arg1
  fill_in "password", :with => arg2
  click_button "Sign In"
end

Given /^I am authorized$/ do
  Given "I am authorized as \"testing\" with password \"testing\""
end


When /^I sign out$/ do
  find_link("Sign Out").click
end

Then /^I should not be authorized$/ do
  page.has_no_content?("Sign Out")
  
end

Then /^I should be logged in$/ do
  page.has_content?("Sign Out")
end

Then /^I am logged-in$/ do
  #print body
  assert find_link("Sign Out").visible?
  assert page.has_content?("You have created your first user for the Spacewalk Service. Additional configuration should be finalized by clicking here")
end

When /^I go to the admin configuration page$/ do
  find_link("Admin").click
  find_link("Spacewalk Configuration").click
end

When /^I go to the users page$/ do
  find_link("Users").click
end

