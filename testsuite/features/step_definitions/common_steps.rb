# Copyright (c) 2010-2023 SUSE LLC.
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

When(/^I mount as "([^"]+)" the ISO from "([^"]+)" in the server$/) do |name, url|
  # When using a mirror it is automatically mounted at /mirror
  if $mirror
    iso_path = url.sub(/^http:.*\/pub/, '/mirror/pub')
  else
    iso_path = "/tmp/#{name}.iso"
    $server.run("wget --no-check-certificate -O #{iso_path} #{url}", timeout: 700)
  end
  mount_point = "/srv/www/htdocs/#{name}"
  $server.run("mkdir -p #{mount_point}")
  $server.run("grep #{iso_path} /etc/fstab || echo '#{iso_path}  #{mount_point}  iso9660  loop,ro,_netdev  0 0' >> /etc/fstab")
  $server.run("umount #{iso_path}; mount #{iso_path}")
end

Then(/^the hostname for "([^"]*)" should be correct$/) do |host|
  node = get_target(host)
  step %(I should see a "#{node.hostname}" text)
end

Then(/^the kernel for "([^"]*)" should be correct$/) do |host|
  node = get_target(host)
  kernel_version, _code = node.run('uname -r')
  log 'I should see kernel version: ' + kernel_version
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
  raise unless code.zero?

  lines = interface.lines
  # selects only lines with IPv6 addresses and proceeds to form an array with only those addresses
  ipv6_addresses_list = lines.grep(/2[:0-9a-f]*|fe80:[:0-9a-f]*/)
  ipv6_addresses_list.map! { |ip_line| ip_line.slice(/2[:0-9a-f]*|fe80:[:0-9a-f]*/) }

  # confirms that the IPv6 address shown on the page is part of that list and, therefore, valid
  ipv6_address = find(:xpath, "//td[text()='IPv6 Address:']/following-sibling::td[1]").text
  log "IPv6 address: #{ipv6_address}"
  raise unless ipv6_addresses_list.include? ipv6_address
end

Then(/^the system ID for "([^"]*)" should be correct$/) do |host|
  client_id = $api_test.system.search_by_name(get_system_name(host)).first['id']
  step %(I should see a "#{client_id.to_s}" text)
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
  eleven_hours_in_seconds = 39600 # 11 hours * 60 minutes * 60 seconds
  rounded_uptime_days = ((uptime[:seconds] + eleven_hours_in_seconds) / 86400.0).round # 60 seconds * 60 minutes * 24 hours

  # the moment.js library being used has some weird rules, which these conditionals follow
  if (uptime[:days] >= 1 && rounded_uptime_days < 2) || (uptime[:days] < 1 && rounded_uptime_hours >= 22) # shows "a day ago" after 22 hours and before it's been 1.5 days
    step %(I should see a "a day ago" text)
  elsif rounded_uptime_hours > 1 && rounded_uptime_hours <= 21
    step %(I should see a "#{rounded_uptime_hours} hours ago" text)
  elsif rounded_uptime_minutes >= 45 && rounded_uptime_hours == 1 # shows "an hour ago" from 45 minutes onwards up to 1.5 hours
    step %(I should see a "an hour ago" text)
  elsif rounded_uptime_minutes > 1 && rounded_uptime_hours < 1
    step %(I should see a "#{rounded_uptime_minutes} minutes ago" text)
  elsif uptime[:seconds] >= 45 && rounded_uptime_minutes == 1
    step %(I should see a "a minute ago" text)
  elsif uptime[:seconds] < 45
    step %(I should see a "a few seconds ago" text)
  elsif rounded_uptime_days < 25
    step %(I should see a "#{rounded_uptime_days} days ago" text) # shows "a month ago" from 25 days onwards
  else
    step %(I should see a "a month ago" text)
  end
end

Then(/^I should see several text fields for "([^"]*)"$/) do |host|
  node = get_target(host)
  steps %(Then I should see a "UUID" text
    And I should see a "Virtualization" text
    And I should see a "Installed Products" text
    And I should see a "Checked In" text
    And I should see a "Registered" text
    And I should see a "Contact Method" text
    And I should see a "Auto Patch Update" text
    And I should see a "Maintenance Schedule" text
    And I should see a "Description" text
    And I should see a "Location" text
  )
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
    And I follow "Pending"
    And I wait at most #{pickup_timeout} seconds until I do not see "#{event}" text, refreshing the page
    And I follow "History"
    And I wait until I see "System History" text
    And I wait until I see "#{event}" text, refreshing the page
    And I follow first "#{event}"
    And I wait at most #{complete_timeout} seconds until the event is completed, refreshing the page
  )
