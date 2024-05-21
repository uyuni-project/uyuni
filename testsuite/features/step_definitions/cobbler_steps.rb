# Copyright (c) 2023-2024 SUSE LLC
# Licensed under the terms of the MIT license.

### This file contains the definitions for all steps concerning Cobbler.

$cobbler_test = CobblerTest.new

# cobbler daemon
Given(/^cobblerd is running$/) do
  raise StandardError, 'cobblerd is not running' unless $cobbler_test.running?
end

When(/^I restart cobbler on the server$/) do
  get_target('server').run('systemctl restart cobblerd.service')
end

Given(/^I am logged in via the Cobbler API as user "([^"]*)" with password "([^"]*)"$/) do |user, pwd|
  $cobbler_test.login(user, pwd)
end

When(/^I log out from Cobbler via the API$/) do
  $cobbler_test.logout
end

# distro, profile and system management
Given(/^distro "([^"]*)" exists$/) do |distro|
  raise ScriptError, "Distro #{distro} does not exist" unless $cobbler_test.element_exists('distros', distro)
end

Given(/^profile "([^"]*)" exists$/) do |profile|
  raise ScriptError, "Profile #{profile} does not exist" unless $cobbler_test.element_exists('profiles', profile)
end

When(/^I create distro "([^"]*)"$/) do |distro|
  raise ScriptError, "Distro #{distro} already exists" if $cobbler_test.element_exists('distros', distro)

  $cobbler_test.distro_create(distro, '/var/autoinstall/SLES15-SP4-x86_64/DVD1/boot/x86_64/loader/linux', '/var/autoinstall/SLES15-SP4-x86_64/DVD1/boot/x86_64/loader/initrd')
end

When(/^I create profile "([^"]*)" for distro "([^"]*)"$/) do |profile, distro|
  raise ScriptError, "Profile #{profile} already exists" if $cobbler_test.element_exists('profiles', profile)

  $cobbler_test.profile_create(profile, distro, '/var/autoinstall/mock/empty.xml')
end

When(/^I create system "([^"]*)" for profile "([^"]*)"$/) do |system, profile|
  raise ScriptError, "System #{system} already exists" if $cobbler_test.element_exists('systems', system)

  $cobbler_test.system_create(system, profile)
end

When(/^I remove system "([^"]*)"$/) do |system|
  $cobbler_test.system_remove(system)
end

When(/^I remove profile "([^"]*)" as user "([^"]*)" with password "([^"]*)"$/) do |system, user, pwd|
  ct = ::CobblerTest.new
  ct.login(user, pwd)
  ct.profile_remove(system)
end

When(/^I remove distro "([^"]*)" as user "([^"]*)" with password "([^"]*)"$/) do |system, user, pwd|
  ct = ::CobblerTest.new
  ct.login(user, pwd)
  ct.distro_remove(system)
end

# cobbler reports
When(/^I trigger cobbler system record on the "([^"]*)"$/) do |host|
  space = 'spacecmd -u admin -p admin'
  get_target('server').run("#{space} clear_caches")
  out, _code = get_target('server').run("#{space} system_details #{get_target(host).full_hostname}")
  unless out.include? 'ssh-push-tunnel'
    steps %(
      Given I am authorized as "testing" with password "testing"
      And I am on the Systems overview page of this "#{host}"
      And I follow "Provisioning"
      And I click on "Create PXE installation configuration"
      And I wait until file "/srv/tftpboot/pxelinux.cfg/01-*" contains "inst.ks=" on server
    )
  end
end

