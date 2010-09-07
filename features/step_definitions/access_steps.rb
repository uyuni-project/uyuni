Given /^I am not authorized$/ do
  visit Capybara.app_host
  fail if not find_button('Sign In').visible?
end

When /^I go to the home page$/ do
  visit Capybara.app_host
end

Given /^I access the host the first time$/ do
  visit Capybara.app_host
  fail if not page.has_content?("Create Spacewalk Administrator")
end

