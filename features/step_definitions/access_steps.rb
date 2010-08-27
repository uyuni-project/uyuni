Given /^I am not authorized$/ do
  visit Capybara.app_host
  find_link('Sign Out').click if page.has_content?('Sign Out')
end

When /^I go to the home page$/ do
#  visit '/'
  visit Capybara.app_host
end
    
When /^I enter "([^"]*)" as "([^"]*)"$/ do |arg1, arg2|
  fill_in arg2, :with => arg1
end
  
When /^I click on "([^"]*)"$/ do |arg1|
  click_button arg1
end

When /^I follow "([^"]*)"$/ do |arg1|
  find_link(arg1).click
end
  