Then(/^the cobbler report should contain "([^"]*)" for "([^"]*)"$/) do |text, host|
  node = get_target(host)
  output, _code = get_target('server').run("cobbler system report --name #{node.full_hostname}:1", check_errors: false)
  raise ScriptError, "Not found:\n#{output}" unless output.include?(text)
end

Then(/^the cobbler report should contain "([^"]*)" for cobbler system name "([^"]*)"$/) do |text, name|
  output, _code = get_target('server').run("cobbler system report --name #{name}", check_errors: false)
  raise ScriptError, "Not found:\n#{output}" unless output.include?(text)
end

# buildiso
When(/^I prepare Cobbler for the buildiso command$/) do
  tmp_dir = '/var/cache/cobbler/buildiso'
  get_target('server').run("mkdir -p #{tmp_dir}")
  # we need bootloaders for the buildiso command
  out, code = get_target('server').run('cobbler mkloaders', verbose: true)
  raise ScriptError, "error in cobbler mkloaders.\nLogs:\n#{out}" if code.nonzero?
end

When(/^I run Cobbler buildiso for distro "([^"]*)" and all profiles$/) do |distro|
  tmp_dir = '/var/cache/cobbler/buildiso'
  iso_dir = '/var/cache/cobbler'
  out, code = get_target('server').run("cobbler buildiso --tempdir=#{tmp_dir} --iso #{iso_dir}/profile_all.iso --distro=#{distro}", verbose: true)
  raise ScriptError, "error in cobbler buildiso.\nLogs:\n#{out}" if code.nonzero?

  profiles = %w[orchid flame pearl]
  isolinux_profiles = []
  cobbler_profiles = []
  profiles.each do |profile|
    # get all profiles from Cobbler
    result_cobbler, code = get_target('server').run("cobbler profile list | grep -o #{profile}", verbose: true)
    cobbler_profiles.push(result_cobbler) if code.zero?
    # get all profiles from isolinux.cfg
    result_isolinux, _code = get_target('server').run("cat #{tmp_dir}/isolinux/isolinux.cfg | grep -o #{profile} | cut -c -6 | head -n 1")
    unless result_isolinux.empty?
      isolinux_profiles.push(result_isolinux)
    end
  end
  raise ScriptError, "error during comparison of Cobbler profiles.\nLogs:\nCobbler profiles:\n#{cobbler_profiles}\nisolinux profiles:\n#{isolinux_profiles}" unless cobbler_profiles == isolinux_profiles
end

When(/^I run Cobbler buildiso for distro "([^"]*)" and profile "([^"]*)"$/) do |distro, profile|
  tmp_dir = '/var/cache/cobbler/buildiso'
  iso_dir = '/var/cache/cobbler'
  out, code = get_target('server').run("cobbler buildiso --tempdir=#{tmp_dir} --iso #{iso_dir}/#{profile}.iso --distro=#{distro} --profile=#{profile}", verbose: true)
  raise ScriptError, "error in cobbler buildiso.\nLogs:\n#{out}" if code.nonzero?
end

When(/^I run Cobbler buildiso for distro "([^"]*)" and profile "([^"]*)" without dns entries$/) do |distro, profile|
  tmp_dir = '/var/cache/cobbler/buildiso'
  iso_dir = '/var/cache/cobbler'
  out, code = get_target('server').run("cobbler buildiso --tempdir=#{tmp_dir} --iso #{iso_dir}/#{profile}.iso --distro=#{distro} --profile=#{profile} --exclude-dns", verbose: true)
  raise ScriptError, "error in cobbler buildiso.\nLogs:\n#{out}" if code.nonzero?

  result, code = get_target('server').run("cat #{tmp_dir}/isolinux/isolinux.cfg | grep -o nameserver", check_errors: false)
  # we have to fail here if the command suceeds
  raise ScriptError, "error in Cobbler buildiso, nameserver parameter found in isolinux.cfg but should not be found.\nLogs:\n#{result}" if code.zero?
end

When(/^I run Cobbler buildiso "([^"]*)" for distro "([^"]*)"$/) do |param, distro|
  # param can either be standalone or airgapped
  # workaround to get the contents of the buildiso folder
  step %(I run Cobbler buildiso for distro "#{distro}" and all profiles)
  tmp_dir = '/var/cache/cobbler/buildiso'
  iso_dir = '/var/cache/cobbler'
  source_dir = "/var/cache/cobbler/source_#{param}"
  get_target('server').run("mv #{tmp_dir} #{source_dir}")
  get_target('server').run("mkdir -p #{tmp_dir}")
  out, code = get_target('server').run("cobbler buildiso --tempdir=#{tmp_dir} --iso #{iso_dir}/#{param}.iso --distro=#{distro} --#{param} --source=#{source_dir}", verbose: true)
  raise ScriptError, "error in cobbler buildiso.\nLogs:\n#{out}" if code.nonzero?
end

When(/^I check Cobbler buildiso ISO "([^"]*)" with xorriso$/) do |name|
  tmp_dir = '/var/cache/cobbler'
  _out, _code = get_target('server').run("cat >#{tmp_dir}/test_image <<-EOF
BIOS
UEFI
EOF")
  xorriso = "xorriso -indev #{tmp_dir}/#{name}.iso -report_el_torito 2>/dev/null"
  iso_filter = 'awk \'/^El Torito boot img[[:space:]]+:[[:space:]]+[0-9]+[[:space:]]+[a-zA-Z]+[[:space:]]+y/{print $7}\''
  iso_file = "#{tmp_dir}/xorriso_#{name}"
  out, code = get_target('server').run("#{xorriso} | #{iso_filter} >> #{iso_file}")
  raise ScriptError, "error while executing xorriso.\nLogs:\n#{out}" if code.nonzero?

  out, code = get_target('server').run("diff #{tmp_dir}/test_image #{tmp_dir}/xorriso_#{name}")
  raise ScriptError, "error in verifying Cobbler buildiso image with xorriso.\nLogs:\n#{out}" if code.nonzero?
end

# xorriso
When(/^I cleanup xorriso temp files$/) do
  get_target('server').run('rm /var/cache/cobbler/xorriso_*', check_errors: false)
end

# cobbler settings
Given(/^cobbler settings are successfully migrated$/) do
  out, code = get_target('server').run('cobbler-settings migrate -t /etc/cobbler/settings.yaml')
  raise ScriptError, "error when running cobbler-settings to migrate current settings.\nLogs:\n#{out}" if code.nonzero?
end

# cobbler parameters
Then(/^I add the Cobbler parameter "([^"]*)" with value "([^"]*)" to item "(distro|profile|system)" with name "([^"]*)"$/) do |param, value, item, name|
  result, code = get_target('server').run("cobbler #{item} edit --name=#{name} --#{param}=#{value}", verbose: true)
  puts("cobbler #{item} edit --name #{name} #{param}=#{value}")
  raise ScriptError, "error in adding parameter and value to Cobbler #{item}.\nLogs:\n#{result}" if code.nonzero?
