Given /^I am not authorized$/ do
  visit Capybara.app_host
  sleep(1)
  find_link('Sign Out').click if page.has_content?('Sign Out')
  sleep(1)
end

When /^I go to the home page$/ do
  visit Capybara.app_host
  sleep(1)
end

Given /^I access the host the first time$/ do
  visit Capybara.app_host
  sleep(1)
  page.should have_content("Create Spacewalk Administrator")
end

