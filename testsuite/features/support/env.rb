# Copyright (c) 2010-2026 SUSE LLC
# Licensed under the terms of the MIT license.

require 'English'
require 'rubygems'
require 'tmpdir'
require 'base64'
require 'capybara'
require 'capybara/cucumber'
require 'cucumber'
# require 'simplecov'
require 'minitest/autorun'
require 'minitest/unit'
require 'securerandom'
require 'selenium-webdriver'
require 'multi_test'
require 'set'
require 'timeout'
require_relative 'code_coverage'
require_relative 'quality_intelligence'
require_relative 'remote_nodes_env'
require_relative 'commonlib'
require_relative 'navigation_step_helper'

$stdout.puts("Using Ruby version: #{RUBY_VERSION}")

# code coverage analysis
# SimpleCov.start

if ENV['DEBUG']
  $debug_mode = true
  $stdout.puts('DEBUG MODE ENABLED.')
end
if ENV['REDIS_HOST'] && ENV.fetch('CODE_COVERAGE', false)
  $code_coverage_mode = true
  $stdout.puts('CODE COVERAGE MODE ENABLED.')
end
if ENV.fetch('QUALITY_INTELLIGENCE', false)
  $quality_intelligence_mode = true
  $stdout.puts('QUALITY INTELLIGENCE MODE ENABLED.')
end

# Context per feature
$context = {}

# Other global variables
$pxeboot_mac = ENV.fetch('PXEBOOT_MAC', nil)
$pxeboot_image = ENV.fetch('PXEBOOT_IMAGE', nil) || 'sles15sp3o'
$sle15sp6_terminal_mac = ENV.fetch('SLE15SP6_TERMINAL_MAC', nil)
$sle15sp7_terminal_mac = ENV.fetch('SLE15SP7_TERMINAL_MAC', nil)
$private_net = ENV.fetch('PRIVATENET', nil) if ENV['PRIVATENET']
$mirror = ENV.fetch('MIRROR', nil)
$server_http_proxy = ENV.fetch('SERVER_HTTP_PROXY', nil) if ENV['SERVER_HTTP_PROXY']
$custom_download_endpoint = ENV.fetch('CUSTOM_DOWNLOAD_ENDPOINT', nil) if ENV['CUSTOM_DOWNLOAD_ENDPOINT']
$build_sources = ENV.fetch('BUILD_SOURCES', nil) if ENV['BUILD_SOURCES']
$no_auth_registry = ENV.fetch('NO_AUTH_REGISTRY', nil) if ENV['NO_AUTH_REGISTRY']
$auth_registry = ENV.fetch('AUTH_REGISTRY', nil) if ENV['AUTH_REGISTRY']
$current_user = 'admin'
$current_password = 'admin'
$chromium_dev_tools = ENV.fetch('REMOTE_DEBUG', false)
$chromium_dev_port = 9222 + ENV['TEST_ENV_NUMBER'].to_i
$use_salt_bundle = ENV.fetch('USE_SALT_BUNDLE', true)

# maximal wait before giving up
# the tests return much before that delay in case of success
$stdout.sync = true
STARTTIME = Time.new.to_i
Capybara.default_max_wait_time = ENV['CAPYBARA_TIMEOUT'] ? ENV['CAPYBARA_TIMEOUT'].to_i : 10
DEFAULT_TIMEOUT = ENV['DEFAULT_TIMEOUT'] ? ENV['DEFAULT_TIMEOUT'].to_i : 250
$is_cloud_provider = ENV['PROVIDER'].include? 'aws'
$is_gh_validation = ENV['PROVIDER'].include? 'podman'
$is_containerized_server = %w[k3s podman].include? ENV.fetch('CONTAINER_RUNTIME', '')
$is_transactional_server = transactional_system?('server', runs_in_container: false)
$is_using_build_image = ENV.fetch('IS_USING_BUILD_IMAGE', false)
$is_using_scc_repositories = (ENV.fetch('IS_USING_SCC_REPOSITORIES', 'False') != 'False')
$catch_timeout_message = (ENV.fetch('CATCH_TIMEOUT_MESSAGE', 'False') == 'True')
$beta_enabled = (ENV.fetch('BETA_ENABLED', 'False') == 'True')
$api_protocol = ENV.fetch('API_PROTOCOL', nil) if ENV['API_PROTOCOL'] # force the API protocol to be used. You can use 'http' or 'xmlrpc'