end

When(/^I wait at most (\d+) seconds until event "([^"]*)" is completed$/) do |final_timeout, event|
  step %(I wait 90 seconds until the event is picked up and #{final_timeout} seconds until the event "#{event}" is completed)
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
    begin
      accept_prompt do
        execute_script 'window.location.reload()'
      end
    rescue Capybara::ModalNotFound
      # ignored
    end
  end
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
  raise 'error found, check the client up2date logs' if code.nonzero?
end

# salt failures log check
Then(/^the salt event log on server should contain no failures$/) do
  # upload salt event parser log
  file = 'salt_event_parser.py'
  source = "#{File.dirname(__FILE__)}/../upload_files/#{file}"
  dest = "/tmp/#{file}"
  return_code = file_inject($server, source, dest)
  raise 'File injection failed' unless return_code.zero?
  # print failures from salt event log
  output, _code = $server.run("python3 /tmp/#{file}")
  count_failures = output.to_s.scan(/false/).length
  output = output.join.to_s if output.respond_to?(:join)
  # Ignore the error if there is only the expected failure from min_salt_lock_packages.feature
  if count_failures == 1 && !$build_validation
    ignore_error = output.include?('remove lock')
  end
  raise "\nFound #{count_failures} failures in salt event log:\n#{output}\n" if count_failures.nonzero? && !ignore_error
end

# action chains
When(/^I check radio button "(.*?)"$/) do |arg1|
  raise "#{arg1} can't be checked" unless choose(arg1)
end

When(/^I enter as remote command this script in$/) do |multiline|
  find(:xpath, '//textarea[@name="script_body"]').set(multiline)
end

# bare metal
When(/^I check the ram value of the "([^"]*)"$/) do |host|
  node = get_target(host)
  get_ram_value = "grep MemTotal /proc/meminfo |awk '{print $2}'"
  ram_value, _local, _remote, _code = node.test_and_store_results_together(get_ram_value, 'root', 600)
  ram_value = ram_value.gsub(/\s+/, '')
  ram_mb = ram_value.to_i / 1024
  step %(I should see a "#{ram_mb}" text)
end

When(/^I check the MAC address value of the "([^"]*)"$/) do |host|
  node = get_target(host)
  get_mac_address = 'cat /sys/class/net/eth0/address'
  mac_address, _local, _remote, _code = node.test_and_store_results_together(get_mac_address, 'root', 600)
  mac_address = mac_address.gsub(/\s+/, '')
  mac_address.downcase!
  step %(I should see a "#{mac_address}" text)
end

Then(/^I should see the CPU frequency of the "([^"]*)"$/) do |host|
  node = get_target(host)
  get_cpu_freq = "cat /proc/cpuinfo  | grep -i 'CPU MHz'" # | awk '{print $4}'"
  cpu_freq, _local, _remote, _code = node.test_and_store_results_together(get_cpu_freq, 'root', 600)
  get_cpu = cpu_freq.gsub(/\s+/, '')
  cpu = get_cpu.split('.')
  cpu = cpu[0].gsub(/[^\d]/, '')
  step %(I should see a "#{cpu.to_i / 1000} GHz" text)
end

Then(/^I should see the power is "([^"]*)"$/) do |status|
  within(:xpath, "//*[@for='powerStatus']/..") do
    repeat_until_timeout(message: "power is not #{status}") do
      break if has_content?(status)
      find(:xpath, '//button[@value="Get status"]').click
    end
    raise "Power status #{status} not found" unless has_content?(status)
  end
end

When(/^I select "(.*?)" as the origin channel$/) do |label|
  step %(I select "#{label}" from "original_id")
end

# systemspage
Given(/^I am on the Systems page$/) do
  steps %(
    And I follow the left menu "Systems > System List > All"
    And I wait until I do not see "Loading..." text
  )
end

When(/^I attach the file "(.*)" to "(.*)"$/) do |path, field|
  canonical_path = Pathname.new(File.join(File.dirname(__FILE__), '/../upload_files/', path)).cleanpath
  attach_file(field, canonical_path)
end

When(/^I refresh the metadata for "([^"]*)"$/) do |host|
  node = get_target(host)
  os_family = node.os_family
  if os_family =~ /^opensuse/ || os_family =~ /^sles/
    node.run_until_ok('zypper --non-interactive refresh -s')
  elsif os_family =~ /^centos/ || os_family =~ /^rocky/
    node.run('yum clean all && yum makecache', timeout: 600)
  elsif os_family =~ /^ubuntu/
    node.run('apt-get update')
  else
    raise "The host #{host} has not yet a implementation for that step"
  end
