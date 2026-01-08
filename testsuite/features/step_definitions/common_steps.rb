# Copyright (c) 2010-2024 SUSE LLC.
# Licensed under the terms of the MIT license.

### This file contains all step definitions concerning general product funtionality
### as well as those which do not fit into any other category or are temporary workarounds.
###
### The definitions are divided into blocks marked with a summary headline.

require 'jwt'
require 'securerandom'
require 'pathname'

# Used for debugging purposes
When(/^I save a screenshot as "([^"]+)"$/) do |filename|
  save_screenshot(filename)
  attach File.open(filename, 'rb'), 'image/png'
end

When(/^I wait for "(\d+)" seconds?$/) do |arg1|
  sleep(arg1.to_i)
end

When(/^I mount as "([^"]+)" the ISO from "([^"]+)" in the server, validating its checksum$/) do |name, url|
  # When using a mirror it is automatically mounted at /mirror
  if $mirror
    iso_path = $is_containerized_server ? url.sub(/^https?:\/\/[^\/]+/, '/srv/mirror') : url.sub(/^https?:\/\/[^\/]+/, '/mirror')
  else
    iso_path = "/tmp/#{name}.iso"
    get_target('server').run("curl --insecure -o #{iso_path} #{url}", runs_in_container: false, timeout: 1500)
  end

  iso_dir = File.dirname(iso_path)
  original_iso_name = url.split('/').last
  checksum_path = get_checksum_path(iso_dir, original_iso_name, url)

  raise 'SHA256 checksum validation failed' unless checksum_with_file_valid?(original_iso_name, iso_path, checksum_path)

  if $is_containerized_server
    mount_point = '/srv/www/distributions'
    get_target('server').run("mkdir -p #{mount_point}")
    # this needs to be run outside the container
    get_target('server').run("mgradm distro copy #{iso_path} #{name}", runs_in_container: false, verbose: true)
    get_target('server').run("ln -s #{mount_point}/#{name} /srv/www/htdocs/pub/")
  else
    mount_point = "/srv/www/htdocs/pub/#{name}"
    cmd = "mkdir -p #{mount_point} && " \
          "grep #{iso_path} /etc/fstab || echo '#{iso_path}  #{mount_point}  iso9660  loop,ro,_netdev  0 0' >> /etc/fstab && " \
          "umount #{iso_path}; mount #{iso_path}"
    get_target('server').run(cmd, verbose: true)
  end
end

Then(/^the hostname for "([^"]*)" should be correct$/) do |host|
  node = get_target(host)
  step %(I should see a "#{node.hostname}" text)
end

Then(/^the kernel for "([^"]*)" should be correct$/) do |host|
  node = get_target(host)
  kernel_version, _code = node.run('uname -r')
  log "I should see kernel version: #{kernel_version}"
  step %(I should see a "#{kernel_version.strip}" text)
end

Then(/^the OS version for "([^"]*)" should be correct$/) do |host|
  node = get_target(host)
  os_version = node.os_version
  os_family = node.os_family
  # skip this test for Red Hat-like and Debian-like systems
  step %(I should see a "#{os_version.gsub!('-SP', ' SP')}" text) if os_family.include? 'sles'
end

Then(/^the IPv4 address for "([^"]*)" should be correct$/) do |host|
  node = get_target(host)
  ipv4_address = node.public_ip
  log "IPv4 address: #{ipv4_address}"
  step %(I should see a "#{ipv4_address}" text)
end

Then(/^the IPv6 address for "([^"]*)" should be correct$/) do |host|
  node = get_target(host)
  interface, code = node.run("ip -6 address show #{node.public_interface}")
  raise RuntimeError unless code.zero?

  lines = interface.lines
  # selects only lines with IPv6 addresses and proceeds to form an array with only those addresses
  ipv6_addresses_list = lines.grep(/2[:0-9a-f]*|fe80:[:0-9a-f]*/)
  ipv6_addresses_list.map! { |ip_line| ip_line.slice(/2[:0-9a-f]*|fe80:[:0-9a-f]*/) }

  # confirms that the IPv6 address shown on the page is part of that list and, therefore, valid
  ipv6_address = find(:xpath, '//td[text()=\'IPv6 Address:\']/following-sibling::td[1]').text
  log "IPv6 address: #{ipv6_address}"
  raise ScriptError, "List of IPv6 addresses: #{ipv6_addresses_list} doesn't include #{ipv6_address}" unless ipv6_addresses_list.include? ipv6_address
