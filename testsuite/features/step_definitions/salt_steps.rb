# Copyright 2015-2024 SUSE LLC
# Licensed under the terms of the MIT license.

### This file contains all step definitions concerning Salt and bootstrapping
### Salt minions.

require 'timeout'
require 'open-uri'
require 'tempfile'

Given(/^the Salt master can reach "(.*?)"$/) do |minion|
  system_name = get_system_name(minion)
  server = get_target('server')
  start = Time.now
  # 700 seconds is the maximum time it takes the proxy to recover after being redefined for Retail
  # 300 seconds would be the default first keepalive interval for the minion before it realizes the connection is stuck
  repeat_until_timeout(timeout: 700, message: "Master can not communicate with #{minion}", report_result: true) do
    out, _code = server.run("salt #{system_name} test.ping", check_errors: false)
    if out.include?(system_name) && out.include?('True')
      finished = Time.now
      log "It took #{finished.to_i - start.to_i} seconds to contact the minion"
      break
    end
    sleep 1
    out
  end
end

When(/^I get the contents of the remote file "(.*?)"$/) do |filename|
  $output, _code = get_target('server').run("cat #{filename}")
end

When(/^I stop salt-minion on "(.*?)"$/) do |minion|
  node = get_target(minion)
  pkgname = use_salt_bundle ? 'venv-salt-minion' : 'salt-minion'
  os_version = node.os_version
  os_family = node.os_family
  if os_family =~ /^sles/ && os_version =~ /^11/
    node.run("rc#{pkgname} stop", check_errors: false)
  else
    node.run("systemctl stop #{pkgname}", check_errors: false)
  end
end

When(/^I start salt-minion on "(.*?)"$/) do |minion|
  node = get_target(minion)
  pkgname = use_salt_bundle ? 'venv-salt-minion' : 'salt-minion'
  os_version = node.os_version
  os_family = node.os_family
  if os_family =~ /^sles/ && os_version =~ /^11/
    node.run("rc#{pkgname} start", check_errors: false)
  else
    node.run("systemctl start #{pkgname}", check_errors: false)
  end
end

When(/^I restart salt-minion on "(.*?)"$/) do |minion|
  node = get_target(minion)
  pkgname = use_salt_bundle ? 'venv-salt-minion' : 'salt-minion'
  os_version = node.os_version
  os_family = node.os_family
  if os_family =~ /^sles/ && os_version =~ /^11/
    node.run("rc#{pkgname} restart", check_errors: false)
  else
    node.run("systemctl restart #{pkgname}", check_errors: false)
  end
end

When(/^I refresh salt-minion grains on "(.*?)"$/) do |minion|
  node = get_target(minion)
  salt_call = use_salt_bundle ? 'venv-salt-call' : 'salt-call'
  node.run("#{salt_call} saltutil.refresh_grains")
end

When(/^I wait at most (\d+) seconds until Salt master sees "([^"]*)" as "([^"]*)"$/) do |key_timeout, minion, key_type|
  cmd = "salt-key --list #{key_type}"
  repeat_until_timeout(timeout: key_timeout.to_i, message: "Minion '#{minion}' is not listed among #{key_type} keys on Salt master") do
    system_name = get_system_name(minion)
    unless system_name.empty?
      output, return_code = get_target('server').run(cmd, check_errors: false)
      break if return_code.zero? && output.include?(system_name)
    end
    sleep 1
  end
end

When(/^I wait until Salt client is inactive on "([^"]*)"$/) do |minion|
  salt_minion = use_salt_bundle ? 'venv-salt-minion' : 'salt-minion'
  step %(I wait until "#{salt_minion}" service is inactive on "#{minion}")
end

When(/^I wait until no Salt job is running on "([^"]*)"$/) do |minion|
  target = get_target(minion)
  salt_call = use_salt_bundle ? 'venv-salt-call' : 'salt-call'
  repeat_until_timeout(timeout: 600, message: "A Salt job is still running on #{minion}") do
    output, _code = target.run("#{salt_call} -lquiet saltutil.running", verbose: true)
    break if output == "local:\n"

    sleep 3
  end
end

When(/^I delete "([^"]*)" key in the Salt master$/) do |host|
  system_name = get_system_name(host)
  $output, _code = get_target('server').run("salt-key -y -d #{system_name}", check_errors: false)