end

# rubocop:disable Metrics/BlockLength
# WORKAROUND for https://github.com/SUSE/spacewalk/issues/20318
When(/^I install the needed packages for highstate in build host$/) do
  packages = "bea-stax
  bea-stax-api
  btrfsmaintenance
  btrfsprogs
  btrfsprogs-udev-rules
  catatonit
  checkmedia
  containerd
  cryptsetup
  cryptsetup-lang
  dbus-1-x11
  device-mapper
  docker
  dpkg
  fontconfig
  git-core
  git-gui
  gitk
  grub2-snapper-plugin
  iptables
  java-17-openjdk-headless
  javapackages-filesystem
  javapackages-tools
  jing
  kernel-default
  kernel-firmware-all
  kernel-firmware-amdgpu
  kernel-firmware-ath10k
  kernel-firmware-ath11k
  kernel-firmware-atheros
  kernel-firmware-bluetooth
  kernel-firmware-bnx2
  kernel-firmware-brcm
  kernel-firmware-chelsio
  kernel-firmware-dpaa2
  kernel-firmware-i915
  kernel-firmware-intel
  kernel-firmware-iwlwifi
  kernel-firmware-liquidio
  kernel-firmware-marvell
  kernel-firmware-media
  kernel-firmware-mediatek
  kernel-firmware-mellanox
  kernel-firmware-mwifiex
  kernel-firmware-network
  kernel-firmware-nfp
  kernel-firmware-nvidia
  kernel-firmware-platform
  kernel-firmware-prestera
  kernel-firmware-qcom
  kernel-firmware-qlogic
  kernel-firmware-radeon
  kernel-firmware-realtek
  kernel-firmware-serial
  kernel-firmware-sound
  kernel-firmware-ti
  kernel-firmware-ueagle
  kernel-firmware-usb-network
  kiwi-boot-descriptions
  kiwi-man-pages
  kiwi-systemdeps
  kiwi-systemdeps-bootloaders
  kiwi-systemdeps-containers
  kiwi-systemdeps-core
  kiwi-systemdeps-disk-images
  kiwi-systemdeps-filesystems
  kiwi-systemdeps-image-validation
  kiwi-systemdeps-iso-media
  kiwi-tools
  kpartx
  libaio1
  libasound2
  libbtrfs0
  libburn4
  libcontainers-common
  libdevmapper-event1_03
  libefa1
  libfmt8
  libfontconfig1
  libfreebl3
  libfreebl3-hmac
  libibverbs
  libibverbs1
  libip6tc2
  libisoburn1
  libisofs6
  libjpeg8
  libjte1
  liblcms2-2
  liblmdb-0_9_17
  liblttng-ust0
  liblvm2cmd2_03
  liblzo2-2
  libmd0
  libmediacheck6
  libmlx4-1
  libmlx5-1
  libmpath0
  libnetfilter_conntrack3
  libnfnetlink0
  libnftnl11
  libnuma1
  libpcsclite1
  libpwquality1
  libpwquality-lang
  librados2
  librbd1
  librdmacm1
  libreiserfscore0
  libsgutils2-1_47-2
  libsha1detectcoll1
  libsnapper5
  libsoftokn3
  libsoftokn3-hmac
  liburcu6
  libX11-6
  libX11-data
  libXau6
  libxcb1
  libXext6
  libXft2
  libxkbcommon0
  libxml2-tools
  libXmuu1
  libXrender1
  libxslt1
  libXss1
  lvm2
  make
  make-lang
  mdadm
  mozilla-nspr
  mozilla-nss
  mozilla-nss-certs
  mtools
  multipath-tools
  openssl
  patch
  pcsc-lite
  perl-TimeDate
  postfix
  python3-cssselect
  python3-docopt
  python3-kiwi
  python3-lxml
  python3-simplejson
  python3-solv
  python3-xattr
  qemu-block-curl
  qemu-block-rbd
  qemu-tools
  rdma-core
  rdma-ndd
  relaxngDatatype
  rollback-helper
  runc
  saxon9
  saxon9-scripts
  screen
  sg3_utils
  skopeo
  snapper
  snapper-zypp-plugin
  sqlite3-tcl
  squashfs
  syslinux
  tcl
  thin-provisioning-tools
  timezone-java
  tk
  umoci
  xalan-j2
  xerces-j2
  xhost
  xkeyboard-config
  xkeyboard-config-lang
  xml-commons-apis
  xml-commons-resolver
  xorriso
  xtables-plugins"
  $build_host.run("zypper --non-interactive in #{packages}", timeout: 600)
