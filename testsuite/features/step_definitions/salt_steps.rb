# Copyright (c) 2015-2022 SUSE LLC
# Licensed under the terms of the MIT license.

require 'timeout'
require 'open-uri'
require 'tempfile'

Given(/^the Salt master can reach "(.*?)"$/) do |minion|
  system_name = get_system_name(minion)
  start = Time.now
  # 300 is the default 1st keepalive interval for the minion
  # where it realizes the connection is stuck
  repeat_until_timeout(timeout: 300, retries: 3, message: "Master can not communicate with #{minion}", report_result: true) do
    out, _code = $server.run("salt #{system_name} test.ping")
    if out.include?(system_name) && out.include?('True')
      finished = Time.now
      log "Took #{finished.to_i - start.to_i} seconds to contact the minion"
      break
    end
    sleep 1
    out
  end
end

When(/^I get the contents of the remote file "(.*?)"$/) do |filename|
  $output, _code = $server.run("cat #{filename}")
end

When(/^I stop salt-minion on "(.*?)"$/) do |minion|
  node = get_target(minion)
  os_version, os_family = get_os_version(node)
  if os_family =~ /^sles/ && os_version =~ /^11/
    node.run('rcsalt-minion stop', check_errors: false)
  else
    node.run('systemctl stop salt-minion', check_errors: false)
  end
end

When(/^I start salt-minion on "(.*?)"$/) do |minion|
  node = get_target(minion)
  os_version, os_family = get_os_version(node)
  if os_family =~ /^sles/ && os_version =~ /^11/
    node.run('rcsalt-minion start', check_errors: false)
  else
    node.run('systemctl start salt-minion', check_errors: false)
  end
end

When(/^I restart salt-minion on "(.*?)"$/) do |minion|
  node = get_target(minion)
  os_version, os_family = get_os_version(node)
  if os_family =~ /^sles/ && os_version =~ /^11/
    node.run('rcsalt-minion restart', check_errors: false)
  else
    node.run('systemctl restart salt-minion', check_errors: false)
  end
end

When(/^I wait at most (\d+) seconds until Salt master sees "([^"]*)" as "([^"]*)"$/) do |key_timeout, minion, key_type|
  cmd = "salt-key --list #{key_type}"
  repeat_until_timeout(timeout: key_timeout.to_i, message: "Minion '#{minion}' is not listed among #{key_type} keys on Salt master") do
    system_name = get_system_name(minion)
    unless system_name.empty?
      output, return_code = $server.run(cmd, check_errors: false)
      break if return_code.zero? && output.include?(system_name)
    end
    sleep 1
  end
end

When(/^I wait until no Salt job is running on "([^"]*)"$/) do |minion|
  target = get_target(minion)
  repeat_until_timeout(message: "A Salt job is still running on #{minion}") do
    output, _code = target.run('salt-call -lquiet saltutil.running')
    break if output == "local:\n"
    sleep 3
  end
end

When(/^I delete "([^"]*)" key in the Salt master$/) do |host|
  system_name = get_system_name(host)
  $output, _code = $server.run("salt-key -y -d #{system_name}", check_errors: false)
end

When(/^I accept "([^"]*)" key in the Salt master$/) do |host|
  system_name = get_system_name(host)
  $server.run("salt-key -y --accept=#{system_name}*")
end

When(/^I reject "([^"]*)" key in the Salt master$/) do |host|
  system_name = get_system_name(host)
  $server.run("salt-key -y --reject=#{system_name}")
end

When(/^I delete all keys in the Salt master$/) do
  $server.run('salt-key -y -D')
end

When(/^I get OS information of "([^"]*)" from the Master$/) do |host|
  system_name = get_system_name(host)
  $output, _code = $server.run("salt #{system_name} grains.get osfullname")
end

