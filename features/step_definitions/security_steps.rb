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
    assert_includes(Capybara.current_session.response_headers.keys, arg1,
                    "Header '#{arg1}' not present in '#{Capybara.current_session.current_url}'")
    assert_equal(arg2, Capybara.current_session.response_headers[arg1],
                 "Header '#{arg1}' in '#{Capybara.current_session.current_url}' is not '#{arg2}'")
  rescue Capybara::NotSupportedByDriverError
    pending('Current driver does not support checking response headers')
  end
end

And(/^the response header "(.*?)" should not be "(.*?)"$/) do |arg1, arg2|
  begin
    refute_equal(arg2, Capybara.current_session.response_headers[arg1],
                 "Header '#{arg1}' in '#{Capybara.current_session.current_url}' is '#{arg2}'")
  rescue Capybara::NotSupportedByDriverError
    pending('Current driver does not support checking response headers')
  end
end

Then(/^the response header "(.*?)" should contain "(.*?)"$/) do |arg1, arg2|
  begin
    assert_includes(Capybara.current_session.response_headers.keys, arg1,
                    "Header '#{arg1}' not present in '#{Capybara.current_session.current_url}'")
    assert_includes(Capybara.current_session.response_headers[arg1], arg2,
                    "Header '#{arg1}' in '#{Capybara.current_session.current_url}' does not contain '#{arg2}'")
  rescue Capybara::NotSupportedByDriverError
    pending('Current driver does not support checking response headers')
  end
end

Then(/^the response header "(.*?)" should not be present$/) do |arg1|
  begin
    refute_includes(Capybara.current_session.response_headers.keys, arg1,
                    "Header '#{arg1}' present in '#{Capybara.current_session.current_url}'")
  rescue Capybara::NotSupportedByDriverError
    pending('Current driver does not support checking response headers')
  end
end

Then(/^the login form does not contain a jsessionid$/) do
  form = find(:xpath, '//form[@name="loginForm"]')
  refute_includes(form['action'], 'jsessionid=', 'URL rewriting is enabled: jsessionid present')
end