end

When(/^I accept "([^"]*)" key in the Salt master$/) do |host|
  system_name = get_system_name(host)
  get_target('server').run("salt-key -y --accept=#{system_name}*")
end

When(/^I list all Salt keys shown on the Salt master$/) do
  get_target('server').run('salt-key --list-all', check_errors: false, verbose: true)
end

When(/^I get OS information of "([^"]*)" from the Master$/) do |host|
  system_name = get_system_name(host)
  $output, _code = get_target('server').run("salt #{system_name} grains.get osfullname")
end

Then(/^it should contain a "([^"]*?)" text$/) do |content|
  assert_match(/#{content}/, $output)
end

Then(/^it should contain the OS of "([^"]*)"$/) do |host|
  node = get_target(host)
  os_family = node.os_family
  family = os_family =~ /^opensuse/ ? 'Leap' : 'SLES'
  assert_match(/#{family}/, $output)
end

When(/^I apply state "([^"]*)" to "([^"]*)"$/) do |state, host|
  system_name = get_system_name(host)
  get_target('server').run("salt #{system_name} state.apply #{state}", verbose: true)
end

Then(/^salt-api should be listening on local port (\d+)$/) do |port|
  $output, _code = get_target('server').run("ss -ntl | grep #{port}")
  assert_match(/127.0.0.1:#{port}/, $output)
end

Then(/^salt-master should be listening on public port (\d+)$/) do |port|
  $output, _code = get_target('server').run("ss -ntl | grep #{port}")
  assert_match(/(0.0.0.0|\*|\[::\]):#{port}/, $output)
end

Then(/^the system should have a base channel set$/) do
  step 'I should not see a "This system has no Base Software Channel. You can select a Base Channel from the list below." text'
end

Then(/^"(.*?)" should not be registered$/) do |host|
  system_name = get_system_name(host)
  refute_includes($api_test.system.list_systems.map { |s| s['name'] }, system_name)
end

Then(/^"(.*?)" should be registered$/) do |host|
  system_name = get_system_name(host)
  assert_includes($api_test.system.list_systems.map { |s| s['name'] }, system_name)
end

Then(/^"(.*?)" should have been reformatted$/) do |host|
  system_name = get_system_name(host)
  output, _code = get_target('server').run("salt #{system_name} file.file_exists /intact")
  raise ScriptError, "Minion #{host} is intact" unless output.include? 'False'
end

# user salt steps
When(/^I click on preview$/) do
  find('button#preview').click
end

When(/^I click on stop waiting$/) do
  find('button#stop').click
end

When(/^I click on run$/) do
  find('button#run', wait: DEFAULT_TIMEOUT).click
end

When(/^I expand the results for "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  find("div[id='#{system_name}']").click
end

When(/^I enter command "([^"]*)"$/) do |cmd|
  fill_in('command', with: cmd, fill_options: { clear: :backspace })
end

When(/^I enter target "([^"]*)"$/) do |host|
  value = get_system_name(host)
  fill_in('target', with: value, fill_options: { clear: :backspace })
end

Then(/^I should see "([^"]*)" in the command output for "([^"]*)"$/) do |text, host|
  system_name = get_system_name(host)
  within("pre[id='#{system_name}-results']") do
    raise ScriptError, "Text '#{text}' not found in the results of #{system_name}" unless check_text_and_catch_request_timeout_popup?(text)
  end
end

# Salt formulas

When(/^I manually install the "([^"]*)" formula on the server$/) do |package|
  get_target('server').run('zypper --non-interactive refresh')
  get_target('server').run("zypper --non-interactive install --force #{package}-formula")
end

When(/^I manually uninstall the "([^"]*)" formula from the server$/) do |package|
  get_target('server').run("zypper --non-interactive remove #{package}-formula")
  # Remove automatically installed dependency if needed
  if package == 'uyuni-config'
    get_target('server').run("zypper --non-interactive remove #{package}-modules")
  end
end

