# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# features/support/env.rb
#

# :firefox requires MozillaFirefox 3.7 or later !!
$: << File.join(File.dirname(__FILE__), "..", "..", "lib")
require 'rubygems'
require 'tmpdir'
require 'base64'
require 'capybara'
require 'capybara/cucumber'
require File.join(File.dirname(__FILE__), 'cobbler_test')
require File.join(File.dirname(__FILE__), 'zypp_lock_helper')
require 'owasp_zap'
include OwaspZap

browser = ( ENV['BROWSER'] ? ENV['BROWSER'].to_sym : nil ) || :firefox
host = ENV['TESTHOST'] || 'andromeda.suse.de'
proxy = ENV['ZAP_PROXY'].to_s || nil

require 'minitest/unit'
World(MiniTest::Assertions)

# basic support for rebranding of strings in the UI
BRANDING = ENV['BRANDING'] || 'suse'

# Returns current url
def current_url
  driver.current_url
end

def debrand_string(str)
  case BRANDING
    when 'suse'
      case str
        # do not replace
        when "Update Kickstart" then str
        when "Kickstart Snippets" then str
        when "Create a New Kickstart Profile" then str
        when "Step 1: Create Kickstart Profile" then str
        when "Create Kickstart Profile" then str
        when "Test Erratum" then str
        # replacement exceptions
        when "Create Kickstart Distribution" then "Create Autoinstallable Distribution"
        when "Upload Kickstart File" then "Upload Kickstart/Autoyast File"
        when "Upload a New Kickstart File" then "Upload a New Kickstart/AutoYaST File"
        when "RHN Reference Guide" then "Reference Guide"
        when "Create Errata" then "Create Patch"
        when "Publish Errata" then "Publish Patch"
        # generic regex replace
        when /.*kickstartable.*/ then str.gsub(/kickstartable/, 'autoinstallable')
        when /.*Kickstartable.*/ then str.gsub(/Kickstartable/, 'Autoinstallable')
        when /.*Kickstart.*/ then str.gsub(/Kickstart/, 'Autoinstallation')
        when /Errata .* created./ then str.sub(/Errata/, 'Patch')
        when /.*errata update.*/ then str.gsub(/errata update/, 'patch update')
        when /.*Erratum.*/ then str.gsub(/Erratum/, 'Patch')
        when /.*erratum.*/ then str.gsub(/erratum/, 'patch')
        when /.*Errata.*/ then str.gsub(/Errata/, 'Patches')
        when /.*errata.*/ then str.gsub(/errata/, 'patches')
        else str
      end
    else str
  end
end

# may be non url was given
if host.include?("//")
  raise "TESTHOST must be the FQDN only"
end
host = "https://#{host}"

$myhostname = `hostname -f`
$myhostname.chomp!

ENV['LANG'] = "en_US.UTF-8"
ENV['IGNORECERT'] = "1"

Capybara.default_wait_time = 10

# Setup browsers
case browser
when :phantomjs
  require 'capybara/poltergeist'
  Capybara.register_driver :poltergeist do |app|
    Capybara::Poltergeist::Driver.new(app,
                                      :phantomjs_options => ['--debug=no',
                                                             '--ignore-ssl-errors=yes',
                                                             '--ssl-protocol=TLSv1',
                                                             '--web-security=false'],
                                                             :js_errors => false,
                                      :debug => false)
  end
  Capybara.default_driver = :poltergeist
  Capybara.javascript_driver = :poltergeist
  Capybara.app_host = host
when :firefox
  require 'selenium-webdriver'
  Capybara.register_driver :selenium do |app|
    profile = Selenium::WebDriver::Firefox::Profile.new
    if proxy
      profile["network.proxy.type"] = 1
      profile["network.proxy.http"] = proxy
      profile["network.proxy.http_port"] = 8080
      profile["network.proxy.ssl"] = proxy
      profile["network.proxy.ssl_port"] = 8080
    end
    driver = Capybara::Selenium::Driver.new(app, :browser => :firefox,:profile=> profile)
    driver.browser.manage.window.resize_to(1280, 1024)
    driver
  end
  Capybara.default_driver = :selenium
  Capybara.app_host = host
else
  raise "Unsupported browser '#{browser}'"
end

# don't run own server on a random port
Capybara.run_server = false

# screenshots
After do |scenario|
  if scenario.failed?
    encoded_img = page.driver.render_base64(:png, :full => true)
    embed("data:image/png;base64,#{encoded_img}", 'image/png')
  end
end

# make sure proxy is started if we will use ut
Before do
  sec_proxy = ENV['ZAP_PROXY']
  if sec_proxy && ['localhost', '127.0.0.1'].include?(sec_proxy)
    $zap = Zap.new(:target=> "https://#{ENV['TESTHOST']}", :zap=>"/usr/share/owasp-zap/zap.sh")
    unless $zap.running?
      $zap.start(:daemon => true)
      until $zap.running?
        STDERR.puts 'waiting for security proxy...'
        sleep 1
      end
    end
  end
end

# kill owasp zap before exiting
at_exit do
  $zap.shutdown if $zap
end