# QAM and Build Validation pipelines will provide a json file including all custom (MI) repositories
custom_repos_path = "#{File.dirname(__FILE__)}/../upload_files/custom_repositories.json"
if File.exist?(custom_repos_path)
  custom_repos_file = File.read(custom_repos_path)
  $custom_repositories = JSON.parse(custom_repos_file)
  $build_validation = true
end

# Fix a problem with minitest and cucumber options passed through rake
MultiTest.disable_autorun

# register chromedriver in headless mode
def capybara_register_driver
  Capybara.register_driver :selenium_chrome_headless do |app|
    # WORKAROUND failure at Scenario: Test IPMI functions: increase from 60 s to 180 s
    client = Selenium::WebDriver::Remote::Http::Default.new(open_timeout: 30, read_timeout: 240)
    chrome_options = Selenium::WebDriver::Chrome::Options.new(
      args: %w[
        --disable-dev-shm-usage
        --ignore-certificate-errors
        --window-size=2048,2048
        --js-flags=--max-old-space-size=2048
        --no-sandbox
        --disable-notifications
      ]
    )
    chrome_options.args << '--headless=new' unless $debug_mode
    chrome_options.args << "--remote-debugging-port=#{$chromium_dev_port}" if $chromium_dev_tools
    chrome_options.add_argument("--user-data-dir=/tmp/chrome_profile_#{Process.pid}_#{Time.now.to_i}") if $is_cloud_provider

    chrome_options.add_preference('prompt_for_download', false)
    chrome_options.add_preference('download.default_directory', '/tmp/downloads')
    chrome_options.add_preference('unhandledPromptBehavior', 'accept')
    chrome_options.add_preference('unexpectedAlertBehaviour', 'accept')

    Capybara::Selenium::Driver.new(app, browser: :chrome, options: chrome_options, http_client: client)
  end
end

# register chromedriver headless mode
$capybara_driver = capybara_register_driver
Selenium::WebDriver.logger.level = :error
Capybara.default_driver = :selenium_chrome_headless
Capybara.javascript_driver = :selenium_chrome_headless
Capybara.default_normalize_ws = true
Capybara.enable_aria_label = true
Capybara.automatic_label_click = true
Capybara.app_host = "https://#{ENV.fetch('SERVER', nil)}"
Capybara.server_port = 8888 + ENV['TEST_ENV_NUMBER'].to_i
$stdout.puts "Capybara APP Host: #{Capybara.app_host}:#{Capybara.server_port}"

# enable minitest assertions in steps
World(MiniTest::Assertions)

# Initialize the API client
$api_test = new_api_client

# Init CodeCoverage Handler
$code_coverage = CodeCoverage.new if $code_coverage_mode

# Init Quality Intelligence Handler
$quality_intelligence = QualityIntelligence.new if $quality_intelligence_mode

# Define the current feature scope
Before do |scenario|
  $feature_scope = scenario.location.file.split(%r{(\.feature|/)})[-2]
end

# Embed a screenshot after each failed scenario
After do |scenario|
  current_epoch = Time.new.to_i
  log "This scenario took: #{current_epoch - @scenario_start_time} seconds"
  if scenario.failed?
    begin
      if scenario.exception.is_a?(Selenium::WebDriver::Error::WebDriverError)
        log "Caught web driver error: #{scenario.exception.message}"
        Capybara.current_session.driver.quit
        visit Capybara.app_host
        log 'Web driver has been restarted'
      elsif web_session_is_active?
        handle_screenshot_and_relog(scenario, current_epoch)
      else
        warn 'There is no active web session; unable to take a screenshot or relog.'
      end
    ensure
      print_server_logs
    end
  end
  page.instance_variable_set(:@touched, false) if capybara_session_created?
end

# Test if capybara session instance was created
def capybara_session_created?
  return false if Capybara::Session.nil?

  Capybara::Session.instance_created?
end

# Test if web session is open
def web_session_is_active?
  return false unless capybara_session_created?

  page.has_selector?('header') || page.has_selector?('#username-field')
end

