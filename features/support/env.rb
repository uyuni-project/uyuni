#
# features/support/env.rb
#

# :firefox requires MozillaFirefox 3.7 or later !!

browser = :chrome #:htmlunit #:chrome #:firefox
#browser = :firefox

host = ENV['TESTHOST'] || 'https://andromeda.suse.de'
# may be non url was given
if not host.include?("//")
  host = "https://#{host}"
end

require 'rubygems'
require 'capybara'
require 'capybara/cucumber'
require 'selenium-webdriver'

require 'culerity' if browser == :htmlunit

#Capybara.app = Galaxy

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
  require 'spacewalk_testsuite_base/monkey_patches'
  #Capybara::Driver::Selenium.browser = browser
  driver = Selenium::WebDriver.for browser
  Capybara.app_host = host
  #driver.navigate.to host
end

# Remote::Capabilities.chrome
