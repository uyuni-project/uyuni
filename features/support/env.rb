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
require File.join(File.dirname(__FILE__), '/cobbler_test.rb')

browser = ( ENV['BROWSER'] ? ENV['BROWSER'].to_sym : nil ) || :firefox
host = ENV['TESTHOST'] || 'andromeda.suse.de'

# basic support for rebranding of strings in the UI
BRANDING = ENV['BRANDING'] || 'suse'

def debrand_string(str)
  case BRANDING
    when 'suse'
      case str
        # do not replace
        when "Update Kickstart" then str
        when "Kickstart Snippets" then str
        when "Create a New Kickstart Profile" then str
        when "Step 1: Create Kickstart Profile" then str
        when "Test Erratum" then str
        # replacement exceptions
        when "Create Kickstart Distribution" then "Create Autoinstallable Distribution"
        when "upload new kickstart file" then "upload new kickstart/autoyast file"
        when "Upload a New Kickstart File" then "Upload a New Kickstart/AutoYaST File"
        when "RHN Reference Guide" then "Reference Guide"
        when "Create Errata" then "Create Patch"
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

# Register different browsers
case browser
  when :chrome
    Capybara.register_driver :selenium do |app|
      Capybara::Selenium::Driver.new(app, :browser => :chrome, :switches => ['--ignore-certificate-errors'])
    end
  when :firefox
    #require 'selenium-webdriver'
    Capybara.register_driver :selenium do |app|
      Capybara::Selenium::Driver.new(app, :browser => :firefox)
    end
end

case browser
when :htmlunit
  require 'culerity'
  Capybara.default_driver = :culerity
  Capybara.use_default_driver
when :webkit
  require "capybara-webkit"
  Capybara.default_driver = :webkit
  Capybara.javascript_driver = :webkit
  Capybara.app_host = host
else
  Capybara.default_driver = :selenium
  Capybara.app_host = host
end

# don't run own server on a random port
Capybara.run_server = false

# screenshots
After do |scenario|

  if scenario.failed?
    case page.driver
    when Capybara::Selenium::Driver
      # chromiumdriver does not support screenshots yet
      if page.driver.options[:browser] == :firefox
        encoded_img = page.driver.browser.screenshot_as(:base64)
        embed("data:image/png;base64,#{encoded_img}", 'image/png')
      end
    when Capybara::Driver::Webkit
      path = File.join(Dir.tmpdir, "testsuite.png")
      page.driver.render(path)
      embed("data:image/png;base64,#{Base64.encode64(File.read(path))}", 'image/png')
    end
  end
end