end
# rubocop:enable Metrics/BlockLength

Then(/^channel "([^"]*)" should be enabled on "([^"]*)"$/) do |channel, host|
  node = get_target(host)
  node.run("zypper lr -E | grep '#{channel}'")
end

Then(/^channel "([^"]*)" should not be enabled on "([^"]*)"$/) do |channel, host|
  node = get_target(host)
  _out, code = node.run("zypper lr -E | grep '#{channel}'", check_errors: false)
  raise "'#{channel}' was not expected but was found." if code.to_i.zero?
end

Then(/^"(\d+)" channels should be enabled on "([^"]*)"$/) do |count, host|
  node = get_target(host)
  node.run("zypper lr -E | tail -n +5", verbose: true)
  out, _code = node.run("zypper lr -E | tail -n +5 | wc -l")
  raise "Expected #{count} channels enabled but found #{out}." unless count.to_i == out.to_i
end

Then(/^"(\d+)" channels with prefix "([^"]*)" should be enabled on "([^"]*)"$/) do |count, prefix, host|
  node = get_target(host)
  node.run("zypper lr -E | tail -n +5 | grep '#{prefix}'", verbose: true)
  out, _code = node.run("zypper lr -E | tail -n +5 | grep '#{prefix}' | wc -l")
  raise "Expected #{count} channels enabled but found #{out}." unless count.to_i == out.to_i
end

# metadata steps
Then(/^I should have '([^']*)' in the patch metadata for "([^"]*)"$/) do |text, host|
  node = get_target(host)
  arch, _code = node.run('uname -m')
  arch.chomp!
  # TODO: adapt for architectures
  cmd = "zgrep '#{text}' /var/cache/zypp/raw/susemanager:fake-rpm-sles-channel/repodata/*updateinfo.xml.gz"
  node.run(cmd, timeout: 500)
end

# package steps
Then(/^I should see package "([^"]*)"$/) do |package|
  step %(I should see a "#{package}" text)
end

Given(/^metadata generation finished for "([^"]*)"$/) do |channel|
  $server.run_until_ok("ls /var/cache/rhn/repodata/#{channel}/*updateinfo.xml.gz")
end

When(/^I push package "([^"]*)" into "([^"]*)" channel$/) do |arg1, arg2|
  srvurl = "http://#{ENV['SERVER']}/APP"
  command = "rhnpush --server=#{srvurl} -u admin -p admin --nosig -c #{arg2} #{arg1} "
  $server.run(command, timeout: 500)
  $server.run('ls -lR /var/spacewalk/packages', timeout: 500)
end

Then(/^I should see package "([^"]*)" in channel "([^"]*)"$/) do |pkg, channel|
  steps %(
    When I follow the left menu "Software > Channel List > All"
    And I follow "#{channel}"
    And I follow "Packages"
    Then I should see package "#{pkg}"
  )
end

# content lifecycle steps
When(/^I click the environment build button$/) do
  raise 'Click on environment build failed' unless find_button('cm-build-modal-save-button', disabled: false, wait: DEFAULT_TIMEOUT).click
end

When(/^I click promote from Development to QA$/) do
  raise 'Click on promote from Development failed' unless find_button('dev_label-promote-modal-link', disabled: false, wait: DEFAULT_TIMEOUT).click
end

When(/^I click promote from QA to Production$/) do
  raise 'Click on promote from QA failed' unless find_button('qa_label-promote-modal-link', disabled: false, wait: DEFAULT_TIMEOUT).click
end

Then(/^I should see a "([^"]*)" text in the environment "([^"]*)"$/) do |text, env|
  within(:xpath, "//h3[text()='#{env}']/../..") do
    raise "Text \"#{text}\" not found" unless has_content?(text)
  end
end

When(/^I wait at most (\d+) seconds until I see "([^"]*)" text in the environment "([^"]*)"$/) do |seconds, text, env|
  within(:xpath, "//h3[text()='#{env}']/../..") do
    step %(I wait at most #{seconds} seconds until I see "#{text}" text)
  end
end

When(/^I wait until I see "([^"]*)" text in the environment "([^"]*)"$/) do |text, env|
  within(:xpath, "//h3[text()='#{env}']/../..") do
    raise "Text \"#{text}\" not found" unless has_text?(text, wait: DEFAULT_TIMEOUT)
  end
end

When(/^I add the "([^"]*)" channel to sources$/) do |channel|
  within(:xpath, "//span[text()='#{channel}']/../..") do
    raise "Add channel failed" unless find(:xpath, './/input[@type="checkbox"]').set(true)
  end
end

When(/^I click the "([^\"]*)" item (.*?) button$/) do |name, action|
  button = case action
           when /details/ then "i[contains(@class, 'fa-list')]"
           when /edit/ then "i[contains(@class, 'fa-edit')]"
           when /delete/ then "i[contains(@class, 'fa-trash')]"
           else raise "Unknown element with description '#{action}'"
           end
  xpath = "//td[contains(text(), '#{name}')]/ancestor::tr/td/div/button/#{button}"
  raise "xpath: #{xpath} not found" unless find(:xpath, xpath).click
end

When(/^I backup the SSH authorized_keys file of host "([^"]*)"$/) do |host|
  # authorized_keys paths on the client
  auth_keys_path = '/root/.ssh/authorized_keys'
  auth_keys_sav_path = '/root/.ssh/authorized_keys.sav'
  target = get_target(host)
  _, ret_code = target.run("cp #{auth_keys_path} #{auth_keys_sav_path}")
  raise 'error backing up authorized_keys on host' if ret_code.nonzero?
