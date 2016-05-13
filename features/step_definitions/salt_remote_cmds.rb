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

When(/^I expand the results$/) do
   find("div[id='#{$myhostname}']").click
end

When(/^I expand the results for "(.*)"$/) do |host|
   find("div[id=#{host}]").click
end

When(/^I verify the results$/) do
  within("pre[id='#{$myhostname}-results']") do
    fail unless page.has_content?('.ssh')
    fail unless page.has_content?('spacewalk-testsuite-base')
    fail unless page.has_content?('.bashrc')
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
  fail unless File.exist?(file)
end