# Take a screenshot and try to log back at suse manager server
def handle_screenshot_and_relog(scenario, current_epoch)
  Dir.mkdir('screenshots') unless File.directory?('screenshots')
  path = "screenshots/#{scenario.name.tr(' ./', '_')}.png"
  begin
    click_details_if_present
    page.driver.browser.save_screenshot(path)
    attach path, 'image/png'
    # Attach additional information
    attach "#{Time.at(@scenario_start_time).strftime('%H:%M:%S:%L')} - #{Time.at(current_epoch).strftime('%H:%M:%S:%L')} | Current URL: #{current_url}", 'text/plain'
  rescue StandardError => e
    warn "Error message: #{e.message}"
  ensure
    relog_and_visit_previous_url
  end
end

# Try to get the minion details when on minion page
def click_details_if_present
  return unless page.has_content?('Bootstrap Minions', wait: 0) && page.has_content?('Details', wait: 0)

  begin
    click_button('Details')
  rescue Capybara::ElementNotFound
    log "Button 'Details' not found on the page."
  rescue Capybara::ElementNotInteractable
    log "Button 'Details' found but not interactable."
  end
end

# Relog and visit the previous URL
def relog_and_visit_previous_url
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      previous_url = current_url
      step %(I am authorized as "#{$current_user}" with password "#{$current_password}")
      visit previous_url
    end
  rescue Timeout::Error
    warn "Timed out while attempting to relog and visit the previous URL: #{current_url}"
  rescue StandardError => e
    warn "An error occurred while relogging and visiting the previous URL: #{e.message}"
  end
end

# Process the code coverage for each feature when it ends
def process_code_coverage
  return if $feature_path.nil?

  feature_filename = $feature_path.split(%r{(\.feature|/)})[-2]
  $code_coverage.jacoco_dump(feature_filename)
  $code_coverage.push_feature_coverage(feature_filename)
end

# Dump feature code coverage into a Redis DB before we run next feature
Before do |scenario|
  next unless $code_coverage_mode

  # Initialize $feature_path if that's the first feature
  $feature_path ||= scenario.location.file

  # Skip if still in the same feature file
  next if $feature_path == scenario.location.file

  # Runs only if a new feature file starts
  process_code_coverage
  $feature_path = scenario.location.file
end

# Dump feature code coverage into a Redis DB, for the last feature
AfterAll do
  next unless $code_coverage_mode

  process_code_coverage
end

# get the Cobbler log output when it fails
After('@scope_cobbler') do |scenario|
  if scenario.failed?
    $stdout.puts '=> /var/log/cobbler/cobbler.log'
    out, _code = get_target('server').run('tail -n20 /var/log/cobbler/cobbler.log')
    out.each_line do |line|
      $stdout.puts line.to_s
    end
    $stdout.puts
  end
end

AfterStep do
  next unless capybara_session_created?

  log 'Timeout: Waiting AJAX transition' if has_css?('.senna-loading', wait: 0) && !has_no_css?('.senna-loading', wait: 30)
end

Before do
  current_time = Time.new
  @scenario_start_time = current_time.to_i
  log "This scenario ran at: #{current_time}\n"
end

Before('@skip') do
  skip_this_scenario
end

Before('@skip_known_issue') do
  raise Core::Test::Result::Failed, 'This scenario is known to fail, skipping it'
end

# Create a user for each feature
Before do |scenario|
  feature_path = scenario.location.file
  $feature_filename = feature_path.split(%r{(\.feature|/)})[-2]
  next if get_context('user_created') == true

  # Create own user based on feature filename. Exclude core, reposync, finishing and build_validation features.
  unless feature_path.match?(/core|reposync|finishing|build_validation/)
    step %(I create a user with name "#{$feature_filename}" and password "linux")
    add_context('user_created', true)
  end
end

# do some tests only if the corresponding node exists
Before('@proxy') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['proxy']
end

Before('@run_if_proxy_transactional_or_slmicro61_minion') do
  skip_this_scenario unless suse_proxy_transactional? || ENV.key?(ENV_VAR_BY_HOST['slmicro61_minion'])
end

Before('@run_if_proxy_not_transactional_or_sles15sp7_minion') do
  skip_this_scenario unless suse_proxy_non_transactional? || ENV.key?(ENV_VAR_BY_HOST['sle15sp7_minion'])
end

