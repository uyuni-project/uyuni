# Copyright (c) 2019 SUSE LLC.
# Licensed under the terms of the MIT license.

When(/^I click the environment build button$/) do
  find(:xpath, '//*[@id="cm-build-modal-save-button"]').click
end

When(/^I click promote for Development to QA$/) do
  find(:xpath, '//*[@id="dev_name-promote-modal-link"]').click
end

When(/^I click promote for QA to Production$/) do
  find(:xpath, '//*[@id="qa_name-promote-modal-link"]').click
end

When(/^I should see a "([^"]*)" text in the environment "([^"]*)"$/) do |text, env|
  within(:xpath, "//h3[text()='#{env}']/../..") do
    has_content?(text)
  end
end

When(/^I add the "([^"]*)" channel to sources$/) do |channel|
  within(:xpath, "//span[text()='#{channel}']/../..") do
    find(:xpath, './/input[@type="checkbox"]').set(true)
  end
end

Then(/^I wait until I see "([^"]*)" text in the environment "([^"]*)"$/) do |text, env|
  within(:xpath, "//h3[text()='#{env}']/../..") do
    raise "Text #{text} not found" unless has_text?(text, wait: DEFAULT_TIMEOUT)
  end
end