end

When(/^I add pre\-generated SSH public key to authorized_keys of host "([^"]*)"$/) do |host|
  key_filename = 'id_rsa_bootstrap-passphrase_linux.pub'
  target = get_target(host)
  ret_code = file_inject(
    target,
    File.dirname(__FILE__) + '/../upload_files/ssh_keypair/' + key_filename,
    '/tmp/' + key_filename
  )
  target.run("cat /tmp/#{key_filename} >> /root/.ssh/authorized_keys", timeout: 500)
  raise 'Error copying ssh pubkey to host' if ret_code.nonzero?
end

When(/^I restore the SSH authorized_keys file of host "([^"]*)"$/) do |host|
  # authorized_keys paths on the client
  auth_keys_path = '/root/.ssh/authorized_keys'
  auth_keys_sav_path = '/root/.ssh/authorized_keys.sav'
  target = get_target(host)
  target.run("cp #{auth_keys_sav_path} #{auth_keys_path}")
  target.run("rm #{auth_keys_sav_path}")
end

When(/^I add "([^\"]*)" calendar file as url$/) do |file|
  source = File.dirname(__FILE__) + '/../upload_files/' + file
  dest = "/srv/www/htdocs/pub/" + file
  return_code = file_inject($server, source, dest)
  raise 'File injection failed' unless return_code.zero?
  $server.run("chmod 644 #{dest}")
  url = "https://#{$server.full_hostname}/pub/" + file
  log "URL: #{url}"
  step %(I enter "#{url}" as "calendar-data-text")
end

When(/^I deploy testing playbooks and inventory files to "([^"]*)"$/) do |host|
  target = get_target(host)
  dest = "/srv/playbooks/orion_dummy/"
  target.run("mkdir -p #{dest}")
  source = File.dirname(__FILE__) + '/../upload_files/ansible/playbooks/orion_dummy/playbook_orion_dummy.yml'
  return_code = file_inject(target, source, dest + "playbook_orion_dummy.yml")
  raise 'File injection failed' unless return_code.zero?
  source = File.dirname(__FILE__) + '/../upload_files/ansible/playbooks/orion_dummy/hosts'
  return_code = file_inject(target, source, dest + "hosts")
  raise 'File injection failed' unless return_code.zero?
  source = File.dirname(__FILE__) + '/../upload_files/ansible/playbooks/orion_dummy/file.txt'
  return_code = file_inject(target, source, dest + "file.txt")
  raise 'File injection failed' unless return_code.zero?
  dest = "/srv/playbooks/"
  source = File.dirname(__FILE__) + '/../upload_files/ansible/playbooks/playbook_ping.yml'
  return_code = file_inject(target, source, dest + "playbook_ping.yml")
  raise 'File injection failed' unless return_code.zero?
end

