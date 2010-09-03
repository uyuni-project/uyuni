Given /^I am authorized as "([^"]*)" with password "([^"]*)"$/ do |arg1,arg2|
  visit Capybara.app_host
  sleep(1) 
  fill_in "username", :with => arg1
  fill_in "password", :with => arg2
  click_button "Sign In"
  sleep(1)
end

Given /^I am authorized$/ do
  Given "I am authorized as \"testing\" with password \"testing\""
end


When /^I sign out$/ do
  find_link("Sign Out").click
  sleep(1)
end

Then /^I should not be authorized$/ do
  page.should have_no_content("Sign Out")
end

Then /^I should be logged in$/ do
  page.should have_content("Sign Out")
end

Then /^I am logged-in$/ do
  #print body
  find_link("Sign Out").visible?
  page.should have_content("You have created your first user for the Spacewalk Service. Additional configuration should be finalized by clicking here")
end

When /^I go to the admin configuration page$/ do
  find_link("Admin").click
  sleep(1)
  find_link("Spacewalk Configuration").click
  sleep(1)
end

When /^I go to the users page$/ do
  find_link("Users").click
  sleep(1)
end

