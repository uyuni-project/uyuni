require 'open-uri'
require 'uri'
require 'openssl'

Given(/^any non\-static resource$/) do
  pending # express the regexp above with the code you wish you had
end

Given(/^I navigate to any non-static page$/) do
  page = ['I am on the Systems page'].sample
  step(page)
end

Given(/^I retrieve any static resource$/) do
  resource = ['/img/action-add.gif', '/css/spacewalk.css', '/fonts/DroidSans.ttf',
              '/javascript/actionchain.js'].sample
  visit Capybara.app_host + resource
end

And(/^the response header "(.*?)" should be "(.*?)"$/) do |arg1, arg2|
  begin
    assert_equal(Capybara.current_session.response_headers[arg1], arg2)
  rescue Capybara::NotSupportedByDriverError
    pending('Current driver does not support checking response headers')
  end
end

Then(/^the response header "(.*?)" should include "(.*?)"$/) do |arg1, arg2|
  begin
    assert_includes(Capybara.current_session.response_headers[arg1], arg2)
  rescue Capybara::NotSupportedByDriverError
    pending('Current driver does not support checking response headers')
  end
end