Then(/^it should contain a "([^"]*?)" text$/) do |content|
  assert_match(/#{content}/, $output)
end

Then(/^it should contain the OS of "([^"]*)"$/) do |host|
  node = get_target(host)
  _os_version, os_family = get_os_version(node)
  family = os_family =~ /^opensuse/ ? 'Leap' : 'SLES'
  assert_match(/#{family}/, $output)
end

When(/^I apply state "([^"]*)" to "([^"]*)"$/) do |state, host|
  system_name = get_system_name(host)
  $server.run("salt #{system_name} state.apply #{state}")
end

Then(/^salt\-api should be listening on local port (\d+)$/) do |port|
  $output, _code = $server.run("ss -ntl | grep #{port}")
  assert_match(/127.0.0.1:#{port}/, $output)
end

Then(/^salt\-master should be listening on public port (\d+)$/) do |port|
  $output, _code = $server.run("ss -ntl | grep #{port}")
  assert_match(/(0.0.0.0|\*|\[::\]):#{port}/, $output)
end

Then(/^the system should have a base channel set$/) do
  step %(I should not see a "This system has no Base Software Channel. You can select a Base Channel from the list below." text)
end

Then(/^"(.*?)" should not be registered$/) do |host|
  system_name = get_system_name(host)
  $api_test.auth.login('admin', 'admin')
  refute_includes($api_test.system.list_systems.map { |s| s['name'] }, system_name)
  $api_test.auth.logout
end

Then(/^"(.*?)" should be registered$/) do |host|
  system_name = get_system_name(host)
  $api_test.auth.login('admin', 'admin')
  assert_includes($api_test.system.list_systems.map { |s| s['name'] }, system_name)
  $api_test.auth.logout
end

Then(/^"(.*?)" should have been reformatted$/) do |host|
  system_name = get_system_name(host)
  output, _code = $server.run("salt #{system_name} file.file_exists /intact")
  raise "Minion #{host} is intact" unless output.include? 'False'
end

# user salt steps
Given(/^I am authorized as an example user with no roles$/) do
  $api_test.auth.login('admin', 'admin')
  @username = 'testuser' + (0...8).map { (65 + rand(26)).chr }.join.downcase
  $api_test.user.create_user(@username, 'linux')
  step %(I am authorized as "#{@username}" with password "linux")
  $api_test.auth.logout
end

When(/^I click on preview$/) do
  find('button#preview').click
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

When(/^I enter target "([^"]*)"$/) do |minion|
  fill_in('target', with: minion, fill_options: { clear: :backspace })
end

Then(/^I should see "([^"]*)" in the command output for "([^"]*)"$/) do |text, host|
  system_name = get_system_name(host)
  within("pre[id='#{system_name}-results']") do
    raise "Text '#{text}' not found in the results of #{system_name}" unless has_content?(text)
  end
end

# Salt formulas
When(/^I manually install the "([^"]*)" formula on the server$/) do |package|
  $server.run("zypper --non-interactive refresh")
  $server.run("zypper --non-interactive install --force #{package}-formula")
end

Then(/^I wait for "([^"]*)" formula to be installed on the server$/) do |package|
  $server.run_until_ok("rpm -q #{package}-formula")
end

When(/^I manually uninstall the "([^"]*)" formula from the server$/) do |package|
  $server.run("zypper --non-interactive remove #{package}-formula")
end

When(/^I synchronize all Salt dynamic modules on "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  $server.run("salt #{system_name} saltutil.sync_all")
end

When(/^I ([^ ]*) the "([^"]*)" formula$/) do |action, formula|
  # Complicated code because the checkbox is not a <input type=checkbox> but an <i>
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if action == 'check'
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if action == 'uncheck'
  # DOM refreshes content of chooseFormulas element by accessing it. Then conditions are evaluated properly.
  find('#chooseFormulas')['innerHTML']
  if has_xpath?(xpath_query, wait: DEFAULT_TIMEOUT)
    raise "xpath: #{xpath_query} not found" unless find(:xpath, xpath_query, wait: DEFAULT_TIMEOUT).click
  else
    xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if action == 'check'
    xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if action == 'uncheck'
    raise "xpath: #{xpath_query} not found" unless has_xpath?(xpath_query, wait: DEFAULT_TIMEOUT)
  end
end

Then(/^the "([^"]*)" formula should be ([^ ]*)$/) do |formula, state|
  # Complicated code because the checkbox is not a <input type=checkbox> but an <i>
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if state == 'checked'
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if state == 'unchecked'
  # DOM refreshes content of chooseFormulas element by accessing it. Then conditions are evaluated properly.
  find('#chooseFormulas')['innerHTML']
  raise "Checkbox is not #{state}" if has_xpath?(xpath_query)
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
  raise "The timezone #{timezone} is different to #{result}" unless result == timezone
end

