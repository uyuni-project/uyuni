# Copyright (c) 2010-2017 Suse-linux
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
require 'simplecov'
require 'capybara/poltergeist'
SimpleCov.start
host = ENV['SERVER']

require 'minitest/unit'
World(MiniTest::Assertions)

# basic support for rebranding of strings in the UI
BRANDING = ENV['BRANDING'] || 'suse'
DEFAULT_TIMEOUT = 200

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
  raise "SERVER must be the FQDN only"
end
host = "https://#{host}"

$myhostname = host
ENV['LANG'] = "en_US.UTF-8"
ENV['IGNORECERT'] = "1"

Capybara.default_wait_time = 10
Capybara.register_driver :poltergeist do |app|
  Capybara::Poltergeist::Driver.new(app,
                                    :phantomjs_options => ['--debug=no',
                                                           '--ignore-ssl-errors=yes',
                                                           '--ssl-protocol=TLSv1',
                                                           '--web-security=false'],
                                    :js_errors => false,
                                    :timeout => 250,
                                    :debug => false)
end
Capybara.default_driver = :poltergeist
Capybara.javascript_driver = :poltergeist
Capybara.app_host = host
# don't run own server on a random port
Capybara.run_server = false

# screenshots
After do |scenario|
  if scenario.failed?
    encoded_img = page.driver.render_base64(:png, :full => true)
    embed("data:image/png;base64,#{encoded_img}", 'image/png')
    debug_server_on_realtime_failure
  end
end

def debug_server_on_realtime_failure
  puts
  puts '#' * 55 + 'var/log/rhn_web_ui.log' + '#' * 55
  out, _code = $server.run('tail -n35 /var/log/rhn/rhn_web_ui.log')
  puts out
  puts '#' * 131
end
