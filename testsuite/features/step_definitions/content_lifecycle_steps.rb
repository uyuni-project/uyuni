When(/^I click the environment build button$/) do
  page.find(:xpath, '//*[@id="cm-build-modal-save-button"]').click
end

When(/^I click promote for Development to QA$/) do
  page.find(:xpath, '//*[@id="dev_name-promote-modal-link"]').click
end

When(/^I click promote for QA to Production$/) do
  page.find(:xpath, '//*[@id="qa_name-promote-modal-link"]').click
end

When(/^I should see a "([^"]*)" text in the environment "([^"]*)"$/) do |text, env|
  within(:xpath, "//h3[text()='#{env}']/../..") do
    page.has_content?(text)
  end
end

When(/^I add the "([^"]*)" channel to sources$/) do |channel|
  within(:xpath, "//span[text()='#{channel}']/../..") do
    find(:xpath, './/input[@type="checkbox"]').set(true)
  end
end
