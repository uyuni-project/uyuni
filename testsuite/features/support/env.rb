# Copyright (c) 2010-2021 SUSE LLC
# Licensed under the terms of the MIT license.

require 'English'
require 'rubygems'
require 'tmpdir'
require 'base64'
require 'capybara'
require 'capybara/cucumber'
#require 'simplecov'
require 'minitest/autorun'
require 'securerandom'
require 'selenium-webdriver'
require 'multi_test'

## code coverage analysis
# SimpleCov.start

server = ENV['SERVER']
$debug_mode = true if ENV['DEBUG']
$long_tests_enabled = true if ENV['LONG_TESTS'] == 'true'
puts "Executing long running tests" if $long_tests_enabled

# maximal wait before giving up
# the tests return much before that delay in case of success
$stdout.sync = true
STARTTIME = Time.new.to_i
Capybara.default_max_wait_time = 10
DEFAULT_TIMEOUT = 250

# QAM and Build Validation pipelines will provide a json file including all custom (MI) repositories
custom_repos_path = File.dirname(__FILE__) + '/../upload_files/' + 'custom_repositories.json'
if File.exist?(custom_repos_path)
  custom_repos_file = File.read(custom_repos_path)
  $custom_repositories = JSON.parse(custom_repos_file)
  $build_validation = true
  # HACK
  # Build Validations will require longer timeouts due to the low performance of our VMs
  # if we ever improve this fact, we can reduce these timeouts.
  Capybara.default_max_wait_time = 30
  DEFAULT_TIMEOUT = 1800
end

def enable_assertions
  # include assertion globally
  World(MiniTest::Assertions)
end

# Fix a problem with minitest and cucumber options passed through rake
MultiTest.disable_autorun

# register chromedriver headless mode
Capybara.register_driver(:headless_chrome) do |app|
  client = Selenium::WebDriver::Remote::Http::Default.new
  # WORKAROUND failure at Scenario: Test IPMI functions: increase from 60 s to 180 s
  client.read_timeout = 180
  # Chrome driver options
  chrome_options = %w[no-sandbox disable-dev-shm-usage ignore-certificate-errors disable-gpu window-size=2048,2048, js-flags=--max_old_space_size=2048]
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

  Capybara::Selenium::Driver.new(
    app,
    browser: :chrome,
    desired_capabilities: capabilities,
    http_client: client
  )
end

Capybara.default_driver = :headless_chrome
Capybara.javascript_driver = :headless_chrome
Capybara.app_host = "https://#{server}"
Capybara.server_port = 8888 + ENV['TEST_ENV_NUMBER'].to_i
puts "Capybara APP Host: #{Capybara.app_host}:#{Capybara.server_port}"

# embed a screenshot after each failed scenario
After do |scenario|
  if scenario.failed?
    begin
      img_path = "screenshots/#{scenario.name.tr(' ./', '_')}.png"
      if page.driver.browser.respond_to?(:save_screenshot)
        Dir.mkdir("screenshots") unless File.directory?("screenshots")
        page.driver.browser.save_screenshot(img_path)
      else
        save_screenshot(img_path)
      end
      # embed the image name in the cucumber HTML report
      embed current_url, 'text/plain'
      embed img_path, 'image/png'
    rescue StandardError => e
      puts "Error taking a screenshot: #{e.message}"
    ensure
      debug_server_on_realtime_failure
      page.reset!
    end
  end
  page.instance_variable_set(:@touched, false)
end

AfterStep do
  if all('.senna-loading').any?
    puts "WARN: Step ends with an ajax transition not finished, let's wait a bit!"
    raise 'Timeout: Waiting AJAX transition' unless has_no_css?('.senna-loading')
  end
end

# enable minitest assertions in steps
enable_assertions

# do some tests only if the corresponding node exists
Before('@proxy') do
  skip_this_scenario unless $proxy
end

Before('@sle_client') do
  skip_this_scenario unless $client
end

Before('@sle_minion') do
  skip_this_scenario unless $minion
end

Before('@centos_minion') do
  skip_this_scenario unless $ceos_minion
end

Before('@ubuntu_minion') do
  skip_this_scenario unless $ubuntu_minion
end

Before('@pxeboot_minion') do
  skip_this_scenario unless $pxeboot_mac
end

Before('@ssh_minion') do
  skip_this_scenario unless $ssh_minion
end

Before('@buildhost') do
  skip_this_scenario unless $build_host
end

Before('@virthost_kvm') do
  skip_this_scenario unless $kvm_server
end

Before('@virthost_xen') do
  skip_this_scenario unless $xen_server
end

Before('@ceos6_minion') do
  skip_this_scenario unless $ceos6_minion
end

Before('@ceos6_ssh_minion') do
  skip_this_scenario unless $ceos6_ssh_minion
end

Before('@ceos6_client') do
  skip_this_scenario unless $ceos6_client
end

Before('@ceos7_minion') do
  skip_this_scenario unless $ceos7_minion
end

Before('@ceos7_ssh_minion') do
  skip_this_scenario unless $ceos7_ssh_minion
end

Before('@ceos7_client') do
  skip_this_scenario unless $ceos7_client
end

Before('@ceos8_minion') do
  skip_this_scenario unless $ceos8_minion
end

Before('@ceos8_ssh_minion') do
  skip_this_scenario unless $ceos8_ssh_minion
end

