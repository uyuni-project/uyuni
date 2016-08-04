require 'open-uri'
require 'uri'
require 'openssl'

Given(/^any non\-static resource$/) do
  pending # express the regexp above with the code you wish you had
end

Given(/^I navigate to any non-static page$/) do
  where = ['I am on the Systems page'].sample
  step(where)
  @headers = Capybara.current_session.response_headers
  @url = Capybara.current_session.current_url
end

Given(/^I retrieve any static resource$/) do
  resource = ['/img/action-add.gif', '/css/spacewalk.css', '/fonts/DroidSans.ttf',
              '/javascript/actionchain.js'].sample
  visit Capybara.app_host + resource
  @headers = Capybara.current_session.response_headers
  @url = Capybara.current_session.current_url
end

And(/^the response header "(.*?)" should be "(.*?)"$/) do |arg1, arg2|
  begin
    assert_includes(@headers.keys, arg1,
                    "Header '#{arg1}' not present in '#{@url}'")
    assert_equal(arg2, Capybara.current_session.response_headers[arg1],
                 "Header '#{arg1}' in '#{@url}' is not '#{arg2}'")
  rescue Capybara::NotSupportedByDriverError
    pending('Current driver does not support checking response headers')
  end
end

And(/^the response header "(.*?)" should not be "(.*?)"$/) do |arg1, arg2|
  begin
    refute_equal(arg2, @headers[arg1],
                 "Header '#{arg1}' in '#{@url}' is '#{arg2}'")
  rescue Capybara::NotSupportedByDriverError
    pending('Current driver does not support checking response headers')
  end
end

Then(/^the response header "(.*?)" should contain "(.*?)"$/) do |arg1, arg2|
  begin
    assert_includes(@headers.keys, arg1,
                    "Header '#{arg1}' not present in '#{@url}'")
    assert_includes(@headers[arg1], arg2,
                    "Header '#{arg1}' in '#{@url}' does not contain '#{arg2}'")
  rescue Capybara::NotSupportedByDriverError
    pending('Current driver does not support checking response headers')
  end
end

Then(/^the response header "(.*?)" should not be present$/) do |arg1|
  begin
    refute_includes(@headers.keys, arg1,
                    "Header '#{arg1}' present in '#{@url}'")
  rescue Capybara::NotSupportedByDriverError
    pending('Current driver does not support checking response headers')
  end
end

Then(/^the login form does not contain a jsessionid$/) do
  form = find(:xpath, '//form[@name="loginForm"]')
  refute_includes(form['action'], 'jsessionid=', 'URL rewriting is enabled: jsessionid present')
end
