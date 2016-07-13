# Copyright 2016 (c) SUSE LLC
# Licensed under the terms of the MIT license.
require 'timeout'

When(/^I click on preview$/) do
  find('button#preview').click
end

When(/^I click on run$/) do
  find('button#run').click
end

When(/^I should see my hostname$/) do
  fail unless page.has_content?($myhostname)
end

When(/^I should not see my hostname$/) do
  fail if page.has_content?($myhostname)
end

When(/^I expand the results$/) do
   find("div[id='#{$myhostname}']").click
end

When(/^I expand the results for "(.*)"$/) do |host|
   find("div[id=#{host}]").click
end

Then(/^I enter command "([^"]*)"$/) do |arg1|
  fill_in "command", with: "ls -la /etc"
end

Then(/^I should see "([^"]*)" in the command output$/) do |arg1|
  within("pre[id='#{$myhostname}-results']") do
    fail unless page.has_content?('SuSE-release')
  end
end

When(/^"(.*)" exists on the filesystem$/) do |file|
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        break if File.exist?(file)
        sleep(1)
      end
    end
  rescue Timeout::Error
    puts "timeout waiting for the file to appear"
  end
  out , _local, _remote, code = $client.test_and_store_results_together("test -f #{file}", "root", 600)
  fail if code != 0
end