end

Then(/^the system ID for "([^"]*)" should be correct$/) do |host|
  client_id = $api_test.system.search_by_name(get_system_name(host)).first['id']
  step %(I should see a "#{client_id}" text)
end

Then(/^the system name for "([^"]*)" should be correct$/) do |host|
  system_name = get_system_name(host)
  step %(I should see a "#{system_name}" text)
end

Then(/^the uptime for "([^"]*)" should be correct$/) do |host|
  uptime = get_uptime_from_host(host)
  # rounded values to nearest integer number
  rounded_uptime_minutes = uptime[:minutes].round
  rounded_uptime_hours = uptime[:hours].round
  # needed for the library's conversion of 24h multiples plus 11 hours to consider the next day
  eleven_hours_in_seconds = 39_600 # 11 hours * 60 minutes * 60 seconds
  rounded_uptime_days = ((uptime[:seconds] + eleven_hours_in_seconds) / 86_400.0).round # 60 seconds * 60 minutes * 24 hours

  # select the text in the time HTML element associated with 'Last Booted'
  ui_uptime_text = find(:xpath, '//td[contains(text(), "Last Booted")]/following-sibling::td/time').text
  raise ScriptError, "Uptime text for host '#{host}' not found" unless ui_uptime_text

  # we may have to accept as valid slightly different messages to account for rounding operations resulting in off by one errors
  valid_uptime_messages = []
  diffs = [-1, 0, +1]
  # the moment.js library being used has some weird rules, which these conditionals follow
  if (uptime[:days] >= 1 && rounded_uptime_days < 2) || (uptime[:days] < 1 && rounded_uptime_hours >= 22) # shows "a day ago" after 22 hours and before it's been 1.5 days
    valid_uptime_messages << 'a day ago'
  elsif rounded_uptime_hours > 1 && rounded_uptime_hours <= 21
    valid_uptime_messages = diffs.map { |n| "#{rounded_uptime_hours + n} hours ago" }
    valid_uptime_messages.map! { |time| time == '1 hours ago' ? 'an hour ago' : time }
  elsif rounded_uptime_minutes >= 45 && rounded_uptime_hours == 1 # shows "an hour ago" from 45 minutes onwards up to 1.5 hours
    valid_uptime_messages << 'an hour ago'
  elsif rounded_uptime_minutes > 1 && rounded_uptime_hours <= 1
    valid_uptime_messages += diffs.map { |n| "#{rounded_uptime_minutes + n} minutes ago" }
    valid_uptime_messages.map! { |time| time == '1 minutes ago' ? 'a minute ago' : time }
  elsif uptime[:seconds] >= 45 && rounded_uptime_minutes == 1
    valid_uptime_messages << 'a minute ago'
  elsif uptime[:seconds] < 45
    valid_uptime_messages << 'a few seconds ago'
  elsif rounded_uptime_days < 25 # shows "a month ago" from 25 days onwards
    valid_uptime_messages += diffs.map { |n| "#{rounded_uptime_days + n} days ago" }
    valid_uptime_messages.map! { |time| time == '1 days ago' ? 'a day ago' : time }
  else
    valid_uptime_messages << 'a month ago'
  end

  check = valid_uptime_messages.find { |message| message == ui_uptime_text }
  raise ScriptError, "Expected uptime message to be one of #{valid_uptime_messages} - found '#{ui_uptime_text}'" unless check
end

# events

When(/^I wait until event "([^"]*)" is completed$/) do |event|
  step %(I wait at most #{DEFAULT_TIMEOUT} seconds until event "#{event}" is completed)
end

When(/^I wait (\d+) seconds until the event is picked up and (\d+) seconds until the event "([^"]*)" is completed$/) do |pickup_timeout, complete_timeout, event|
  # The code below is not perfect because there might be other events with the
  # same name in the events history - however, that's the best we have so far.
  steps %(
    When I follow "Events"
    And I wait until I see "Pending Events" text
    And I follow "Pending"
    And I wait until I see "Pending Events" text
    And I wait at most #{pickup_timeout} seconds until I do not see "#{event}" text, refreshing the page
    And I follow "History"
    And I wait until I see "System History" text
    And I wait until I see "#{event}" text, refreshing the page
    And I follow first "#{event}"
    And I wait until I see "This action will be executed after" text
    And I wait until I see "#{event}" text
    And I wait at most #{complete_timeout} seconds until the event is completed, refreshing the page
  )
