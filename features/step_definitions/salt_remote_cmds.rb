# Copyright 2016 (c) SUSE LLC
# Licensed under the terms of the MIT license.
require 'timeout'

When(/^I click on preview$/) do
  find('button#preview').click
end

When(/^I click on run$/) do
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        begin
          find('button#run').click
          break
        rescue Capybara::ElementNotFound
          sleep(5)
        end
      end
    end
  rescue Timeout::Error
      fail "Run button not found"
  end
end

When(/^I should see my hostname$/) do
  fail unless page.has_content?($minion_hostname)
end

When(/^I should not see my hostname$/) do
  fail if page.has_content?($minion_hostname)
end

When(/^I expand the results$/) do
   find("div[id='#{$minion_fullhostname}']").click
end

When(/^I expand the results for "(.*)"$/) do |host|
  find("div[id='#{$ceos_minion_fullhostname}']").click if host == "ceos-minion"
  find("div[id='#{$ssh_minion_fullhostname}']").click if host == "ssh-minion"
end

Then(/^I enter command "([^"]*)"$/) do |arg1|
  fill_in "command", with: arg1
end

Then(/^I should see "([^"]*)" in the command output$/) do |arg1|
  within("pre[id='#{$minion_fullhostname}-results']") do
    fail unless page.has_content?('SuSE-release')
  end
end
