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
# Click on a button and confirm in alert box
When(/^I click on "([^"]*)" and confirm$/) do |arg1|
  accept_alert do
    step %(I click on "#{arg1}")
    sleep 1
  end
end
#
# Click on a link
#
When(/^I follow "([^"]*)"$/) do |text|
  begin
    click_link(debrand_string(text))
  rescue
    sleep 15
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

When(/^I click on Next Page$/) do
  first(:xpath, "//button[@class='btn btn-default btn-xs fa fa-forward']").click
end

When(/^I click on Last Page$/) do
  first(:xpath, "//button[@class='btn btn-default btn-xs fa fa-fast-forward')]").click
end

When(/^I click on Prev Page$/) do
  first(:xpath, "//button[@class='btn btn-default btn-xs fa fa-backward')]").click
end

When(/^I click on First Page$/) do
  first(:xpath, "//button[@class='btn btn-default btn-xs fa fa-fast-backward')]").click
end

When(/^I click the div "([^"]*)"$/) do |arg1|
  # must give . or # for class or id
  within("#spacewalk-content") do
    fail unless find(arg1).click
  end
end

When(/^I click element by css "([^"]*)"$/) do |arg1|
  fail unless find(arg1).click
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

Then(/^I reload the page$/) do
  visit current_url
end

Then(/^I try to reload page until contains "([^"]*)" text$/) do |arg1|
  found = false
  begin
    Timeout.timeout(30) do
      loop do
        if page.has_content?(debrand_string(arg1))
          found = true
          break
        end
        visit current_url
      end
    end
  rescue Timeout::Error
    raise "'#{arg1}' cannot be found after wait and reload page"
  end
  fail unless found
end

And(/^I navigate to "([^"]*)" page$/) do |page|
  visit("https://#{$server_fullhostname}/#{page}")
end

When(/^I wait until i see "([^"]*)" text, refreshing the page$/) do |text|
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        break if page.has_content?(text)
        sleep 3
        page.evaluate_script 'window.location.reload()'
      end
    end
  rescue Timeout::Error
    raise "Couldn't find the #{text} in webpage"
  end
end

When(/^I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows$/) do
  # this step is used for long operations like refreshing caches, repositories, etc.
  # therefore we use a non-standard timeout
  refresh_timeout = 500
  begin
    Timeout.timeout(refresh_timeout) do
      loop do
        visit current_url
        # get all texts in the table column under the "Status" header
        under_status = "//tr/td[count(//th[contains(*/text(), 'Status')]/preceding-sibling::*) + 1]"
        statuses = page.all(:xpath, under_status).map(&:text)

        # disregard any number of initial SKIPPED rows
        # this is expected when Taskomatic triggers the same task concurrently
        first_non_skipped = statuses.drop_while do |status|
          status == 'SKIPPED'
        end.first

        # halt in case we are done, or if an error is detected
        break if first_non_skipped == 'FINISHED'
        raise('Taskomatic task was INTERRUPTED') if first_non_skipped == 'INTERRUPTED'

        # otherwise either no row is shown yet, or the task is still RUNNING
        # continue waiting
        sleep 1
      end
    end
  rescue Timeout::Error
    raise "Task does not look FINISHED after #{refresh_timeout} seconds"
  end
end

When(/^I wait until I see "([^"]*)" text$/) do |text|
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        break if page.has_content?(text)
        sleep 3
      end
    end
  rescue Timeout::Error
    raise "Couldn't find the #{text} in webpage"
  end
end
