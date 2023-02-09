# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

### This file contains the definitions for all steps concerning Cobbler.

Given(/^cobblerd is running$/) do
  ct = CobblerTest.new
  raise 'cobblerd is not running' unless ct.running?
end

When(/^I trigger cobbler system record on the "([^"]*)"$/) do |host|
  space = 'spacecmd -u admin -p admin'
  system_name = get_system_name(host)
  $server.run("#{space} clear_caches")
  out, _code = $server.run("#{space} system_details #{system_name}")
  unless out.include? 'ssh-push-tunnel'
    steps %(
      Given I am authorized as "testing" with password "testing"
      And I am on the Systems overview page of this "#{host}"
      And I follow "Provisioning"
      And I click on "Create PXE installation configuration"
      And I click on "Continue"
      And I wait until file "/srv/tftpboot/pxelinux.cfg/01-*" contains "ks=" on server
    )
  end
end

When(/^I prepare Cobbler for the buildiso command$/) do
  tmp_dir = "/var/cache/cobbler/buildiso"
  $server.run("mkdir -p #{tmp_dir}")
  # we need bootloaders for the buildiso command
  out, code = $server.run("cobbler mkloaders", verbose: true)
  raise "error in cobbler mkloaders.\nLogs:\n#{out}" if code.nonzero?
end

When(/^I run Cobbler buildiso for distro "([^"]*)" and all profiles$/) do |distro|
  tmp_dir = "/var/cache/cobbler/buildiso"
  iso_dir = "/var/cache/cobbler"
  out, code = $server.run("cobbler buildiso --tempdir=#{tmp_dir} --iso #{iso_dir}/profile_all.iso --distro=#{distro}", verbose: true)
  raise "error in cobbler buildiso.\nLogs:\n#{out}" if code.nonzero?
  profiles = %w[orchid flame pearl]
  isolinux_profiles = []
  cobbler_profiles = []
  profiles.each do |profile|
    # get all profiles from Cobbler
    result_cobbler, code = $server.run("cobbler profile list | grep -o #{profile}", verbose: true)
    cobbler_profiles.push(result_cobbler) if code.zero?
    # get all profiles from isolinux.cfg
    result_isolinux, code = $server.run("cat #{tmp_dir}/isolinux/isolinux.cfg | grep -o #{profile} | cut -c -6 | head -n 1")
    unless result_isolinux.empty?
      isolinux_profiles.push(result_isolinux)
    end
  end
  raise "error during comparison of Cobbler profiles.\nLogs:\nCobbler profiles:\n#{cobbler_profiles}\nisolinux profiles:\n#{isolinux_profiles}" unless cobbler_profiles == isolinux_profiles
end

When(/^I run Cobbler buildiso for distro "([^"]*)" and profile "([^"]*)"$/) do |distro, profile|
  tmp_dir = "/var/cache/cobbler/buildiso"
  iso_dir = "/var/cache/cobbler"
  out, code = $server.run("cobbler buildiso --tempdir=#{tmp_dir} --iso #{iso_dir}/#{profile}.iso --distro=#{distro} --profile=#{profile}", verbose: true)
  raise "error in cobbler buildiso.\nLogs:\n#{out}" if code.nonzero?
end

When(/^I run Cobbler buildiso for distro "([^"]*)" and profile "([^"]*)" without dns entries$/) do |distro, profile|
  tmp_dir = "/var/cache/cobbler/buildiso"
  iso_dir = "/var/cache/cobbler"
  out, code = $server.run("cobbler buildiso --tempdir=#{tmp_dir} --iso #{iso_dir}/#{profile}.iso --distro=#{distro} --profile=#{profile} --exclude-dns", verbose: true)
  raise "error in cobbler buildiso.\nLogs:\n#{out}" if code.nonzero?
  result, code = $server.run("cat #{tmp_dir}/isolinux/isolinux.cfg | grep -o nameserver", check_errors: false)
  # we have to fail here if the command suceeds
  raise "error in Cobbler buildiso, nameserver parameter found in isolinux.cfg but should not be found.\nLogs:\n#{result}" if code.zero?
end

When(/^I run Cobbler buildiso "([^"]*)" for distro "([^"]*)"$/) do |param, distro|
  # param can either be standalone or airgapped
  # workaround to get the contents of the buildiso folder
  step %(I run Cobbler buildiso for distro "#{distro}" and all profiles)
  tmp_dir = "/var/cache/cobbler/buildiso"
  iso_dir = "/var/cache/cobbler"
  source_dir = "/var/cache/cobbler/source_#{param}"
  $server.run("mv #{tmp_dir} #{source_dir}")
  $server.run("mkdir -p #{tmp_dir}")
  out, code = $server.run("cobbler buildiso --tempdir=#{tmp_dir} --iso #{iso_dir}/#{param}.iso --distro=#{distro} --#{param} --source=#{source_dir}", verbose: true)
  raise "error in cobbler buildiso.\nLogs:\n#{out}" if code.nonzero?
end

When(/^I check Cobbler buildiso ISO "([^"]*)" with xorriso$/) do |name|
  tmp_dir = "/var/cache/cobbler"
  out, code = $server.run("cat >#{tmp_dir}/test_image <<-EOF
BIOS
UEFI
EOF")
  xorriso = "xorriso -indev #{tmp_dir}/#{name}.iso -report_el_torito 2>/dev/null"
  iso_filter = "awk '/^El Torito boot img[[:space:]]+:[[:space:]]+[0-9]+[[:space:]]+[a-zA-Z]+[[:space:]]+y/{print $7}'"
  iso_file = "#{tmp_dir}/xorriso_#{name}"
  out, code = $server.run("#{xorriso} | #{iso_filter} >> #{iso_file}")
  raise "error while executing xorriso.\nLogs:\n#{out}" if code.nonzero?
  out, code = $server.run("diff #{tmp_dir}/test_image #{tmp_dir}/xorriso_#{name}")
  raise "error in verifying Cobbler buildiso image with xorriso.\nLogs:\n#{out}" if code.nonzero?
end

When(/^I cleanup xorriso temp files$/) do
  $server.run('rm /var/cache/cobbler/xorriso_*', check_errors: false)
end

Then(/^I add the Cobbler parameter "([^"]*)" with value "([^"]*)" to item "(distro|profile|system)" with name "([^"]*)"$/) do |param, value, item, name|
  result, code = $server.run("cobbler #{item} edit --name=#{name} --#{param}=#{value}", verbose: true)
  puts("cobbler #{item} edit --name #{name} #{param}=#{value}")
  raise "error in adding parameter and value to Cobbler #{item}.\nLogs:\n#{result}" if code.nonzero?
end

When(/^I check the Cobbler parameter "([^"]*)" with value "([^"]*)" in the isolinux.cfg$/) do |param, value|
  tmp_dir = "/var/cache/cobbler/buildiso"
  result, code = $server.run("cat #{tmp_dir}/isolinux/isolinux.cfg | grep -o #{param}=#{value}")
  raise "error while verifying isolinux.cfg parameter for Cobbler buildiso.\nLogs:\n#{result}" if code.nonzero?
end

When(/^I cleanup after Cobbler buildiso$/) do
  result, code = $server.run("rm -Rf /var/cache/cobbler")
  raise "error during Cobbler buildiso cleanup.\nLogs:\n#{result}" if code.nonzero?
end