When(/^I synchronize all Salt dynamic modules on "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  get_target('server').run("salt #{system_name} saltutil.sync_all")
end

When(/^I remove "([^"]*)" from salt cache on "([^"]*)"$/) do |filename, host|
  node = get_target(host)
  salt_cache = use_salt_bundle ? '/var/cache/venv-salt-minion/' : '/var/cache/salt/'
  file_delete(node, "#{salt_cache}#{filename}")
end

When(/^I remove "([^"]*)" from salt minion config directory on "([^"]*)"$/) do |filename, host|
  node = get_target(host)
  salt_config = use_salt_bundle ? '/etc/venv-salt-minion/minion.d/' : '/etc/salt/minion.d/'
  file_delete(node, "#{salt_config}#{filename}")
end

When(/^I configure salt minion on "([^"]*)"$/) do |host|
  content = %(
master: #{get_target('server').full_hostname}
server_id_use_crc: adler32
enable_legacy_startup_events: False
enable_fqdns_grains: False
start_event_grains:
  - machine_id
  - saltboot_initrd
  - susemanager)
  step %(I store "#{content}" into file "susemanager.conf" in salt minion config directory on "#{host}")
end

When(/^I store "([^"]*)" into file "([^"]*)" in salt minion config directory on "([^"]*)"$/) do |content, filename, host|
  salt_config = use_salt_bundle ? '/etc/venv-salt-minion/minion.d/' : '/etc/salt/minion.d/'
  step %(I store "#{content}" into file "#{salt_config}#{filename}" on "#{host}")
end

When(/^I ([^ ]*) the "([^"]*)" formula$/) do |action, formula|
  # Complicated code because the checkbox is not a <input type=checkbox> but an <i>
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if action == 'check'
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if action == 'uncheck'
  # DOM refreshes content of chooseFormulas element by accessing it. Then conditions are evaluated properly.
  find('#chooseFormulas')['innerHTML']
  if has_xpath?(xpath_query, wait: 2)
    raise ScriptError, "xpath: #{xpath_query} not found" unless find(:xpath, xpath_query, wait: 2).click
  else
    xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if action == 'check'
    xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if action == 'uncheck'
    raise ScriptError, "xpath: #{xpath_query} not found" unless has_xpath?(xpath_query, wait: 2)
  end
end

Then(/^the "([^"]*)" formula should be ([^ ]*)$/) do |formula, state|
  # Complicated code because the checkbox is not a <input type=checkbox> but an <i>
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if state == 'checked'
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if state == 'unchecked'
  # DOM refreshes content of chooseFormulas element by accessing it. Then conditions are evaluated properly.
  find('#chooseFormulas')['innerHTML']
  raise ScriptError, "Checkbox is not #{state}" if has_xpath?(xpath_query)

  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if state == 'checked'
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if state == 'unchecked'
  assert has_xpath?(xpath_query), 'Checkbox could not be found'
end

When(/^I select "([^"]*)" in (.*) field$/) do |value, box|
  select(value, from: FIELD_IDS[box])
end

Then(/^the timezone on "([^"]*)" should be "([^"]*)"$/) do |minion, timezone|
  node = get_target(minion)
  output, _code = node.run('date +%Z')
  result = output.strip
  result = 'CET' if result == 'CEST'
  raise ScriptError, "The timezone #{timezone} is different to #{result}" unless result == timezone
end

Then(/^the keymap on "([^"]*)" should be "([^"]*)"$/) do |minion, keymap|
  node = get_target(minion)
  output, _code = node.run('grep \'KEYMAP=\' /etc/vconsole.conf')
  raise ScriptError, "The keymap #{keymap} is different to the output: #{output.strip}" unless output.strip == "KEYMAP=#{keymap}"
end

Then(/^the language on "([^"]*)" should be "([^"]*)"$/) do |minion, language|
  node = get_target(minion)
  output, _code = node.run('grep \'RC_LANG=\' /etc/sysconfig/language')
  unless output.strip == "RC_LANG=\"#{language}\""
    output, _code = node.run('grep \'LANG=\' /etc/locale.conf')
    raise ScriptError, "The language #{language} is different to the output: #{output.strip}" unless output.strip == "LANG=#{language}"
  end
end

When(/^I refresh the pillar data$/) do
  get_target('server').run("salt '#{get_target('sle_minion').full_hostname}' saltutil.refresh_pillar wait=True")
end

When(/^I wait until there is no pillar refresh salt job active$/) do
  repeat_until_timeout(message: 'pillar refresh job still active') do
    output, = get_target('server').run('salt-run jobs.active')
    break unless output.include?('saltutil.refresh_pillar')

    sleep 1
  end
end

When(/^I wait until there is no Salt job calling the module "([^"]*)" on "([^"]*)"$/) do |salt_module, minion|
  target = get_target(minion)
  salt_call = use_salt_bundle ? 'venv-salt-call' : 'salt-call'
  target.run_until_fail("#{salt_call} -lquiet saltutil.running | grep #{salt_module}", timeout: 600)
end

Then(/^the pillar data for "([^"]*)" should be "([^"]*)" on "([^"]*)"$/) do |key, value, minion|
  output, _code = pillar_get(key, minion)
  if value == ''
    raise ScriptError, "Output has more than one line: #{output}" unless output.split("\n").length == 1
  else
    raise ScriptError, "Output value wasn't found: #{output}" unless output.split("\n").length > 1
    raise ScriptError, "Output value is different than #{value}: #{output}" unless output.split("\n")[1].strip == value
  end
