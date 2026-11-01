# Copyright 2017-2025 SUSE LLC.
# SPDX-License-Identifier: MIT

### This file contains the definitions of all steps concerning
### URI and SSL integrity.

require 'open-uri'
require 'uri'
require 'openssl'

When(/^I retrieve a "(.*)" static resource$/) do |resource_type|
  static_resources = {
    'img' => 'action-add.gif',
    'css' => 'susemanager-sp-migration.css',
    'fonts' => 'DroidSans.ttf',
    'javascript' => 'actionchain.js'
  }
  @url = "#{Capybara.app_host}/#{resource_type}/#{static_resources[resource_type]}"
  URI.open(@url, ssl_verify_mode: OpenSSL::SSL::VERIFY_NONE) do |file|
    @headers = file.meta
  end
end

Then(/^the response header "(.*?)" should be "(.*?)"$/) do |name, value|
  assert_includes(@headers.keys, name.downcase, "Header '#{name}' not present in '#{@url}'")
  assert_equal(value, @headers[name.downcase], "Header '#{name}' in '#{@url}' is not '#{value}'")
end

Then(/^the response header "(.*?)" should not be "(.*?)"$/) do |name, value|
  refute_equal(value, @headers[name.downcase], "Header '#{name}' in '#{@url}' is '#{value}'")
end

Then(/^the response header "(.*?)" should contain "(.*?)"$/) do |name, value|
  assert_includes(@headers.keys, name.downcase, "Header '#{name}' not present in '#{@url}'")
  assert_includes(@headers[name.downcase], value, "Header '#{name}' in '#{@url}' does not contain '#{value}'")
end

Then(/^the response header "(.*?)" should not be present$/) do |name|
  refute_includes(@headers.keys, name.downcase, "Header '#{name}' present in '#{@url}'")
end