Then(/^the keymap on "([^"]*)" should be "([^"]*)"$/) do |minion, keymap|
  node = get_target(minion)
  output, _code = node.run("grep 'KEYMAP=' /etc/vconsole.conf")
  raise "The keymap #{keymap} is different to the output: #{output.strip}" unless output.strip == "KEYMAP=#{keymap}"
end

Then(/^the language on "([^"]*)" should be "([^"]*)"$/) do |minion, language|
  node = get_target(minion)
  output, _code = node.run("grep 'RC_LANG=' /etc/sysconfig/language")
  unless output.strip == "RC_LANG=\"#{language}\""
    output, _code = node.run("grep 'LANG=' /etc/locale.conf")
    raise "The language #{language} is different to the output: #{output.strip}" unless output.strip == "LANG=#{language}"
  end
end

When(/^I refresh the pillar data$/) do
  $server.run("salt '#{$minion.full_hostname}' saltutil.refresh_pillar wait=True")
end

When(/^I wait until there is no pillar refresh salt job active$/) do
  repeat_until_timeout(message: "pillar refresh job still active") do
    output, = $server.run("salt-run jobs.active")
    break unless output.include?("saltutil.refresh_pillar")
    sleep 1
  end
end

def pillar_get(key, minion)
  system_name = get_system_name(minion)
  if minion == 'sle_minion'
    cmd = 'salt'
    extra_cmd = ''
  elsif %w[ssh_minion ceos_minion ubuntu_minion].include?(minion)
    cmd = 'salt-ssh'
    extra_cmd = '-i --roster-file=/tmp/roster_tests -w -W 2>/dev/null'
    $server.run("printf '#{system_name}:\n  host: #{system_name}\n  user: root\n  passwd: linux\n' > /tmp/roster_tests")
  else
    raise 'Invalid target'
  end
  $server.run("#{cmd} '#{system_name}' pillar.get '#{key}' #{extra_cmd}")
end

Then(/^the pillar data for "([^"]*)" should be "([^"]*)" on "([^"]*)"$/) do |key, value, minion|
  output, _code = pillar_get(key, minion)
  if value == ''
    raise "Output has more than one line: #{output}" unless output.split("\n").length == 1
  else
    raise "Output value wasn't found: #{output}" unless output.split("\n").length > 1
    raise "Output value is different than #{value}: #{output}" unless output.split("\n")[1].strip == value
  end
end

Then(/^the pillar data for "([^"]*)" should contain "([^"]*)" on "([^"]*)"$/) do |key, value, minion|
  output, _code = pillar_get(key, minion)
  raise "Output doesn't contain #{value}: #{output}" unless output.include? value
end

Then(/^the pillar data for "([^"]*)" should not contain "([^"]*)" on "([^"]*)"$/) do |key, value, minion|
  output, _code = pillar_get(key, minion)
  raise "Output contains #{value}: #{output}" if output.include? value
end

Then(/^the pillar data for "([^"]*)" should be empty on "([^"]*)"$/) do |key, minion|
  output, _code = pillar_get(key, minion)
  raise "Output has more than one line: #{output}" unless output.split("\n").length == 1
end

Given(/^I try to download "([^"]*)" from channel "([^"]*)"$/) do |rpm, channel|
  url = "https://#{$server.full_hostname}/rhn/manager/download/#{channel}/getPackage/#{rpm}"
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
  raise "xpath: #{xpath_query} not found" unless find(:xpath, xpath_query).click
end

When(/^I delete "([^"]*)" from the Rejected section$/) do |host|
  system_name = get_system_name(host)
  xpath_query = "//tr[td[contains(.,'#{system_name}')]]//button[@title = 'Delete']"
  raise "xpath: #{xpath_query} not found" unless find(:xpath, xpath_query).click
end

When(/^I see "([^"]*)" fingerprint$/) do |host|
  node = get_target(host)
  output, _code = node.run('salt-call --local key.finger')
  fing = output.split("\n")[1].strip!
  raise "Text: #{fing} not found" unless has_content?(fing)
end

When(/^I accept "([^"]*)" key$/) do |host|
  system_name = get_system_name(host)
  xpath_query = "//tr[td[contains(.,'#{system_name}')]]//button[@title = 'Accept']"
  raise "xpath: #{xpath_query} not found" unless find(:xpath, xpath_query).click
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

When(/^I click undo for "(.*?)"$/) do |pkg|
  find("button##{pkg}-undo").click
end

When(/^I click apply$/) do
  find('button#apply').click
end

When(/^I click save$/) do
  find('button#save').click
end