When(/^I enter the reactivation key of "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  node_id = $api_test.system.retrieve_server_id(system_name)
  react_key = $api_test.system.obtain_reactivation_key(node_id)
  log "Reactivation Key: #{react_key}"
  step %(I enter "#{react_key}" as "reactivationKey")
end

When(/^I schedule a task to update ReportDB$/) do
  steps %(
    When I follow the left menu "Admin > Task Schedules"
    And I follow "update-reporting-default"
    And I follow "mgr-update-reporting-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows
  )
end

Then(/^port "([^"]*)" should be (open|closed)$/) do |port, selection|
  _output, code = $server.run("ss --listening --numeric | grep :#{port}", check_errors: false, verbose: true)
  port_opened = code.zero?
  if selection == 'closed'
    raise "Port '#{port}' open although it should not be!" if port_opened
  else
    raise "Port '#{port}' not open although it should be!" unless port_opened
  end
end

When(/^I reboot the server through SSH$/) do
  init_string = "ssh:#{$server.public_ip}"
  temp_server = twopence_init(init_string)
  temp_server.extend(LavandaBasic)
  temp_server.run('reboot > /dev/null 2> /dev/null &')
  default_timeout = 300

  check_shutdown($server.public_ip, default_timeout)
  check_restart($server.public_ip, temp_server, default_timeout)

  repeat_until_timeout(timeout: default_timeout, message: "Spacewalk didn't come up") do
    out, code = temp_server.run('spacewalk-service status', check_errors: false, timeout: 10)
    if !out.to_s.include? "dead" and out.to_s.include? "running"
      log "Server spacewalk service is up"
      break
    end
    sleep 1
  end
end

When(/^I reboot the "([^"]*)" minion through SSH$/) do |host|
  node = get_target(host)
  node.run('reboot > /dev/null 2> /dev/null &')
  reboot_timeout = 120
  check_shutdown($node.public_ip, reboot_timeout)
  check_restart($server.public_ip, node, reboot_timeout)
end

When(/^I reboot the "([^"]*)" minion through the web UI$/) do |host|
  step %(Given I am on the Systems overview page of this "#{host}")
  step %(When I follow first "Schedule System Reboot")
  step %(Then I should see a "System Reboot Confirmation" text")
  step %(And I should see a "Reboot system" button")
  step %(When I click on "Reboot system")
  step %(Then I should see a "Reboot scheduled for system" text")
  step %(And I wait at most 600 seconds until event "System reboot scheduled by admin" is completed")
  step %(Then I should see a "This action's status is: Completed" text")
end

When(/^I change the server's short hostname from hosts and hostname files$/) do
  old_hostname = $server.hostname
  new_hostname = old_hostname + '2'
  log "New short hostname: #{new_hostname}"

  $server.run("sed -i 's/#{old_hostname}/#{new_hostname}/g' /etc/hostname &&
  echo '#{$server.public_ip} #{$server.full_hostname} #{old_hostname}' >> /etc/hosts &&
  echo '#{$server.public_ip} #{new_hostname}#{$server.full_hostname.delete_prefix($server.hostname)} #{new_hostname}' >> /etc/hosts")
end

When(/^I run spacewalk-hostname-rename command on the server$/) do
  temp_server = twopence_init("ssh:#{$server.public_ip}")
  temp_server.extend(LavandaBasic)
  command = "spacewalk-hostname-rename #{$server.public_ip}
            --ssl-country=DE --ssl-state=Bayern --ssl-city=Nuremberg
            --ssl-org=SUSE --ssl-orgunit=SUSE --ssl-email=galaxy-noise@suse.de
            --ssl-ca-password=spacewalk -u admin -p admin"
  out_spacewalk, result_code = temp_server.run(command, check_errors: false, timeout: 10)
  log "#{out_spacewalk}"

  default_timeout = 300
  repeat_until_timeout(timeout: default_timeout, message: "Spacewalk didn't come up") do
    out, code = temp_server.run('spacewalk-service status', check_errors: false, timeout: 10)
    if !out.to_s.include? "dead" and out.to_s.include? "running"
      log "Server: spacewalk service is up"
      break
    end
    sleep 1
  end
  raise "Error while running spacewalk-hostname-rename command - see logs above" unless result_code.zero?
  raise "Error in the output logs - see logs above" if out_spacewalk.include? "No such file or directory"
end

When(/^I change back the server's hostname$/) do
  init_string = "ssh:#{$server.public_ip}"
  temp_server = twopence_init(init_string)
  temp_server.extend(LavandaBasic)
  temp_server.run("echo '#{$server.full_hostname}' > /etc/hostname ")
end

When(/^I clean up the server's hosts file$/) do
  command = "sed -i '$d' /etc/hosts && sed -i '$d' /etc/hosts"
  $server.run(command)
end