end

When(/^I wait at most (\d+) seconds until event "([^"]*)" is completed$/) do |final_timeout, event|
  step %(I wait 180 seconds until the event is picked up and #{final_timeout} seconds until the event "#{event}" is completed)
end

When(/^I wait until I see the event "([^"]*)" completed during last minute, refreshing the page$/) do |event|
  repeat_until_timeout(message: "Couldn't find the event #{event}") do
    now = Time.now
    current_minute = now.strftime('%H:%M')
    previous_minute = (now - 60).strftime('%H:%M')
    begin
      break if find(:xpath, "//a[contains(text(),'#{event}')]/../..//td[4]/time[contains(text(),'#{current_minute}') or contains(text(),'#{previous_minute}')]/../../td[3]/a[1]", wait: 1)
    rescue Capybara::ElementNotFound
      # ignored - pending actions cannot be found
    end
    refresh_page
  end
end

When(/^I wait up to (\d+) minutes to see "([^"]*)" in the last lines of "([^"]*)" on "([^"]*)"$/) do |waiting_time, text, file, host|
  node = get_target(host)
  timeout_seconds = waiting_time.to_i * 60
  # The grep command returns 0 (success) only if the text is found.
  # node.run_until_ok waits until the command returns an exit code of 0.
  node.run_until_ok("tail -n 10 #{file} | grep -E '#{text}'", timeout: timeout_seconds)
end

When(/^I follow the event "([^"]*)" completed during last minute$/) do |event|
  now = Time.now
  current_minute = now.strftime('%H:%M')
  previous_minute = (now - 60).strftime('%H:%M')
  xpath_query = "//a[contains(text(), '#{event}')]/../..//td[4]/time[contains(text(),'#{current_minute}') or contains(text(),'#{previous_minute}')]/../../td[3]/a[1]"
  element = find_and_wait_click(:xpath, xpath_query)
  element.click
end

# spacewalk errors steps
Then(/^the up2date logs on "([^"]*)" should contain no Traceback error$/) do |host|
  node = get_target(host)
  cmd = 'if grep "Traceback" /var/log/up2date ; then exit 1; else exit 0; fi'
  _out, code = node.run(cmd)
  raise ScriptError, 'error found, check the client up2date logs' if code.nonzero?
end

# action chains
When(/^I check radio button "(.*?)"$/) do |radio_button|
  if has_checked_field?(radio_button)
    log("Warning: Radio button '#{radio_button}' is already checked")
  else
    raise ScriptError, "#{radio_button} can't be checked" unless choose(radio_button)
  end
end

When(/^I check default base channel radio button of this "([^"]*)"$/) do |host|
  default_base_channel = BASE_CHANNEL_BY_CLIENT[product][host]
  raise ScriptError, "#{default_base_channel} can't be checked" unless choose(default_base_channel)
end

When(/^I enter as remote command this script in$/) do |multiline|
  find(:xpath, '//textarea[@name="script_body"]').set(multiline)
end

# bare metal
When(/^I check the ram value of the "([^"]*)"$/) do |host|
  node = get_target(host)
  get_ram_value = 'grep MemTotal /proc/meminfo |awk \'{print $2}\''
  ram_value, _err, _code = node.ssh(get_ram_value)
  ram_value = ram_value.gsub(/\s+/, '')
  ram_mb = ram_value.to_i / 1024
  step %(I should see a "#{ram_mb}" text)
end

When(/^I check the MAC address value of the "([^"]*)"$/) do |host|
  node = get_target(host)
  get_mac_address = 'cat /sys/class/net/eth0/address'
  mac_address, _err, _code = node.ssh(get_mac_address)
  mac_address = mac_address.gsub(/\s+/, '')
  mac_address.downcase!
  step %(I should see a "#{mac_address}" text)
end

