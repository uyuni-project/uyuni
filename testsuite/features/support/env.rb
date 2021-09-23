# Copyright (c) 2010-2021 SUSE LLC
# Licensed under the terms of the MIT license.

require 'English'
require 'rubygems'
require 'tmpdir'
require 'base64'
require 'capybara'
require 'capybara/cucumber'
# require 'simplecov'
require 'minitest/autorun'
require 'securerandom'
require 'selenium-webdriver'
require 'multi_test'
require_relative 'common_lib'
require_relative 'constants'
require_relative 'lavanda'
require_relative 'twopence_init'
require_relative 'navigation_lib'
require_relative 'client_stack'
require_relative 'retail_lib'
require_relative 'xmlrpc/xmlrpc_test'
require_relative 'xmlrpc/xmlrpc_image'
require_relative 'custom_formatter'
require_relative 'pretty_formatter_extended'

## code coverage analysis
# SimpleCov.start

# Global variables
server = ENV['SERVER']
$pxeboot_mac = ENV['PXEBOOT_MAC']
$private_net = ENV['PRIVATENET'] if ENV['PRIVATENET']
$mirror = ENV['MIRROR']
$server_http_proxy = ENV['SERVER_HTTP_PROXY'] if ENV['SERVER_HTTP_PROXY']
$no_auth_registry = ENV['NO_AUTH_REGISTRY'] if ENV['NO_AUTH_REGISTRY']
$auth_registry = ENV['AUTH_REGISTRY'] if ENV['AUTH_REGISTRY']
if ENV['SCC_CREDENTIALS']
  scc_username, scc_password = ENV['SCC_CREDENTIALS'].split('|')
  $scc_credentials = !scc_username.to_s.empty? && !scc_password.to_s.empty?
end
$debug_mode = true if ENV['DEBUG']
$long_tests_enabled = true if ENV['LONG_TESTS'] == 'true'
puts 'Executing long running tests' if $long_tests_enabled

# maximal wait before giving up
# the tests return much before that delay in case of success
$stdout.sync = true
Capybara.default_max_wait_time = ENV['CAPYBARA_TIMEOUT'] ? ENV['CAPYBARA_TIMEOUT'].to_i : 10

# QAM and Build Validation pipelines will provide a json file including all custom (MI) repositories
custom_repos_path = "#{File.dirname(__FILE__)}/../upload_files/custom_repositories.json"
if File.exist?(custom_repos_path)
  custom_repos_file = File.read(custom_repos_path)
  $custom_repositories = JSON.parse(custom_repos_file)
  $build_validation = true
end

# Fix a problem with minitest and cucumber options passed through rake
MultiTest.disable_autorun

World(MiniTest::Assertions, CommonLib, LavandaBasic, TwoPenceLib, NavigationLib, ClientStack, RetailLib)
World(CustomFormatter, PrettyFormatterExtended)

# Initialize Twopence Nodes
TwoPenceLib.initialize_nodes

# register chromedriver headless mode
Capybara.register_driver(:headless_chrome) do |app|
  client = Selenium::WebDriver::Remote::Http::Default.new
  # WORKAROUND failure at Scenario: Test IPMI functions: increase from 60 s to 180 s
  client.read_timeout = 180
  # Chrome driver options
  chrome_options = %w[
    no-sandbox
    disable-dev-shm-usage
    ignore-certificate-errors
    disable-gpu
    window-size=2048,2048
    js-flags=--max_old_space_size=2048
  ]
  chrome_options << 'headless' unless $debug_mode
  capabilities = Selenium::WebDriver::Remote::Capabilities.chrome(
    chromeOptions: {
      args: chrome_options,
      w3c: false,
      prefs: {
        'download.default_directory': '/tmp/downloads'
      }
    },
    unexpectedAlertBehaviour: 'accept',
    unhandledPromptBehavior: 'accept'
  )

  Capybara::Selenium::Driver.new(app, browser: :chrome, desired_capabilities: capabilities, http_client: client)
end

Capybara.default_driver = :headless_chrome
Capybara.javascript_driver = :headless_chrome
Capybara.app_host = "https://#{server}"
Capybara.server_port = 8888 + ENV['TEST_ENV_NUMBER'].to_i
puts "Capybara APP Host: #{Capybara.app_host}:#{Capybara.server_port}"

# container operations
$cont_op = XMLRPCImageTest.new(ENV['SERVER'])