end

Then(/^the pillar data for "([^"]*)" should contain "([^"]*)" on "([^"]*)"$/) do |key, value, minion|
  output, _code = pillar_get(key, minion)
  raise ScriptError, "Output doesn't contain #{value}: #{output}" unless output.include? value
end

Then(/^the pillar data for "([^"]*)" should not contain "([^"]*)" on "([^"]*)"$/) do |key, value, minion|
  output, _code = pillar_get(key, minion)
  raise ScriptError, "Output contains #{value}: #{output}" if output.include? value
end

Then(/^the pillar data for "([^"]*)" should be empty on "([^"]*)"$/) do |key, minion|
  output = ''
  repeat_until_timeout(timeout: DEFAULT_TIMEOUT, message: "Output has more than one line: #{output}", report_result: true) do
    output, _code = pillar_get(key, minion)
    break if output.split("\n").length == 1

    sleep 1
  end
end

Given(/^I try to download "([^"]*)" from channel "([^"]*)"$/) do |rpm, channel|
  url = "https://#{get_target('server').full_hostname}/rhn/manager/download/#{channel}/getPackage/#{rpm}"
  url = "#{url}?#{@token}" if @token
  @download_path = nil
  @download_error = nil
  Tempfile.open(rpm) do |tmpfile|
    @download_path = tmpfile.path
    begin
      open(url, ssl_verify_mode: OpenSSL::SSL::VERIFY_NONE) do |urlfile|
        tmpfile.write(urlfile.read)
      end
    rescue OpenURI::HTTPError => e
      @download_error = e
    end
  end
end

Then(/^the download should get a (\d+) response$/) do |code|
  refute_nil(@download_error)
  assert_equal(code.to_i, @download_error.io.status[0].to_i)
end

Then(/^the download should get no error$/) do
  assert_nil(@download_error)
end

# Perform actions
When(/^I reject "([^"]*)" from the Pending section$/) do |host|
  system_name = get_system_name(host)
  xpath_query = "//tr[td[contains(.,'#{system_name}')]]//button[@title = 'Reject']"
  raise ScriptError, "xpath: #{xpath_query} not found" unless find(:xpath, xpath_query).click
end

When(/^I delete "([^"]*)" from the Rejected section$/) do |host|
  system_name = get_system_name(host)
  xpath_query = "//tr[td[contains(.,'#{system_name}')]]//button[@title = 'Delete']"
  raise ScriptError, "xpath: #{xpath_query} not found" unless find(:xpath, xpath_query).click
end

When(/^I see "([^"]*)" fingerprint$/) do |host|
  node = get_target(host)
  salt_call = use_salt_bundle ? 'venv-salt-call' : 'salt-call'
  output, _code = node.run("#{salt_call} --local key.finger")
  fing = output.split("\n")[1].strip!
  raise ScriptError, "Text: #{fing} not found" unless check_text_and_catch_request_timeout_popup?(fing)
end

When(/^I accept "([^"]*)" key$/) do |host|
  system_name = get_system_name(host)
  xpath_query = "//tr[td[contains(.,'#{system_name}')]]//button[@title = 'Accept']"
  raise ScriptError, "xpath: #{xpath_query} not found" unless find(:xpath, xpath_query).click
end

