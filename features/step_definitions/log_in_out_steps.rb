Given /^I am authorized as "([^"]*)" with password "([^"]*)"$/ do |arg1,arg2|
  visit "/"
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
  page.should have_no_content("Sign Out")
end

Then /^I should be logged in$/ do
  page.should have_content("Sign Out")
end

Then /^I can login$/ do
  current_path == "/rhn/newlogin/CreateFirstUser.do"
  # bad, but I have not yet found out, how I can get 
  # really shown page
end

When /^I go to the admin configuration page$/ do
  sleep(1)
  find_link("Admin").click
  sleep(1)
  find_link("Spacewalk Configuration").click
  sleep(1)
end

When /^I go to the createuser page$/ do
  sleep(1)
  find_link("Users").click
  sleep(1)
end
