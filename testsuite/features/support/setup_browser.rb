# Copyright (c) 2010-2017 SUSE-LINUX
# Licensed under the terms of the MIT license.

require 'English'
require 'rubygems'
require 'tmpdir'
require 'base64'
require 'capybara'
require 'capybara/cucumber'
require_relative 'cobbler_test'
require 'simplecov'
require 'capybara/poltergeist'
require 'minitest/unit'
# FIXME: is the cobbler_test.rb inclusion really needed?

ENV['LANG'] = 'en_US.UTF-8'
ENV['IGNORECERT'] = '1'
# FIXME: these 2 variables why, for what are they set?

## codecoverage gem
SimpleCov.start
server = ENV['SERVER']
# minutes maximal wait before giving up
# the tests return much before that delay in case of success
DEFAULT_TIMEOUT = 250
$stdout.sync = true
Capybara.default_wait_time = 10

def enable_assertions
  # include assertion globally
  World(MiniTest::Assertions)
end

# this class is for phantomjs initialization
class PhantomjsInit
  attr_reader :options
  def initialize
    @options = ['--debug=no', '--ignore-ssl-errors=yes',
                '--ssl-protocol=TLSv1', '--web-security=false']
  end
end

def restart_driver
  session_pool = Capybara.send('session_pool')
  session_pool.each_value do |session|
    driver = session.driver
    driver.restart if driver.is_a?(Capybara::Poltergeist::Driver)
  end
end

# MAIN
phantom = PhantomjsInit.new
# Setups browser driver with capybara/poltergeist
Capybara.register_driver :poltergeist do |app|
  Capybara::Poltergeist::Driver.new(app,
                                    phantomjs_options: phantom.options,
                                    js_errors: false,
                                    timeout: 250,
                                    window_size: [1920, 1080],
                                    debug: false)
end
Capybara.default_driver = :poltergeist
Capybara.javascript_driver = :poltergeist
Capybara.app_host = "https://#{server}"
# don't run own server on a random port
Capybara.run_server = false
# At moment we have only phantomjs

# always restart before each feature,
# so we spare RAM and avoid RAM issues
Before do
  restart_driver
end

# do some tests only if the corresponding node exists
Before('@proxy') do |scenario|
  scenario.skip_invoke! unless $proxy
end
Before('@centosminion') do |scenario|
  scenario.skip_invoke! unless $ceos_minion
end
Before('@sshminion') do |scenario|
  scenario.skip_invoke! unless $ssh_minion
end

# embed a screenshot after each failed scenario
After do |scenario|
  if scenario.failed?
    encoded_img = page.driver.render_base64(:png, full: true)
    embed("data:image/png;base64,#{encoded_img}", 'image/png')
  end
end

# enable minitest assertions in steps
enable_assertions