Then(/^I should see the CPU frequency of the "([^"]*)"$/) do |host|
  node = get_target(host)
  get_cpu_freq = 'cat /proc/cpuinfo  | grep -i \'CPU MHz\'' # | awk '{print $4}'"
  cpu_freq, _err, _code = node.ssh(get_cpu_freq)
  get_cpu = cpu_freq.gsub(/\s+/, '')
  cpu = get_cpu.split('.')
  cpu = cpu[0].gsub(/[^\d]/, '')
  step %(I should see a "#{cpu.to_i / 1000} GHz" text)
end

Then(/^I should see the power is "([^"]*)"$/) do |status|
  within(:xpath, '//*[@for=\'powerStatus\']/..') do
    repeat_until_timeout(message: "power is not #{status}") do
      break if check_text_and_catch_request_timeout_popup?(status)

      find(:xpath, '//button[@value="Get status"]').click
    end
    raise ScriptError, "Power status #{status} not found" unless check_text_and_catch_request_timeout_popup?(status)
  end
end

When(/^I select "(.*?)" as the origin channel$/) do |label|
  step %(I select "#{label}" from "original_id")
end

# systemspage
Given(/^I am on the Systems page$/) do
  steps '
    And I follow the left menu "Systems > System List > All"
    And I wait until I do not see "Loading..." text
  '
end

When(/^I attach the file "(.*)" to "(.*)"$/) do |path, field|
  canonical_path = Pathname.new(File.join(File.dirname(__FILE__), '/../upload_files/', path)).cleanpath
  attach_file(field, canonical_path)
end

When(/^I refresh the metadata for "([^"]*)"$/) do |host|
  node = get_target(host)
  os_family = node.os_family
  case os_family
  when /^opensuse/, /^sles/, /micro/
    node.run_until_ok('zypper --non-interactive refresh -s')
  when /^centos/, /^rocky/
    node.run('yum clean all && yum makecache', timeout: 600)
  when /^ubuntu/
    node.run('apt-get update')
  else
    raise ScriptError, "The host #{host} has not yet a implementation for that step"
  end
end

# metadata steps
Then(/^I should have '([^']*)' in the patch metadata for "([^"]*)"$/) do |text, host|
  node = get_target(host)
  arch, _code = node.run('uname -m')
  arch.chomp!
  # TODO: adapt for architectures
  cmd = "zgrep '#{text}' /var/cache/zypp/raw/susemanager:fake-rpm-suse-channel/repodata/*updateinfo.xml.gz"
  node.run(cmd, timeout: 500)
end

# package steps
Then(/^I should see package "([^"]*)"$/) do |package|
  step %(I should see a "#{package}" text)
end

Given(/^metadata generation finished for "([^"]*)"$/) do |channel|
  get_target('server').run_until_ok("ls /var/cache/rhn/repodata/#{channel}/*updateinfo.xml.gz")
end

When(/^I push package "([^"]*)" into "([^"]*)" channel through "([^"]*)"$/) do |package_filepath, channel, minion|
  command = "mgrpush -u admin -p admin --server=#{get_target('server').full_hostname} --nosig -c #{channel} #{package_filepath}"
  get_target(minion).run(command, timeout: 500)
  package_filename = File.basename(package_filepath)
  get_target('server').run_until_ok("find /var/spacewalk/packages -name \"#{package_filename}\" | grep -q \"#{package_filename}\"", timeout: 500)
end

Then(/^I should see package "([^"]*)" in channel "([^"]*)"$/) do |pkg, channel|
  steps %(
    When I follow the left menu "Software > Channel List > All"
    And I follow "#{channel}"
    And I follow "Packages"
    Then I should see package "#{pkg}"
  )
end

When(/^I schedule a task to update ReportDB$/) do
  steps '
    When I follow the left menu "Admin > Task Schedules"
    And I follow "update-reporting-default"
    And I follow "mgr-update-reporting-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows
  '
end

Then(/^the user creation should fail with error containing "([^"]*)"$/) do |expected_text|
  status = get_context('user_creation_status')
  error_message = get_context('user_creation_error')

  raise "Expected user creation to fail, but status was '#{status}'" unless status == 'error'
  raise "Expected error message to include '#{expected_text}', but got '#{error_message}'" unless error_message.include?(expected_text)
end

Then(/^the user creation should succeed$/) do
  status = get_context('user_creation_status')
  raise "Expected user creation to succeed, but status was '#{status}'" unless status == 'success'
end