# salt-ssh steps
When(/^I uninstall Salt packages from "(.*?)"$/) do |host|
  target = get_target(host)
  if %w[sle_minion ssh_minion sle_client].include?(host)
    target.run("test -e /usr/bin/zypper && zypper --non-interactive remove -y salt salt-minion", check_errors: false)
  elsif %w[ceos_minion].include?(host)
    target.run("test -e /usr/bin/yum && yum -y remove salt salt-minion", check_errors: false)
  elsif %w[ubuntu_minion].include?(host)
    target.run("test -e /usr/bin/apt && apt -y remove salt-common salt-minion", check_errors: false)
  end
end

When(/^I install Salt packages from "(.*?)"$/) do |host|
  target = get_target(host)
  if %w[sle_minion ssh_minion sle_client].include?(host)
    target.run("test -e /usr/bin/zypper && zypper --non-interactive install -y salt salt-minion", check_errors: false)
  elsif %w[ceos_minion].include?(host)
    target.run("test -e /usr/bin/yum && yum -y install salt salt-minion", check_errors: false)
  elsif %w[ubuntu_minion].include?(host)
    target.run("test -e /usr/bin/apt && apt -y install salt-common salt-minion", check_errors: false)
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
  $server.run('spacecmd -u admin -p admin clear_caches')
  $server.run("spacecmd -u admin -p admin system_listevents #{system_name}")
end

When(/^I enter "([^"]*)" password$/) do |host|
  raise "#{host} minion password is unknown" unless %w[kvm_server xen_server].include?(host)
  step %(I enter "#{ENV['VIRTHOST_KVM_PASSWORD']}" as "password") if host == "kvm_server"
  step %(I enter "#{ENV['VIRTHOST_XEN_PASSWORD']}" as "password") if host == "xen_server"
end

When(/^I perform a full salt minion cleanup on "([^"]*)"$/) do |host|
  node = get_target(host)
  if host.include? 'ceos'
    node.run('yum -y remove --setopt=clean_requirements_on_remove=1 salt salt-minion', check_errors: false)
  elsif (host.include? 'ubuntu') || (host.include? 'debian')
    node.run('apt-get --assume-yes remove salt-common salt-minion && apt-get --assume-yes purge salt-common salt-minion && apt-get --assume-yes autoremove', check_errors: false)
  else
    node.run('zypper --non-interactive remove --clean-deps -y salt salt-minion spacewalk-proxy-salt', check_errors: false)
  end
  node.run('rm -Rf /root/salt /var/cache/salt/minion /var/run/salt /var/log/salt /etc/salt /var/tmp/.root*', check_errors: false)
  step %(I disable the repositories "tools_update_repo tools_pool_repo" on this "#{host}" without error control)
end

When(/^I install a salt pillar top file for "([^"]*)" with target "([^"]*)" on the server$/) do |file, host|
  system_name = host == "*" ? "*" : get_system_name(host)
  script = "base:\n" \
            "  '#{system_name}':\n" \
            "    - '#{file}'\n"
  path = generate_temp_file('top.sls', script)
  inject_salt_pillar_file(path, 'top.sls')
  `rm #{path}`
end

When(/^I install a salt pillar file with name "([^"]*)" on the server$/) do |file|
  source = File.dirname(__FILE__) + '/../upload_files/' + file
  inject_salt_pillar_file(source, file)
end

When(/^I delete a salt "([^"]*)" file with name "([^"]*)" on the server$/) do |type, file|
  case type
  when 'state'
    path = "/srv/salt/" + file
  when 'pillar'
    path = "/srv/pillar/" + file
  else
    raise 'Invalid type.'
  end
  return_code = file_delete($server, path)
  raise 'File Deletion failed' unless return_code.zero?
end

When(/^I install "([^"]*)" to custom formula metadata directory "([^"]*)"$/) do |file, formula|
  source = File.dirname(__FILE__) + '/../upload_files/' + file
  dest = "/srv/formula_metadata/" + formula + '/' + file

  $server.run("mkdir -p /srv/formula_metadata/" + formula)
  return_code = file_inject($server, source, dest)
  raise 'File injection failed' unless return_code.zero?
  $server.run("chmod 644 " + dest)
end

When(/^I kill remaining Salt jobs on "([^"]*)"$/) do |minion|
  system_name = get_system_name(minion)
  output, _code = $server.run("salt #{system_name} saltutil.kill_all_jobs")
  if output.include?(system_name) && output.include?('Signal 9 sent to job')
    log output
  end
end
