# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

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

When /^I uncheck "([^"]*)"$/ do |arg1|
  uncheck(arg1)
end

When /^I select "([^"]*)" from "([^"]*)"$/ do |arg1, arg2|
  select(arg1, :from => arg2)
end

When /^I choose "([^"]*)"$/ do |arg1|
  find(:xpath, "//input[@type='radio' and @value='#{arg1}']").set(true)
end

#
# Enter a text into a textfield
#
When /^I enter "([^"]*)" as "([^"]*)"$/ do |arg1, arg2|
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
When /^I click on "([^"]*)"$/ do |arg1|
  click_button debrand_string(arg1), :match => :first
end

#
# Click on a link
#
When /^I follow "([^"]*)"$/ do |arg1|
  link = find_link(debrand_string(arg1))
  if link.nil?
      sleep 1
      $stderr.puts "ERROR - try again"
      link = find_link(debrand_string(arg1))
  end
  link.click
end

#
# Click on the first link
#
When /^I follow first "([^"]*)"$/ do |arg1|
  link = find_link(debrand_string(arg1), :match => :first)
  if link.nil?
      sleep 1
      $stderr.puts "ERROR - try again"
      link = find_link(debrand_string(arg1))
  end
  link.click
end

#
# Click on a link which appears inside of <div> with
# the given "id"
When /^I follow "([^"]*)" in element "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@id=\"#{arg2}\"]") do
    step "I follow \"#{arg1}\""
  end
end

When /^I click on Next Page$/ do
    first(:xpath, "//button[@class='btn btn-default btn-xs fa fa-forward']").click
end

When /^I click on Last Page$/ do
   first(:xpath, "//button[@class='btn btn-default btn-xs fa fa-fast-forward')]").click
end

When /^I click on Prev Page$/ do
   first(:xpath, "//button[@class='btn btn-default btn-xs fa fa-backward')]").click
end

When /^I click on First Page$/ do
   first(:xpath, "//button[@class='btn btn-default btn-xs fa fa-fast-backward')]").click
end



When /^I follow "([^"]*)" in the (.+)$/ do |arg1, arg2|
  tag = case arg2
  when /left menu/ then "aside"
  when /tab bar|tabs/ then "header"
  when /content area/ then "section"
  else raise "Unknown element with description '#{desc}'"
  end

  within(:xpath, "//#{tag}") do
    step "I follow \"#{arg1}\""
  end
end

When /^I follow first "([^"]*)" in the (.+)$/ do |arg1, arg2|
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
When /^I follow "([^"]*)" in class "([^"]*)"$/ do |arg1, arg2|
  within(:xpath, "//div[@class=\"#{arg2}\"]") do
      step "I follow \"#{arg1}\""
  end
end

When /^I click on "([^"]*)" link in the setup wizard$/ do |arg1|
  tag = case arg1
  when /Edit/ then "i.fa-pencil"
  when /List/ then "i.fa-th-list"
  when /Verify/ then "i.fa-check-square"
  else raise "Unknown element"
  end 
  within(".text-left") do
     fail if not find(tag).click
  end
end
      
