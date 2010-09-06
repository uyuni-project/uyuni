#
# features/support/env.rb
#

# :firefox requires MozillaFirefox 3.7 or later !!

$: << File.join(File.dirname(__FILE__), "..", "..", "lib")

browser = :chrome #:htmlunit #:chrome #:firefox
#browser = :firefox

host = ENV['TESTHOST'] || 'https://andromeda.suse.de'
# may be non url was given
if not host.include?("//")
  host = "https://#{host}"
end

ENV['LANG'] = "en_US.UTF-8"
ENV['IGNORECERT'] = "1"

require 'rubygems'
require 'capybara'
require 'capybara/cucumber'
require 'selenium-webdriver'

require 'culerity' if browser == :htmlunit

#Capybara.app = Galaxy
Capybara.default_wait_time = 5

Capybara.default_wait_time = 30

case browser
when :htmlunit
  Capybara.default_driver = :culerity
  Capybara.use_default_driver
else
  Capybara.default_driver = :selenium
  Capybara.app_host = host
end

# don't run own server on a random port
Capybara.run_server = false
if Capybara.default_driver == :selenium
  #require 'spacewalk_testsuite_base/monkey_patches'
  Capybara::Driver::Selenium.browser = browser
  driver = Selenium::WebDriver.for browser
end

# Remote::Capabilities.chrome
