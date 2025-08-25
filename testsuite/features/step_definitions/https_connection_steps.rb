# Copyright 2017-2025 SUSE LLC.
# Licensed under the terms of the MIT license.

### SSL connection checks to server

require 'open-uri'
require 'uri'
require 'openssl'

When(/^I connect to the server insecurely$/) do
  @url = "#{Capybara.app_host}".gsub("https:", "http:")
  begin
    @res = URI.open(@url, redirect: false, open_timeout: 5)
  rescue => @e
  end
end

Then(/^the connection should redirect to the secured channel$/) do
  if not (@e.instance_of?(OpenURI::HTTPRedirect) and @e.message == "302 302") then
    print("\tReturn code: #{@res.status[0]}\n\tURI: #{@res.base_uri.to_s}\n") if @res
    raise ScriptError, "The connection has not redirected to the secure channel!"
  end
  print("\tException message: #{@e.message}\n")
end

When(/^I connect to the server securely$/) do
  @url = "#{Capybara.app_host}".gsub("http:", "https:")
  @res = URI.open(@url, redirect: false, open_timeout: 5, ssl_verify_mode: OpenSSL::SSL::VERIFY_NONE)
  visit('/')
end

Then(/^the connection should be secured$/) do
  if not (@res and @res.status[0].to_i == 200)
    print("\tReturn code: #{@res.status[0]}\n\tURI: #{@res.base_uri.to_s}\n") if @res
    raise ScriptError, "The return value is not OK (code: 200)."
  end
end

And(/^the page title should be "(.*?)" text$/) do |page_title|
    print("\tGiven title:  #{page_title}\n")
    print("\tPage title:  #{page.title}\n")
    raise ScriptError, "The page title does not match!" unless page_title == page.title
end

When(/^I connect to the server securely while using CA certificate file$/) do
  @url = "#{Capybara.app_host}".gsub("http:", "https:")
  ssl_ca_cert_file = "/etc/ssl/certs/#{@url.gsub('https://', '')}.pem"
  begin
    @res = URI.open(@url, redirect: false, open_timeout: 5, ssl_ca_cert: ssl_ca_cert_file, ssl_verify_mode: OpenSSL::SSL::VERIFY_PEER)
  rescue => @e
    print("\tCA certificate file: ${ssl_ca_cert_file}\n")
  end
  visit('/')
end

When(/^I connect to the server securely while using incorrect certificate as a CA certificate file$/) do
  ssl_ca_cert_file = '/tmp/dummy_CA.pem'
  root_key = OpenSSL::PKey::RSA.new 2048
  root_ca = OpenSSL::X509::Certificate.new
  root_ca.public_key = root_key.public_key
  root_ca.version = 2  # RFC 5280, "v3" certificate
  root_ca.serial = 1
  root_ca.subject = OpenSSL::X509::Name.parse "/DC=localdomain/DC=localhost/CN=dummy CA"
  root_ca.issuer = root_ca.subject
  root_ca.not_before = Time.now
  root_ca.not_after = root_ca.not_before + 864000  # 10 days
  ef = OpenSSL::X509::ExtensionFactory.new
  ef.subject_certificate = root_ca
  ef.issuer_certificate = root_ca
  root_ca.add_extension(ef.create_extension("basicConstraints","CA:TRUE",true))
  root_ca.add_extension(ef.create_extension("keyUsage","keyCertSign, cRLSign", true))
  root_ca.sign(root_key, OpenSSL::Digest::SHA256.new)
  File.open(ssl_ca_cert_file, 'w') { |file| file.write(root_ca.to_pem) }
  @url = "#{Capybara.app_host}".gsub("http:", "https:")
  begin
    @res = URI.open(@url, redirect: false, open_timeout: 5, ssl_ca_cert: ssl_ca_cert_file, ssl_verify_mode: OpenSSL::SSL::VERIFY_PEER)
  rescue OpenSSL::SSL::SSLError => @e
  end
end

Then(/^the secure connection should fail due to unverified certificate signature$/) do
  raise ScriptError, "Connection passed unexpectidly!" if @e.nil?
  raise ScriptError, "Unexpected connection error!\n\t#{@e.message}\n" unless @e.instance_of?(OpenSSL::SSL::SSLError)
end
