# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Test the current path of the URL
#
Then(/^the current path is "([^"]*)"$/) do |arg1|
  fail unless (current_path == arg1)
end

#
# Common "When" phrases
#

#
# Check a checkbox of the given id
#
When(/^I check "([^"]*)"$/) do |arg1|
  check(arg1)
end

When(/^I uncheck "([^"]*)"$/) do |arg1|
  uncheck(arg1)
end

When(/^I select "([^"]*)" from "([^"]*)"$/) do |arg1, arg2|
  select(arg1, :from => arg2)
end

When(/^I choose "([^"]*)"$/) do |arg1|
  find(:xpath, "//input[@type='radio' and @value='#{arg1}']").set(true)
end

#
# Enter a text into a textfield
#
When(/^I enter "([^"]*)" as "([^"]*)"$/) do |arg1, arg2|
  fill_in arg2, :with => arg1
end

When(/^I enter "(.*?)" as "(.*?)" in the content area$/) do |arg1, arg2|
  within(:xpath, "//section") do
    fill_in arg2, :with => arg1
  end
end

#
# Click on a button
#
When(/^I click on "([^"]*)"$/) do |arg1|
  begin
    click_button debrand_string(arg1), :match => :first
  rescue
    sleep 10
    click_button debrand_string(arg1), :match => :first
  end
end
#
# Click on a link
#
When(/^I follow "([^"]*)"$/) do |text|
  begin
    click_link(debrand_string(text))
  rescue
    sleep 10
    click_link(debrand_string(text))
  end
end
#
# Click on the first link
#
When(/^I follow first "([^"]*)"$/) do |text|
  click_link(debrand_string(text), :match => :first)
end

#
# Click on a link which appears inside of <div> with
# the given "id"
When(/^I follow "([^"]*)" in element "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\"]") do
    step %(I follow "#{arg1}")
  end
end

When(/^I want to add a new credential$/) do
  fail unless find("i.fa-plus-circle").click
end

When(/^I follow "([^"]*)" in the (.+)$/) do |arg1, arg2|
  tag = case arg2
  when /left menu/ then "aside"
  when /tab bar|tabs/ then "header"
  when /content area/ then "section"
  else raise "Unknown element with description '#{desc}'"
  end

  within(:xpath, "//#{tag}") do
    step %(I follow "#{arg1}")
  end
end

When(/^I follow first "([^"]*)" in the (.+)$/) do |arg1, arg2|
  tag = case arg2
  when /left menu/ then "aside"
  when /tab bar|tabs/ then "header"
  when /content area/ then "section"
  else raise "Unknown element with description '#{desc}'"
  end

  within(:xpath, "//#{tag}") do
    step "I follow first \"#{arg1}\""
  end
end

#
# Click on a link which appears inside of <div> with
# the given "class"
When(/^I follow "([^"]*)" in class "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@class=\"#{arg2}\"]") do
    step "I follow \"#{arg1}\""
  end
end

When(/^I enter "(.*?)" in the editor$/) do |arg1|
  page.execute_script("ace.edit('contents-editor').setValue('#{arg1}')")
end

When(/^I click Systems, under Systems node$/) do
  find(:xpath, "//div[@id=\"nav\"]/nav/ul/li[contains(@class, 'active')
       and contains(@class, 'open')
       and contains(@class,'node')]/ul/li/a/span[contains(.,'Systems')]").click
end

Given(/^I am not authorized$/) do
  visit Capybara.app_host
  fail unless find_button('Sign In').visible?
end

When(/^I go to the home page$/) do
  visit Capybara.app_host
end

Given(/^I access the host the first time$/) do
  visit Capybara.app_host
  # fail if not page.has_content?("Create Spacewalk Administrator")
  fail unless page.has_content?("Create SUSE Manager Administrator")
end

# Admin Page steps
Given(/^I am on the Admin page$/) do
  steps %(
    When I am authorized as "admin" with password "admin"
    And I follow "Admin"
    And I follow "Setup Wizard"
    )
end

# Credential Page steps
Given(/^I am on the Credentials page$/) do
  steps %(
    When I am authorized as "testing" with password "testing"
    And I follow "User Account"
    And I follow "Credentials"
    )
