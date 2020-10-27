require 'open-uri'
require 'uri'
require 'openssl'

Given(/^I retrieve any static resource$/) do
  resource = ['/img/action-add.gif', '/css/susemanager-light.css', '/fonts/DroidSans.ttf',
              '/javascript/actionchain.js'].sample
  @url = Capybara.app_host + resource
  open(@url, ssl_verify_mode: OpenSSL::SSL::VERIFY_NONE) do |f|
    @headers = f.meta
  end
end

And(/^the response header "(.*?)" should be "(.*?)"$/) do |arg1, arg2|
  assert_includes(@headers.keys, arg1.downcase,
                  "Header '#{arg1}' not present in '#{@url}'")
  assert_equal(arg2, @headers[arg1.downcase],
               "Header '#{arg1}' in '#{@url}' is not '#{arg2}'")
end

And(/^the response header "(.*?)" should not be "(.*?)"$/) do |arg1, arg2|
  refute_equal(arg2, @headers[arg1.downcase],
               "Header '#{arg1}' in '#{@url}' is '#{arg2}'")
end

Then(/^the response header "(.*?)" should contain "(.*?)"$/) do |arg1, arg2|
  assert_includes(@headers.keys, arg1.downcase,
                  "Header '#{arg1}' not present in '#{@url}'")
  assert_includes(@headers[arg1.downcase], arg2,
                  "Header '#{arg1}' in '#{@url}' does not contain '#{arg2}'")
end

Then(/^the response header "(.*?)" should not be present$/) do |arg1|
  refute_includes(@headers.keys, arg1.downcase,
                  "Header '#{arg1}' present in '#{@url}'")
end

Then(/^the login form does not contain a jsessionid$/) do
  form = find(:xpath, '//form[@name="loginForm"]')
  refute_includes(form['action'], 'jsessionid=', 'URL rewriting is enabled: jsessionid present')
end
