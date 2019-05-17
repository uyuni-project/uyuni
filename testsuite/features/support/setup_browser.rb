# Copyright (c) 2010-2019 SUSE LLC
# Licensed under the terms of the MIT license.

require 'English'
require 'rubygems'
require 'tmpdir'
require 'base64'
require 'capybara'
require 'capybara/cucumber'
require 'simplecov'
require 'minitest/unit'
require 'securerandom'
require 'selenium-webdriver'

## codecoverage gem
SimpleCov.start
server = ENV['SERVER']
# maximal wait before giving up
# the tests return much before that delay in case of success
$stdout.sync = true
STARTTIME = Time.new.to_i
Capybara.default_max_wait_time = 10
DEFAULT_TIMEOUT = 250
CLICK_TIMEOUT = Capybara.default_max_wait_time * 2

def enable_assertions
  # include assertion globally
  World(MiniTest::Assertions)
end

# register chromedriver headless mode
Capybara.register_driver(:headless_chrome) do |app|
  capabilities = Selenium::WebDriver::Remote::Capabilities.chrome(
    chromeOptions: { args: %w[headless disable-gpu window-size=1920,1080, js-flags=--max_old_space_size=2048 no-sandbox] }
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

# embed a screenshot after each failed scenario
After do |scenario|
  if scenario.failed?
    img_name = "#{scenario.name.tr(' ', '_')}.png"
    save_screenshot(img_name)
    # embed the image name in the cucumber HTML report
    embed(img_name, 'image/png')
    debug_server_on_realtime_failure
  end
end

# enable minitest assertions in steps
enable_assertions

# do some tests only if the corresponding node exists
Before('@proxy') do |scenario|
  scenario.skip_invoke! unless $proxy
end
Before('@centos_minion') do |scenario|
  scenario.skip_invoke! unless $ceos_minion
end
Before('@ubuntu_minion') do |scenario|
  scenario.skip_invoke! unless $ubuntu_minion
end
Before('@pxeboot_minion') do |scenario|
  scenario.skip_invoke! unless $pxeboot_mac
end
Before('@ssh_minion') do |scenario|
  scenario.skip_invoke! unless $ssh_minion
end
Before('@virthost_kvm') do |scenario|
  scenario.skip_invoke! unless $kvm_server
end
Before('@virthost_xen') do |scenario|
  scenario.skip_invoke! unless $xen_server
end

# do some tests only if node is of a given type
Before('@sle15_minion') do |scenario|
  scenario.skip_invoke! unless $sle15_minion
end

# do some tests only if there is a private network
Before('@private_net') do |scenario|
  scenario.skip_invoke! unless $private_net
end

# do some tests only if we don't use a mirror
Before('@no_mirror') do |scenario|
  scenario.skip_invoke! if $mirror
end

# do some tests only if the server is using SUSE Manager
Before('@susemanager') do |scenario|
  scenario.skip_invoke! unless $product == 'SUSE Manager'
end

# do test only if HTTP proxy for SUSE Manager is defined
Before('@server_http_proxy') do |scenario|
  scenario.skip_invoke! unless $server_http_proxy
end

# have more infos about the errors
def debug_server_on_realtime_failure
  puts
  puts '#' * 51 + ' /var/log/rhn/rhn_web_ui.log ' + '#' * 51
  out, _code = $server.run('tail -n35 /var/log/rhn/rhn_web_ui.log')
  puts out
  puts '#' * 131
end
