# Copyright (c) 2010-2021 SUSE LLC
# Licensed under the terms of the MIT license.

require 'English'
require 'rubygems'
require 'tmpdir'
require 'base64'
require 'capybara'
require 'capybara/cucumber'
require 'cucumber'
#require 'simplecov'
require 'minitest/autorun'
require 'securerandom'
require 'selenium-webdriver'
require 'multi_test'

## code coverage analysis
# SimpleCov.start

server = ENV['SERVER']
$debug_mode = true if ENV['DEBUG']

# maximal wait before giving up
# the tests return much before that delay in case of success
STDOUT.sync = true
STARTTIME = Time.new.to_i
Capybara.default_max_wait_time = ENV['CAPYBARA_TIMEOUT'] ? ENV['CAPYBARA_TIMEOUT'].to_i : 10
DEFAULT_TIMEOUT = ENV['DEFAULT_TIMEOUT'] ? ENV['DEFAULT_TIMEOUT'].to_i : 250

# QAM and Build Validation pipelines will provide a json file including all custom (MI) repositories
custom_repos_path = File.dirname(__FILE__) + '/../upload_files/' + 'custom_repositories.json'
if File.exist?(custom_repos_path)
  custom_repos_file = File.read(custom_repos_path)
  $custom_repositories = JSON.parse(custom_repos_file)
  $build_validation = true
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

Selenium::WebDriver.logger.level = :error unless $debug_mode
Capybara.default_driver = :headless_chrome
Capybara.javascript_driver = :headless_chrome
Capybara.default_normalize_ws = true
Capybara.app_host = "https://#{server}"
Capybara.server_port = 8888 + ENV['TEST_ENV_NUMBER'].to_i
STDOUT.puts "Capybara APP Host: #{Capybara.app_host}:#{Capybara.server_port}"

# enable minitest assertions in steps
enable_assertions

# embed a screenshot after each failed scenario
After do |scenario|
  current_epoch = Time.new.to_i
  STDOUT.puts "This scenario took: #{current_epoch - @scenario_start_time} seconds"
  if scenario.failed?
    begin
      Dir.mkdir("screenshots") unless File.directory?("screenshots")
      path = "screenshots/#{scenario.name.tr(' ./', '_')}.png"
      page.driver.browser.save_screenshot(path)
      attach path, 'image/png'
      attach current_url, 'text/plain'
    rescue StandardError => e
      warn e.message
    ensure
      debug_server_on_realtime_failure
      previous_url = current_url
      step %(I am authorized for the "Admin" section)
      visit previous_url
    end
  end
  page.instance_variable_set(:@touched, false)
end

AfterStep do
  if has_css?('.senna-loading', wait: 0)
    STDOUT.puts "WARN: Step ends with an ajax transition not finished, let's wait a bit!"
    raise StandardError, 'Timeout: Waiting AJAX transition' unless has_no_css?('.senna-loading', wait: 20)
  end
end

Before do
  current_time = Time.new
  @scenario_start_time = current_time.to_i
  STDOUT.puts "This scenario ran at: #{current_time}\n"
end

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

Before('@debian11_minion') do
  skip_this_scenario unless $debian11_minion
end

Before('@debian11_ssh_minion') do
  skip_this_scenario unless $debian11_ssh_minion
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

Before('@sle12sp5_ssh_minion') do
  skip_this_scenario unless $sle12sp5_ssh_minion
end

Before('@sle12sp5_minion') do
  skip_this_scenario unless $sle12sp5_minion
end

Before('@sle12sp5_client') do
  skip_this_scenario unless $sle12sp5_client
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

Before('@sle11sp4_buildhost') do
  skip_this_scenario unless $sle11sp4_buildhost
end

Before('@sle11sp3_terminal') do
  skip_this_scenario unless $sle11sp3_terminal_mac
end

Before('@sle12sp5_buildhost') do
  skip_this_scenario unless $sle12sp5_buildhost
end

Before('@sle12sp5_terminal') do
  skip_this_scenario unless $sle12sp5_terminal_mac
end

Before('@sle15sp3_buildhost') do
  skip_this_scenario unless $sle15sp3_buildhost
end

Before('@sle15sp3_terminal') do
  skip_this_scenario unless $sle15sp3_terminal_mac
end

Before('@opensuse153arm_minion') do
  skip_this_scenario unless $opensuse153arm_minion
end

Before('@skip_for_debianlike') do |scenario|
  filename = scenario.location.file
  skip_this_scenario if (filename.include? 'ubuntu') || (filename.include? 'debian')
end

Before('@skip_for_minion') do |scenario|
  skip_this_scenario if scenario.location.file.include? 'minion'
end

Before('@skip_for_traditional') do |scenario|
  skip_this_scenario if scenario.location.file.include? 'client'
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

# do some tests only if we are using salt bundle
Before('@salt_bundle') do
  skip_this_scenario unless $use_salt_bundle
end

# do some tests only if we are using salt bundle
Before('@skip_if_salt_bundle') do
  skip_this_scenario if $use_salt_bundle
end

# do test only if HTTP proxy for Uyuni is defined
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

# have more infos about the errors
def debug_server_on_realtime_failure
  STDOUT.puts '=> /var/log/rhn/rhn_web_ui.log'
  out, _code = $server.run("tail -n20 /var/log/rhn/rhn_web_ui.log | awk -v limit=\"$(date --date='5 minutes ago' '+%Y-%m-%d %H:%M:%S')\" ' $0 > limit'")
  out.each_line do |line|
    STDOUT.puts line.to_s
  end
  STDOUT.puts
  STDOUT.puts '=> /var/log/rhn/rhn_web_api.log'
  out, _code = $server.run("tail -n20 /var/log/rhn/rhn_web_api.log | awk -v limit=\"$(date --date='5 minutes ago' '+%Y-%m-%d %H:%M:%S')\" ' $0 > limit'")
  out.each_line do |line|
    STDOUT.puts line.to_s
  end
  STDOUT.puts
end

# get the Cobbler log output when we test it
After('@scope_cobbler') do
  STDOUT.puts '=> /var/log/cobbler/cobbler.log'
  out, _code = $server.run("tail -n20 /var/log/cobbler/cobbler.log")
  out.each_line do |line|
    STDOUT.puts line.to_s
  end
  STDOUT.puts
end
