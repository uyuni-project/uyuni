Given /^I am authorized$/ do
  visit "/"
  fill_in "username", :with => "testing"
  fill_in "password", :with => "testing"
  click_button "Sign In"
end
    

When /^I sign out$/ do
  find_link("Sign Out").click
end
    
Then /^I should see "([^"]*)"$/ do |arg1|
  page.should have_content(arg1)
end

Then /^I should not be authorized$/ do
  page.should have_no_content("Sign Out")
end

Then /^I should be logged in$/ do
  page.should have_content("Sign Out")
end