When(/^I refresh page until I see "(.*?)" hostname as text$/) do |minion|
  within('#spacewalk-content') do
    step %(I wait until I see the name of "#{minion}", refreshing the page)
  end
end

When(/^I refresh page until I do not see "(.*?)" hostname as text$/) do |minion|
  within('#spacewalk-content') do
    step %(I wait until I do not see the name of "#{minion}", refreshing the page)
  end
end

When(/^I list packages with "(.*?)"$/) do |str|
  find('input#package-search').set(str)
  repeat_until_timeout(timeout: 60, retries: 30, message: 'Search button not enabled', report_result: true) do
    break unless find('button#search').disabled?

    sleep 1
  end
  find('button#search').click
end

When(/^I change the state of "([^"]*)" to "([^"]*)" and "([^"]*)"$/) do |pkg, state, instd_state|
  # Options for state are Installed, Unmanaged and Removed
  # Options for instd_state are Any or Latest
  # Default if you pick Installed is Latest
  find("##{pkg}-pkg-state").select(state)
  if !instd_state.to_s.empty? && state == 'Installed'
    find("##{pkg}-version-constraint").select(instd_state)
  end
end

When(/^I click apply$/) do
  find('button#apply').click
end

When(/^I click save$/) do
  find('button#save').click
end

# salt failures log check
Then(/^the salt event log on server should contain no failures$/) do
  # upload salt event parser log
  file = 'salt_event_parser.py'
  source = "#{File.dirname(__FILE__)}/../upload_files/#{file}"
  dest = "/tmp/#{file}"
  return_code = file_inject(get_target('server'), source, dest)
  raise ScriptError, 'File injection failed' unless return_code.zero?

  # print failures from salt event log
  output, _code = get_target('server').run("python3 /tmp/#{file}")
  count_failures = output.to_s.scan(/false/).length
  output = output.join.to_s if output.respond_to?(:join)
  # Ignore the error if there is only the expected failure from min_salt_lock_packages.feature
  ignore_error = false
  ignore_error = output.include?('remove lock') if count_failures == 1 && !$build_validation
  raise ScriptError, "\nFound #{count_failures} failures in salt event log:\n#{output}\n" if count_failures.nonzero? && !ignore_error
end

# salt-ssh steps
When(/^I install Salt packages from "(.*?)"$/) do |host|
  target = get_target(host)
  pkgs = use_salt_bundle ? 'venv-salt-minion' : 'salt salt-minion'
  if suse_host?(host)
    target.run("test -e /usr/bin/zypper && zypper --non-interactive install -y #{pkgs}", check_errors: false)
  elsif slemicro_host?(host)
    target.run("test -e /usr/bin/zypper && transactional-update -n pkg install #{pkgs}", check_errors: false)
  elsif rh_host?(host)
    target.run("test -e /usr/bin/yum && yum -y install #{pkgs}", check_errors: false)
  elsif deb_host?(host)
    pkgs = 'salt-common salt-minion' if product != 'Uyuni'
    target.run("test -e /usr/bin/apt && apt -y install #{pkgs}", check_errors: false)
  end
end

When(/^I enable repositories before installing Salt on this "([^"]*)"$/) do |host|
  step %(I enable repository "tools_additional_repo" on this "#{host}" without error control)
end

When(/^I disable repositories after installing Salt on this "([^"]*)"$/) do |host|
  step %(I disable repository "tools_additional_repo" on this "#{host}" without error control)
end