Before('@ubuntu1604_minion') do
  skip_this_scenario unless $ubuntu1604_minion
end

Before('@ubuntu1604_ssh_minion') do
  skip_this_scenario unless $ubuntu1604_ssh_minion
end

Before('@ubuntu1804_minion') do
  skip_this_scenario unless $ubuntu1804_minion
end

Before('@ubuntu1804_ssh_minion') do
  skip_this_scenario unless $ubuntu1804_ssh_minion
end

Before('@ubuntu2004_minion') do
  skip_this_scenario unless $ubuntu2004_minion
end

Before('@ubuntu2004_ssh_minion') do
  skip_this_scenario unless $ubuntu2004_ssh_minion
end

Before('@debian9_minion') do
  skip_this_scenario unless $debian9_minion
end

Before('@debian9_ssh_minion') do
  skip_this_scenario unless $debian9_ssh_minion
end

Before('@debian10_minion') do
  skip_this_scenario unless $debian10_minion
end

Before('@debian10_ssh_minion') do
  skip_this_scenario unless $debian10_ssh_minion
end

Before('@sle11sp4_ssh_minion') do
  skip_this_scenario unless $sle11sp4_ssh_minion
end

Before('@sle11sp4_minion') do
  skip_this_scenario unless $sle11sp4_minion
end

Before('@sle11sp4_client') do
  skip_this_scenario unless $sle11sp4_client
end

Before('@sle12sp4_ssh_minion') do
  skip_this_scenario unless $sle12sp4_ssh_minion
end

Before('@sle12sp4_minion') do
  skip_this_scenario unless $sle12sp4_minion
end

Before('@sle12sp4_client') do
  skip_this_scenario unless $sle12sp4_client
end

Before('@sle15_ssh_minion') do
  skip_this_scenario unless $sle15_ssh_minion
end

Before('@sle15_minion') do
  skip_this_scenario unless $sle15_minion
end

Before('@sle15_client') do
  skip_this_scenario unless $sle15_client
end

Before('@sle15sp1_ssh_minion') do
  skip_this_scenario unless $sle15sp1_ssh_minion
end

Before('@sle15sp1_minion') do
  skip_this_scenario unless $sle15sp1_minion
end

Before('@sle15sp1_client') do
  skip_this_scenario unless $sle15sp1_client
end

Before('@sle15sp2_ssh_minion') do
  skip_this_scenario unless $sle15sp2_ssh_minion
end

Before('@sle15sp2_minion') do
  skip_this_scenario unless $sle15sp2_minion
end

Before('@sle15sp2_client') do
  skip_this_scenario unless $sle15sp2_client
end

Before('@sle15sp3_ssh_minion') do
  skip_this_scenario unless $sle15sp3_ssh_minion
end

Before('@sle15sp3_minion') do
  skip_this_scenario unless $sle15sp3_minion
end

Before('@sle15sp3_client') do
  skip_this_scenario unless $sle15sp3_client
end

# TODO: Remove this when 15sp3 gets released and has patches
Before('@skip_for_sle15sp3') do |scenario|
  skip_this_scenario if scenario.feature.location.file.include? '15sp3'
end

Before('@skip_for_ubuntu') do |scenario|
  skip_this_scenario if scenario.feature.location.file.include? 'ubuntu'
end

Before('@skip_for_minion') do |scenario|
  skip_this_scenario if scenario.feature.location.file.include? 'minion'
end

Before('@skip_for_traditional') do |scenario|
  skip_this_scenario if scenario.feature.location.file.include? 'client'
end

# do some tests only if we have SCC credentials
Before('@scc_credentials') do
  skip_this_scenario unless $scc_credentials
end

# do some tests only if there is a private network
Before('@private_net') do
  skip_this_scenario unless $private_net
end

# do some tests only if we don't use a mirror
Before('@no_mirror') do
  skip_this_scenario if $mirror
end

# do some tests only if the server is using SUSE Manager
Before('@susemanager') do
  skip_this_scenario unless $product == 'SUSE Manager'
end

# do some tests only if the server is using Uyuni
Before('@uyuni') do
  skip_this_scenario unless $product == 'Uyuni'
end

# do test only if HTTP proxy for SUSE Manager is defined
Before('@server_http_proxy') do
  skip_this_scenario unless $server_http_proxy
end

# do test only if the registry is available
Before('@no_auth_registry') do
  skip_this_scenario unless $no_auth_registry
end

# do test only if the registry with authentication is available
Before('@auth_registry') do
  skip_this_scenario unless $auth_registry
end

# do test only if we want to run long tests
Before('@long_test') do
  skip_this_scenario unless $long_tests_enabled
end

# have more infos about the errors
def debug_server_on_realtime_failure
  puts '=> /var/log/rhn/rhn_web_ui.log'
  out, _code = $server.run("tail -n20 /var/log/rhn/rhn_web_ui.log | awk -v limit=\"$(date --date='5 minutes ago' '+%Y-%m-%d %H:%M:%S')\" ' $0 > limit'")
  out.each_line do |line|
    puts line.to_s
  end
  puts
  puts '=> /var/log/rhn/rhn_web_api.log'
  out, _code = $server.run("tail -n20 /var/log/rhn/rhn_web_api.log | awk -v limit=\"$(date --date='5 minutes ago' '+%Y-%m-%d %H:%M:%S')\" ' $0 > limit'")
  out.each_line do |line|
    puts line.to_s
  end
  puts
end
