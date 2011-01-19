# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.
#
# http://github.com/jnicklas/capybara/issues/labels/Feature%20request#issue/69

class ::Capybara::Driver::Selenium
  def self.driver
    unless @driver
      @driver = Selenium::WebDriver.for(:remote, :desired_capabilities => @browser)
      at_exit do
        @driver.quit
      end
    end
    @driver
  end
  def self.browser=(b)
    @browser = b
  end
  def self.browser
    @browser ||= :firefox
  end
end

require 'selenium-webdriver'
require 'selenium/webdriver/chrome'
module ::Selenium::WebDriver::Chrome
  class Launcher
    class UnixLauncher
      def self.possible_paths
        ["/usr/bin/chromium", "/usr/bin/google-chrome"]
      end
    end
  end
end

