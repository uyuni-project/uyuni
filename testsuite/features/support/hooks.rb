# Copyright (c) 2010-2021 SUSE LLC
# Licensed under the terms of the MIT license.

require 'English'
require 'rubygems'
require 'tmpdir'
require 'base64'
require 'capybara'
require 'capybara/cucumber'
require 'selenium-webdriver'

# Save the timestamp when we start a scenario and log it in the report
Before do
  current_time = Time.new
  @scenario_start_time = current_time.to_i
  puts "This scenario ran at: #{current_time} - #{@scenario_start_time - STARTTIME} seconds since start"
end

# Save the Capybara driver in a global variable to be accessed inside our Ruby Modules
Before do
  $capybara_driver = page.driver
end

# embed a screenshot after each failed scenario
After do |scenario|
  # Calculate and log the time that a scenario took
  current_epoch = Time.new.to_i
  puts "This scenario took: #{current_epoch - @scenario_start_time} seconds"

  if scenario.failed?
    begin
      img_path = "screenshots/#{scenario.name.tr(' ./', '_')}.png"
      if page.driver.browser.respond_to?(:save_screenshot)
        Dir.mkdir('screenshots') unless File.directory?('screenshots')
        page.driver.browser.save_screenshot(img_path)
      else
        save_screenshot(img_path)
      end
      # embed the image name in the cucumber HTML report
      embed(current_url, 'text/plain')
      embed(img_path, 'image/png')
    rescue StandardError => e
      puts("Error taking a screenshot: #{e.message}")
    ensure
      debug_server_on_realtime_failure
      previous_url = current_url
      step(%(I am authorized for the "Admin" section))
      visit(previous_url)
    end
  end
  page.instance_variable_set(:@touched, false)
end

AfterStep do
  if all('.senna-loading').any?
    puts "WARN: Step ends with an ajax transition not finished, let's wait a bit!"
    raise(Timeout::Error, 'Timeout: Waiting AJAX transition') unless has_no_css?('.senna-loading')
  end
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

Before('@skip_for_debianlike') do |scenario|
  filename = scenario.feature.location.file
  skip_this_scenario if filename.include?('ubuntu') || filename.include?('debian')
end

Before('@skip_for_minion') do |scenario|
  skip_this_scenario if scenario.feature.location.file.include?('minion')
end

Before('@skip_for_traditional') do |scenario|
  skip_this_scenario if scenario.feature.location.file.include?('client')
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
