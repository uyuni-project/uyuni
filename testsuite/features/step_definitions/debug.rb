When(/^debug$/) do
  page.driver.debug
end

When(/^I print html$/) do
  puts page.html
end