end

# access the multi-clients/minions
Given(/^I am on the Systems overview page of "(.*?)"$/) do |target|
  steps %(
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    )
  step %(I follow "#{$minion_fullhostname}") if target == "sle-minion"
  step %(I follow "#{$ssh_minion_fullhostname}") if target == "ssh-minion"
  step %(I follow "#{$ceos_minion_fullhostname}") if target == "ceos-minion"
  step %(I follow "#{$client_hostname}") if target == "sle-client"
end

When(/^I follow this "(.*?)" link$/) do |target|
  step %(I follow "#{$minion_fullhostname}") if target == "sle-minion"
  step %(I follow "#{$ssh_minion_fullhostname}") if target == "ssh-minion"
  step %(I follow "#{$ceos_minion_fullhostname}") if target == "ceos-minion"
  step %(I follow "#{$client_hostname}") if target == "sle-client"
end

Given(/^I am on the groups page$/) do
  steps %(
    Given I am on the Systems page
    And I follow "System Groups" in the left menu
    )
end

When(/^I check this client$/) do
  step %(I check "#{$client_hostname}" in the list)
end

Given(/^I am on the active Users page$/) do
  steps %(
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Users"
    And I follow "User List"
    And I follow "Active"
    )
end

Then(/^Table row for "([^"]*)" should contain "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//a[contains(.,'#{arg1}')]]") do
    fail unless has_content?(arg2)
  end
end

# login, logout steps

Given(/^I am authorized as "([^"]*)" with password "([^"]*)"$/) do |arg1, arg2|
  visit Capybara.app_host
  fill_in "username", :with => arg1
  fill_in "password", :with => arg2
  click_button "Sign In"
  step %(I should be logged in)
end

Given(/^I am authorized$/) do
  step %(I am authorized as "testing" with password "testing")
end

When(/^I sign out$/) do
  page.find(:xpath, "//a[@href='/rhn/Logout.do']").click
end

Then(/^I should not be authorized$/) do
  fail unless page.has_no_xpath?("//a[@href='/rhn/Logout.do']")
end

Then(/^I should be logged in$/) do
  fail unless page.has_xpath?("//a[@href='/rhn/Logout.do']")
end

Then(/^I am logged-in$/) do
  fail unless page.find(:xpath, "//a[@href='/rhn/Logout.do']").visible?
  fail unless page.has_content?("You have just created your first SUSE Manager user. To finalize your installation please use the Setup Wizard")
end

When(/^I go to the admin configuration page$/) do
  find_link("Admin").click
  find_link("SUSE Manager Configuration").click
end

When(/^I go to the users page$/) do
  find_link("Users").click
end

When(/^I go to the configuration page$/) do
  find_link("Configuration").click
end

Given(/^I am on the errata page$/) do
  steps %(
    Given I am authorized
    And I follow "Home" in the left menu
    And I follow "Patches" in the left menu
    And I follow "Relevant" in the left menu
    )
end

Given(/^I am on the "([^"]*)" errata Details page$/) do |arg1|
  steps %(
    Given I am on the errata page
    And I follow "All" in the left menu
    And I follow "#{arg1}"
    )
end

Then(/^I should see an update in the list$/) do
  fail unless has_xpath?("//div[@class=\"table-responsive\"]/table/tbody/tr/td/a")
end

Given(/^Patches are visible for the registered client$/) do
  step "I am on the errata page"
  step "I follow \"Relevant\" in the left menu"
  for c in 0..20
    begin
      step "I should see an update in the list"
    rescue
      puts "wait #{c} time 5 seconds"
      sleep 5
    else
      break
    end
  end
end

When(/^I check test channel$/) do
  step %(I check "Test Base Channel" in the list)
end

When(/^I check "([^"]*)" erratum$/) do |arg1|
  step %(I check "#{arg1}" in the list)
end

When(/^I search for "([^"]*)"$/) do |arg1|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[contains(.,'Search For')]") do
    fill_in "search_string", :with => arg1
    click_button "Search"
  end
end
When(/^I am on System Set Manager Overview$/) do
  visit("https://#{$server_fullhostname}/rhn/ssm/index.do")
end