# minion bootstrap steps
Then(/^I run spacecmd listevents for "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  get_target('server').run('spacecmd -u admin -p admin clear_caches')
  get_target('server').run("spacecmd -u admin -p admin system_listevents #{system_name}")
end

When(/^I enter KVM Server password$/) do
  step %(I enter "#{ENV.fetch('VIRTHOST_KVM_PASSWORD', nil)}" as "password")
end

When(/^I perform a full salt minion cleanup on "([^"]*)"$/) do |host|
  node = get_target(host)
  if use_salt_bundle
    if slemicro_host?(host)
      node.run('transactional-update --continue -n pkg rm venv-salt-minion', check_errors: false)
    elsif rh_host?(host)
      node.run('yum -y remove --setopt=clean_requirements_on_remove=1 venv-salt-minion', check_errors: false)
    elsif deb_host?(host)
      node.run('apt-get --assume-yes remove venv-salt-minion && apt-get --assume-yes purge venv-salt-minion && apt-get --assume-yes autoremove', check_errors: false)
    else
      node.run('zypper --non-interactive remove --clean-deps -y venv-salt-minion', check_errors: false)
    end
    node.run('rm -Rf /root/salt /var/cache/venv-salt-minion /run/venv-salt-minion /var/venv-salt-minion.log /etc/venv-salt-minion /var/tmp/.root*', check_errors: false)
  else
    if slemicro_host?(host)
      node.run('transactional-update --continue -n pkg rm salt salt-minion', check_errors: false)
    elsif rh_host?(host)
      node.run('yum -y remove --setopt=clean_requirements_on_remove=1 salt salt-minion', check_errors: false)
    elsif deb_host?(host)
      node.run('apt-get --assume-yes remove salt-common salt-minion && apt-get --assume-yes purge salt-common salt-minion && apt-get --assume-yes autoremove', check_errors: false)
    else
      node.run('zypper --non-interactive remove --clean-deps -y salt salt-minion', check_errors: false)
    end
    node.run('rm -Rf /root/salt /var/cache/salt/minion /var/run/salt /run/salt /var/log/salt /etc/salt /var/tmp/.root*', check_errors: false)
  end
  step %(I disable the repositories "tools_update_repo tools_pool_repo" on this "#{host}" without error control)
end

When(/^I install a salt pillar top file for "([^"]*)" with target "([^"]*)" on the server$/) do |files, host|
  system_name = host == '*' ? '*' : get_system_name(host)
  script = "base:\n  '#{system_name}':\n"
  files.split(/, */).each do |file|
    script += "    - '#{file}'\n"
  end
  path = generate_temp_file('top.sls', script)
  inject_salt_pillar_file(path, 'top.sls')
  `rm #{path}`
end

When(/^I install the package download endpoint pillar file on the server$/) do
  filepath = '/srv/pillar/pkg_endpoint.sls'
  uri = URI.parse($custom_download_endpoint)
  content = "pkg_download_point_protocol: #{uri.scheme}\n" \
            "pkg_download_point_host: #{uri.host}\n" \
            "pkg_download_point_port: #{uri.port}"
  get_target('server').run("echo -e \"#{content}\" > #{filepath}")
end

When(/^I delete the package download endpoint pillar file from the server$/) do
  filepath = '/srv/pillar/pkg_endpoint.sls'
  return_code = file_delete(get_target('server'), filepath)
  raise ScriptError, 'File deletion failed' unless return_code.zero?
end

When(/^I install "([^"]*)" to custom formula metadata directory "([^"]*)"$/) do |file, formula|
  source = "#{File.dirname(__FILE__)}/../upload_files/#{file}"
  dest = "/srv/formula_metadata/#{formula}/#{file}"

  get_target('server').run("mkdir -p /srv/formula_metadata/#{formula}")
  return_code = file_inject(get_target('server'), source, dest)
  raise ScriptError, 'File injection failed' unless return_code.zero?

  get_target('server').run("chmod 644 #{dest}")
end

When(/^I migrate "([^"]*)" from salt-minion to venv-salt-minion$/) do |host|
  node = get_target(host)
  system_name = node.full_hostname
  migrate = "salt #{system_name} state.apply util.mgr_switch_to_venv_minion"
  get_target('server').run(migrate, check_errors: true, verbose: true)
end

When(/^I purge salt-minion on "([^"]*)" after a migration$/) do |host|
  node = get_target(host)
  system_name = node.full_hostname
  cleanup = %(salt #{system_name} state.apply util.mgr_switch_to_venv_minion pillar='{"mgr_purge_non_venv_salt_files": True, "mgr_purge_non_venv_salt": True}')
  get_target('server').run(cleanup, check_errors: true, verbose: true)
end

When(/^I apply highstate on "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  if host.include? 'ssh_minion'
    cmd = 'mgr-salt-ssh'
  elsif host.include?('minion') || host.include?('build') || host.include?('proxy')
    cmd = 'salt'
  end
  log "Salt command: #{cmd} #{system_name} state.highstate"
  get_target('server').run_until_ok("#{cmd} #{system_name} state.highstate")
end
