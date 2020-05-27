# Copyright (c) 2010-2020 SUSE LLC
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

# maximal wait before giving up
# the tests return much before that delay in case of success
$stdout.sync = true
STARTTIME = Time.new.to_i
Capybara.default_max_wait_time = 10
DEFAULT_TIMEOUT = 250

# QAM test-suite will provide a json including all client repositories with format :
# {"client_type" : { "salt" : "salt_repo" , "traditional" : "traditional_repo" }}
mu_repos_path = File.dirname(__FILE__) + '/../upload_files/' + 'mu_repositories.json'
if File.exist?(mu_repos_path)
  mu_repos_file = File.read(mu_repos_path)
  $mu_repositories = JSON.parse(mu_repos_file)
  Capybara.default_max_wait_time = 30
  DEFAULT_TIMEOUT = 1200
  $qam_test = true
end

def enable_assertions
  # include assertion globally
  World(MiniTest::Assertions)
end

# Fix a problem with minitest and cucumber options passed through rake
MultiTest.disable_autorun

# register chromedriver headless mode
Capybara.register_driver(:headless_chrome) do |app|
  capabilities = Selenium::WebDriver::Remote::Capabilities.chrome(
    chromeOptions: { args: %w[headless no-sandbox disable-dev-shm-usage disable-gpu window-size=2048,2048, js-flags=--max_old_space_size=2048] }
  )

  Capybara::Selenium::Driver.new(
    app,
    browser: :chrome,
    desired_capabilities: capabilities
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
    end
  end
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

Before('@skip_for_ubuntu') do |scenario|
  skip_this_scenario if scenario.feature.location.file.include? 'ubuntu'
end

Before('@skip_for_minion') do |scenario|
  skip_this_scenario if scenario.feature.location.file.include? 'minion'
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
