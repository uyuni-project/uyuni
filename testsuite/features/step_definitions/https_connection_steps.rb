# Copyright 2025 SUSE LLC.
# Licensed under the terms of the MIT license.

### SSL connection checks to server
# helpers in features/support/network_utils.rb

require 'open-uri'
require 'uri'

When(/^I connect to the server insecurely$/) do
  begin
    add_context(:uri_open_result, URI.open(Capybara.app_host.gsub('https:', 'http:'), redirect: false, open_timeout: 5))
  rescue StandardError => e
    add_context(:error, e)
  end
end

Then(/^the connection should redirect to the secured channel$/) do
  error = get_context(:error)
  unless [OpenURI::HTTPRedirect, OpenSSL::SSL::SSLError, Faraday::SSLError].any? { |err_class| error.instance_of? err_class } && error.message == '302 302'
    uri_open_result = get_context(:uri_open_result)
    log("Return code: #{uri_open_result.status.first}\nURI: #{uri_open_result.base_uri}\n") unless uri_open_result.nil?
    raise ScriptError, 'The connection has not redirected to the secure channel!'
  end
  log("Error: #{error.message}\n") unless error.message.nil?
end

When(/^I connect to the server securely$/) do
  uri_open_result = URI.open(Capybara.app_host.gsub('http:', 'https:'), redirect: false, open_timeout: 5, ssl_verify_mode: OpenSSL::SSL::VERIFY_NONE)
  add_context(:uri_open_result, uri_open_result)
  visit('/')
end

Then(/^the connection should be secured$/) do
  uri_open_result = get_context(:uri_open_result)
  unless uri_open_result && uri_open_result.status.first.to_i == 200
    log("Error: #{error.message}\n") unless error.message.nil?
    log("Return code: #{uri_open_result.status.first}\nURI: #{uri_open_result.base_uri}\n") if uri_open_result
    raise ScriptError, 'The return value is not OK (not code 200).'
  end
end

Then(/^the page title should contain "(.*?)" text$/) do |page_title|
  unless page.title.match(".+#{page_title}")
    log("Given title: #{page_title}\nPage title: #{page.title}\n")
    raise ScriptError, 'The page title does not match!'
  end
end

When(/^I connect to the server securely while using CA certificate file$/) do
  url = Capybara.app_host.gsub('http:', 'https:')
  ssl_ca_cert_file = "/etc/ssl/certs/#{url.gsub('https://', '')}.pem"
  log("ssl_ca_cert_file: #{ssl_ca_cert_file}")
  begin
    add_context(:uri_open_result, URI.open(url, redirect: false, open_timeout: 5, ssl_ca_cert: ssl_ca_cert_file, ssl_verify_mode: OpenSSL::SSL::VERIFY_PEER))
  rescue StandardError => e
    add_context(:error, e)
    log("CA certificate file: ${ssl_ca_cert_file}\n")
  end
  visit('/')
end

When(/^I connect to the server securely while using incorrect certificate as a CA certificate file$/) do
  ssl_cacert_file = '/tmp/dummy_CA.pem'
  generate_dummy_cacert(ssl_cacert_file, '/DC=localdomain/DC=localhost/CN=dummy https test CA')
  begin
    add_context(:ssl_cacert_file, ssl_cacert_file)
    uri_open_result = URI.open(Capybara.app_host.gsub('http:', 'https:'), redirect: false, open_timeout: 5, ssl_ca_cert: ssl_cacert_file, ssl_verify_mode: OpenSSL::SSL::VERIFY_PEER)
    add_context(:uri_open_result, uri_open_result)
  rescue StandardError => e
    add_context(:error, e)
  end
end

Then(/^the secure connection should fail due to unverified certificate signature$/) do
  error = get_context(:error)
  raise ScriptError, 'Connection passed unexpectidly!' if error.message.nil?

  unless error.instance_of?(OpenSSL::SSL::SSLError)
    ssl_cacert = get_dummy_cacert(get_context(:ssl_cacert_file))
    log("Dummy CA certificate:\n#{ssl_cacert}")
    log("Error: #{error.message}\n")
    raise ScriptError, 'Unexpected connection error!'
  end
end