Before('@sle_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle_minion']
end

Before('@rhlike_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['rhlike_minion']
end

Before('@deblike_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['deblike_minion']
end

Before('@pxeboot_minion') do
  skip_this_scenario unless $pxeboot_mac
end

Before('@ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['ssh_minion']
end

Before('@build_host') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['build_host']
end

Before('@alma8_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['alma8_minion']
end

Before('@alma8_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['alma8_ssh_minion']
end

Before('@alma9_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['alma9_minion']
end

Before('@alma9_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['alma9_ssh_minion']
end

Before('@amazon2023_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['amazon2023_minion']
end

Before('@amazon2023_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['amazon2023_ssh_minion']
end

Before('@centos7_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['centos7_minion']
end

Before('@centos7_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['centos7_ssh_minion']
end

Before('@liberty9_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['liberty9_minion']
end

Before('@liberty9_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['liberty9_ssh_minion']
end

Before('@oracle9_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['oracle9_minion']
end

Before('@oracle9_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['oracle9_ssh_minion']
end

Before('@rhel9_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['rhel9_minion']
end

Before('@rhel9_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['rhel9_ssh_minion']
end

Before('@rocky8_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['rocky8_minion']
end

Before('@rocky8_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['rocky8_ssh_minion']
end

Before('@rocky9_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['rocky9_minion']
end

Before('@rocky9_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['rocky9_ssh_minion']
end

Before('@ubuntu2204_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['ubuntu2204_minion']
end

Before('@ubuntu2204_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['ubuntu2204_ssh_minion']
end

Before('@ubuntu2404_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['ubuntu2404_minion']
end

Before('@ubuntu2404_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['ubuntu2404_ssh_minion']
end

Before('@debian12_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['debian12_minion']
end

Before('@debian12_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['debian12_ssh_minion']
end

Before('@sle12sp5_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle12sp5_minion']
end

Before('@sle12sp5_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle12sp5_ssh_minion']
end

Before('@sle15sp3_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle15sp3_minion']
end

Before('@sle15sp3_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle15sp3_ssh_minion']
end

Before('@sle15sp4_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle15sp4_minion']
end

Before('@sle15sp4_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle15sp4_ssh_minion']
end

Before('@sle15sp5_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle15sp5_minion']
end

Before('@sle15sp5_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle15sp5_ssh_minion']
end

Before('@sle15sp6_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle15sp6_minion']
end

Before('@sle15sp6_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle15sp6_ssh_minion']
end

Before('@sle15sp7_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle15sp7_minion']
end

Before('@sle15sp7_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle15sp7_ssh_minion']
end

Before('@opensuse156arm_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['opensuse156arm_minion']
end

Before('@opensuse156arm_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['opensuse156arm_ssh_minion']
end

Before('@sle15sp5s390_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle15sp5s390_minion']
end

Before('@sle15sp5s390_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle15sp5s390_ssh_minion']
end

Before('@salt_migration_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['salt_migration_minion']
end

Before('@slemicro') do |scenario|
  skip_this_scenario unless scenario.location.file.include? 'slemicro'
end

Before('@slemicro51_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['slemicro51_minion']
end

Before('@slemicro51_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['slemicro51_ssh_minion']
end

Before('@slemicro52_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['slemicro52_minion']
end

Before('@slemicro52_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['slemicro52_ssh_minion']
end

Before('@slemicro53_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['slemicro53_minion']
end

Before('@slemicro53_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['slemicro53_ssh_minion']
end

Before('@slemicro54_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['slemicro54_minion']
end

Before('@slemicro54_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['slemicro54_ssh_minion']
end

Before('@slemicro55_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['slemicro55_minion']
end

Before('@slemicro55_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['slemicro55_ssh_minion']
end

Before('@slmicro60_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['slmicro60_minion']
end

Before('@slmicro60_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['slmicro60_ssh_minion']
end

Before('@slmicro61_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['slmicro61_minion']
end

Before('@slmicro61_ssh_minion') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['slmicro61_ssh_minion']
end

Before('@sle15sp6_buildhost') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle15sp6_buildhost']
end

Before('@sle15sp7_buildhost') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['sle15sp7_buildhost']
end

Before('@monitoring_server') do
  skip_this_scenario unless ENV.key? ENV_VAR_BY_HOST['monitoring_server']
end

Before('@sle15sp6_terminal') do
  skip_this_scenario unless $sle15sp6_terminal_mac
end

Before('@sle15sp7_terminal') do
  skip_this_scenario unless $sle15sp6_terminal_mac
end

Before('@suse_minion') do |scenario|
  filename = scenario.location.file
  skip_this_scenario unless filename.include? 'minion'
  skip_this_scenario unless (filename.include? 'sle') || (filename.include? 'suse')
end

Before('@transactional_minion') do |scenario|
  skip_this_scenario unless (scenario.location.file.include? 'slemicro') || (scenario.location.file.include? 'slmicro')
end

Before('@skip_for_debian') do |scenario|
  skip_this_scenario if scenario.location.file.include? 'debian'
end

Before('@skip_for_rocky9') do |scenario|
  skip_this_scenario if scenario.location.file.include? 'rocky9'
end

Before('@skip_for_alma9') do |scenario|
  skip_this_scenario if scenario.location.file.include? 'alma9'
end

Before('@skip_for_minion') do |scenario|
  skip_this_scenario if scenario.location.file.include? 'minion'
end

Before('@skip_for_transactional_minion') do |scenario|
  skip_this_scenario if scenario.location.file.include?('slemicro') || scenario.location.file.include?('slmicro')
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
  skip_this_scenario unless product == 'SUSE Manager'
end

# do some tests only if the server is using Uyuni
Before('@uyuni') do
  skip_this_scenario unless product == 'Uyuni'
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

# do test only if custom downlad endpoint for packages is defined
Before('@custom_download_endpoint') do
  skip_this_scenario unless $custom_download_endpoint
end

# do test only if the registry is available
Before('@no_auth_registry') do
  skip_this_scenario unless $no_auth_registry
end

# do test only if the registry with authentication is available
Before('@auth_registry') do
  skip_this_scenario unless $auth_registry
end

# skip tests if executed in cloud environment
Before('@skip_if_cloud') do
  skip_this_scenario if $is_cloud_provider
end

# skip tests if executed in cloud environment
Before('@cloud') do
  skip_this_scenario unless $is_cloud_provider
end

# skip tests if executed in containers for the GitHub validation
Before('@skip_if_github_validation') do
  skip_this_scenario if $is_gh_validation
end

# skip tests if the server runs in a container
Before('@skip_if_containerized_server') do
  skip_this_scenario if $is_containerized_server
end

# do test only if we have a containerized server
Before('@containerized_server') do
  skip_this_scenario unless $is_containerized_server
end

# skip tests if the server runs on a transactional base OS
Before('@skip_if_transactional_server') do
  skip_this_scenario if $is_transactional_server
end

# do tests only if the server runs on a transactional base OS
Before('@transactional_server') do
  skip_this_scenario unless $is_transactional_server
end

# only test for excessive SCC accesses if SCC access is being logged
Before('@srv_scc_access_logging') do
  skip_this_scenario unless scc_access_logging_grain?
end

# do test only if we have beta channels enabled
Before('@beta') do
  skip_this_scenario unless $beta_enabled
end

# check whether the server has the scc_access_logging variable set
def scc_access_logging_grain?
  cmd = 'grep "\"scc_access_logging\": true" /etc/salt/grains'
  _out, code = get_target('server').run(cmd, check_errors: false)
  code.zero?
end

# have more infos about the errors
def print_server_logs
  $stdout.puts '=> /var/log/rhn/rhn_web_ui.log'
  out, _code = get_target('server').run('tail -n20 /var/log/rhn/rhn_web_ui.log | awk -v limit="$(date --date="5 minutes ago" "+%Y-%m-%d %H:%M:%S")" \'substr($0, 1, 19) > limit\'')
  out.each_line do |line|
    $stdout.puts line.to_s
  end
  $stdout.puts
  $stdout.puts '=> /var/log/rhn/rhn_web_api.log'
  out, _code = get_target('server').run('tail -n20 /var/log/rhn/rhn_web_api.log | awk -v limit="$(date --date="5 minutes ago" "+%Y-%m-%d %H:%M:%S")" \'substr($0, 2, 19) > limit\'')
  out.each_line do |line|
    $stdout.puts line.to_s
  end
  $stdout.puts
end