end

When(/^I check the Cobbler parameter "([^"]*)" with value "([^"]*)" in the isolinux.cfg$/) do |param, value|
  tmp_dir = '/var/cache/cobbler/buildiso'
  result, code = get_target('server').run("cat #{tmp_dir}/isolinux/isolinux.cfg | grep -o #{param}=#{value}")
  raise ScriptError, "error while verifying isolinux.cfg parameter for Cobbler buildiso.\nLogs:\n#{result}" if code.nonzero?
end

# backup step
When(/^I backup Cobbler settings file$/) do
  get_target('server').run('cp /etc/cobbler/settings.yaml /etc/cobbler/settings.yaml.bak 2> /dev/null', check_errors: false)
end

# cleanup steps
When(/^I cleanup after Cobbler buildiso$/) do
  result, code = get_target('server').run('rm -Rf /var/cache/cobbler')
  raise ScriptError, "Error during Cobbler buildiso cleanup.\nLogs:\n#{result}" if code.nonzero?
end

# cobbler commands
When(/^I copy autoinstall mocked files on server$/) do
  target_dirs = '/var/autoinstall/Fedora_12_i386/images/pxeboot /var/autoinstall/SLES15-SP4-x86_64/DVD1/boot/x86_64/loader /var/autoinstall/mock'
  get_target('server').run("mkdir -p #{target_dirs}")
  base_dir = "#{File.dirname(__FILE__)}/../upload_files/autoinstall/cobbler/"
  source_dir = '/var/autoinstall/'
  return_codes = []
  return_codes << file_inject(get_target('server'), "#{base_dir}fedora12/vmlinuz", "#{source_dir}Fedora_12_i386/images/pxeboot/vmlinuz")
  return_codes << file_inject(get_target('server'), "#{base_dir}fedora12/initrd.img", "#{source_dir}Fedora_12_i386/images/pxeboot/initrd.img")
  return_codes << file_inject(get_target('server'), "#{base_dir}mock/empty.xml", "#{source_dir}mock/empty.xml")
  return_codes << file_inject(get_target('server'), "#{base_dir}sles15sp4/initrd", "#{source_dir}SLES15-SP4-x86_64/DVD1/boot/x86_64/loader/initrd")
  return_codes << file_inject(get_target('server'), "#{base_dir}sles15sp4/linux", "#{source_dir}SLES15-SP4-x86_64/DVD1/boot/x86_64/loader/linux")
  raise ScriptError, 'File injection failed' unless return_codes.all?(&:zero?)
end

When(/^I run Cobbler sync (with|without) error checking$/) do |checking|
  if checking == 'with'
    out, _code = get_target('server').run('cobbler sync')
    raise SystemCallError, 'cobbler sync failed' if out.include? 'Push failed'
  else
    _out, _code = get_target('server').run('cobbler sync')
  end
end

When(/^I start local monitoring of Cobbler$/) do
  cobbler_conf_file = '/etc/cobbler/logging_config.conf'
  cobbler_log_file = '/var/log/cobbler/cobbler_debug.log'
  get_target('server').run("rm #{cobbler_log_file}", check_errors: false)
  _result, code = get_target('server').run("test -f #{cobbler_conf_file}.old", check_errors: false)
  if code.zero?
    get_target('server').run('systemctl restart cobblerd')
  else
    handler_name = 'FileLogger02'
    formatter_name = 'JSONlogfile'
    handler_class = "\"\n[handler_#{handler_name}]\n" \
                    "class=FileHandler\n" \
                    "level=DEBUG\n" \
                    "formatter=#{formatter_name}\n" \
                    "args=('#{cobbler_log_file}', 'a')\n\n" \
                    "[formatter_#{formatter_name}]\n" \
                    "format ={\\''threadName\\'': \\''%(threadName)s\\'', " \
                    "\\''asctime\\'': \\''%(asctime)s\\'', \\''levelname\\'':  \\''%(levelname)s\\'', " \
                    "\\''message\\'': \\''%(message)s\\''}\n\""
    command = "cp #{cobbler_conf_file} #{cobbler_conf_file}.old && " \
              "line_number=`awk \"/\\\[handlers\\\]/{ print NR; exit }\" #{cobbler_conf_file}` && " \
              "sed -e \"$(($line_number + 1))s/$/,#{handler_name}/\" -i #{cobbler_conf_file} && " \
              "line_number=`awk \"/\\\[formatters\\\]/{ print NR; exit }\" #{cobbler_conf_file}` && " \
              "sed -e \"$(($line_number + 1))s/$/,#{formatter_name}/\" -i #{cobbler_conf_file} && " \
              "line_number=`awk \"/\\\[logger_root\\\]/{ print NR; exit }\" #{cobbler_conf_file}` && " \
              "sed -e \"$(($line_number + 2))s/$/,#{handler_name}/\" -i #{cobbler_conf_file} && " \
              "echo -e #{handler_class} >> #{cobbler_conf_file}"
    get_target('server').run("#{command} && systemctl restart cobblerd")
  end
  # give cobbler a few seconds to come up
  sleep 3
end

Then(/^the local logs for Cobbler should not contain errors$/) do
  cobbler_log_file = '/var/log/cobbler/cobbler_debug.log'
  local_file = '/tmp/cobbler_debug.log'
  return_code = file_extract(get_target('server'), cobbler_log_file, local_file)
  raise ScriptError, 'File extraction failed' unless return_code.zero?

  file_data = File.read(local_file).gsub!("\n", ',').chop.gsub('"', ' \' ').gsub('\\\'\'', '"')
  file_data = "[#{file_data}]"
  data_hash = JSON.parse(file_data)
  output = data_hash.select { |key, _hash| key['levelname'] == 'ERROR' }
  get_target('server').run("cp #{cobbler_log_file} #{cobbler_log_file}$(date +\"%Y_%m_%d_%I_%M_%p\")") unless output.empty?
  raise ScriptError, "Errors in Cobbler logs:\n #{output}" unless output.empty?
end
