# Copyright 2015-2018 SUSE LLC
require 'timeout'
require 'open-uri'
require 'tempfile'

Given(/^the Salt master can reach "(.*?)"$/) do |minion|
  system_name = get_system_name(minion)
  begin
    start = Time.now
    # 300 is the default 1st keepalive interval for the minion
    # where it realizes the connection is stuck
    keepalive_timeout = 300
    Timeout.timeout(keepalive_timeout) do
      # only try 3 times
      3.times do
        out, _code = $server.run("salt #{system_name} test.ping")
        if out.include?(system_name) && out.include?('True')
          finished = Time.now
          puts "Took #{finished.to_i - start.to_i} seconds to contact the minion"
          break
        end
        sleep(1)
      end
    end
  rescue Timeout::Error
    raise "Master can not communicate with #{minion}: #{@output[:stdout]}"
  end
end

When(/^I get the contents of the remote file "(.*?)"$/) do |filename|
  $output, _code = $server.run("cat #{filename}")
end

When(/^I stop salt-master$/) do
  $server.run('systemctl stop salt-master', false)
end

When(/^I start salt-master$/) do
  $server.run('systemctl start salt-master', false)
end

When(/^I stop salt-minion on "(.*?)"$/) do |minion|
  node = get_target(minion)
  node.run('rcsalt-minion stop', false) if minion == 'sle-minion'
  node.run('systemctl stop salt-minion', false) if minion == 'ceos-minion' or minion == 'ceos-ssh-minion'
end

When(/^I start salt-minion on "(.*?)"$/) do |minion|
  node = get_target(minion)
  node.run('rcsalt-minion restart', false) if minion == 'sle-minion'
  node.run('systemctl restart salt-minion', false) if minion == 'ceos-minion' or minion == 'ceos-ssh-minion'
end

When(/^I restart salt-minion on "(.*?)"$/) do |minion|
  node = get_target(minion)
  node.run('rcsalt-minion restart', false) if minion == 'sle-minion'
  node.run('systemctl restart salt-minion', false) if minion == 'ceos-minion' or minion == 'ceos-ssh-minion'
end

When(/^I wait at most (\d+) seconds until Salt master sees "([^"]*)" as "([^"]*)"$/) do |key_timeout, minion, key_type|
  system_name = get_system_name(minion)
  cmd = "salt-key --list #{key_type}"
  output = ''
  begin
    Timeout.timeout(key_timeout.to_i) do
      loop do
        output, return_code = $server.run(cmd, false)
        break if return_code.zero? && output.include?(system_name)
        sleep 1
      end
    end
  rescue Timeout::Error
    raise "Minion #{system_name} is not listed among #{key_type} keys on Salt master after #{key_timeout} seconds"
  end
end

When(/^I wait until no Salt job is running on "([^"]*)"$/) do |minion|
  target = get_target(minion)
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        output, _code = target.run('salt-call -lquiet saltutil.running')
        break if output == "local:\n"
        sleep 3
      end
    end
  rescue Timeout::Error
    raise "A Salt job is still running on #{minion} after timeout"
  end
end

When(/^I wait until onboarding is completed for "([^"]*)"$/) do |host|
  steps %(
    When I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "#{host}", refreshing the page
    And I follow this "#{host}" link
    And I wait until event "Package List Refresh scheduled by (none)" is completed
  )
end

When(/^I delete "([^"]*)" key in the Salt master$/) do |host|
  system_name = get_system_name(host)
  $output, _code = $server.run("salt-key -y -d #{system_name}", false)
end

When(/^I accept "([^"]*)" key in the Salt master$/) do |host|
  system_name = get_system_name(host)
  $server.run("salt-key -y --accept=#{system_name}")
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

Then(/^it should contain a "(.*?)" text$/) do |content|
  assert_match(/#{content}/, $output)
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
  @rpc = XMLRPCSystemTest.new(ENV['SERVER'])
  @rpc.login('admin', 'admin')
  refute_includes(@rpc.list_systems.map { |s| s['name'] }, system_name)
end

Then(/^"(.*?)" should be registered$/) do |host|
  system_name = get_system_name(host)
  @rpc = XMLRPCSystemTest.new(ENV['SERVER'])
  @rpc.login('admin', 'admin')
  assert_includes(@rpc.list_systems.map { |s| s['name'] }, system_name)
end

# user salt steps
Given(/^I am authorized as an example user with no roles$/) do
  @rpc = XMLRPCUserTest.new(ENV['SERVER'])
  @rpc.login('admin', 'admin')
  @username = 'testuser' + (0...8).map { (65 + rand(26)).chr }.join.downcase
  @rpc.create_user(@username, 'linux')
  step %(I am authorized as "#{@username}" with password "linux")
end

Then(/^I can cleanup the no longer needed user$/) do
  @rpc.delete_user(@username)
end

When(/^I click on preview$/) do
  find('button#preview').click
end

When(/^I click on run$/) do
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        begin
          find('button#run').click
          break
        rescue Capybara::ElementNotFound
          sleep(5)
        end
      end
    end
  rescue Timeout::Error
    raise 'Run button not found'
  end
end

Then(/^I should see "([^"]*)" hostname$/) do |host|
  system_name = get_system_name(host)
  raise unless page.has_content?(system_name)
end

Then(/^I should not see "([^"]*)" hostname$/) do |host|
  system_name = get_system_name(host)
  raise if page.has_content?(system_name)
end

When(/^I expand the results for "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  find("div[id='#{system_name}']").click
end

When(/^I enter command "([^"]*)"$/) do |cmd|
  fill_in 'command', with: cmd
end

When(/^I enter target "([^"]*)"$/) do |minion|
  fill_in 'target', with: minion
end

Then(/^I should see "([^"]*)" in the command output for "([^"]*)"$/) do |text, host|
  system_name = get_system_name(host)
  within("pre[id='#{system_name}-results']") do
    raise unless page.has_content?(text)
  end
end

Then(/^I click on the css "(.*)" until page does not contain "([^"]*)" text$/) do |css, text|
  not_found = false
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        unless page.has_content?(text)
          not_found = true
          break
        end
        find(css).click
        sleep 3
      end
    end
  rescue Timeout::Error
    raise "'#{text}' still found after several tries"
  end
  raise unless not_found
end

Then(/^I click on the css "(.*)" until page does contain "([^"]*)" text$/) do |css, text|
  found = false
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        if page.has_content?(text)
          found = true
          break
        end
        find(css).click
        sleep 3
      end
    end
  rescue Timeout::Error
    raise "'#{text}' cannot be found after several tries"
  end
  raise unless found
end

When(/^I click on the css "(.*)"$/) do |css|
  find(css).click
end

When(/^I enter "(.*)" in the css "(.*)"$/) do |input, css|
  find(css).set(input)
end

# salt formulas
When(/^I manually install the "([^"]*)" formula on the server$/) do |package|
  $server.run("zypper --non-interactive install --force #{package}-formula")
end

When(/^I manually uninstall the "([^"]*)" formula from the server$/) do |package|
  $server.run("zypper --non-interactive remove #{package}-formula")
end

When(/^I ([^ ]*) the "([^"]*)" formula$/) do |action, formula|
  # Complicated code because the checkbox is not a <input type=checkbox> but an <i>
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if action == 'check'
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if action == 'uncheck'
  if all(:xpath, xpath_query).any?
    raise unless find(:xpath, xpath_query).click
  else
    xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if action == 'check'
    xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if action == 'uncheck'
    assert all(:xpath, xpath_query).any?, 'Checkbox could not be found'
  end
end

Then(/^the "([^"]*)" formula should be ([^ ]*)$/) do |formula, state|
  # Complicated code because the checkbox is not a <input type=checkbox> but an <i>
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if state == 'checked'
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if state == 'unchecked'
  raise "Checkbox is not #{action}" if all(:xpath, xpath_query).any?
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if state == 'checked'
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if state == 'unchecked'
  assert all(:xpath, xpath_query).any?, 'Checkbox could not be found'
end

When(/^I select "([^"]*)" in (.*) field$/) do |value, box|
  boxids = { 'timezone name'   => 'timezone$name',
             'language'        => 'keyboard_and_language$language',
             'keyboard layout' => 'keyboard_and_language$keyboard_layout' }
  select(value, from: boxids[box])
end

Then(/^the timezone on "([^"]*)" should be "([^"]*)"$/) do |minion, timezone|
  node = get_target(minion)
  output, _code = node.run('date +%Z')
  result = output.strip
  result = 'CET' if result == 'CEST'
  raise unless result == timezone
end

Then(/^the keymap on "([^"]*)" should be "([^"]*)"$/) do |minion, keymap|
  node = get_target(minion)
  output, _code = node.run('cat /etc/vconsole.conf')
  raise unless output.strip == "KEYMAP=#{keymap}"
end

Then(/^the language on "([^"]*)" should be "([^"]*)"$/) do |minion, language|
  node = get_target(minion)
  output, _code = node.run("grep 'RC_LANG=' /etc/sysconfig/language")
  unless output.strip == "RC_LANG=\"#{language}\""
    output, _code = node.run("grep 'LANG=' /etc/locale.conf")
    raise unless output.strip == "LANG=#{language}"
  end
end

When(/^I refresh the pillar data$/) do
  $server.run("salt '#{$minion.ip}' saltutil.refresh_pillar")
end

Then(/^the pillar data for "([^"]*)" should be "([^"]*)" on "([^"]*)"$/) do |key, value, minion|
  system_name = get_system_name(minion)
  if minion == 'sle-minion'
    cmd = 'salt'
    extra_cmd = ''
  elsif minion == 'ssh-minion' or minion == 'ceos-minion' or minion == 'ceos-ssh-minion'
    cmd = 'salt-ssh'
    extra_cmd = '-i --roster-file=/tmp/roster_tests -w -W 2>/dev/null'
    $server.run("printf '#{system_name}:\n  host: #{system_name}\n  user: root\n  passwd: linux\n' > /tmp/roster_tests")
  else
    raise 'Invalid target'
  end
  output, _code = $server.run("#{cmd} '#{system_name}' pillar.get '#{key}' #{extra_cmd}")
  if value == ''
    raise unless output.split("\n").length == 1
  else
    raise unless output.split("\n")[1].strip == value
  end
end

Then(/^the pillar data for "([^"]*)" should be empty on "([^"]*)"$/) do |key, minion|
  step %(the pillar data for "#{key}" should be "" on "#{minion}")
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
  raise unless find(:xpath, xpath_query).click
end

When(/^I delete "([^"]*)" from the Rejected section$/) do |host|
  system_name = get_system_name(host)
  xpath_query = "//tr[td[contains(.,'#{system_name}')]]//button[@title = 'Delete']"
  raise unless find(:xpath, xpath_query).click
end

When(/^I see "([^"]*)" fingerprint$/) do |host|
  node = get_target(host)
  output, _code = node.run('salt-call --local key.finger')
  fing = output.split("\n")[1].strip!
  raise unless page.has_content?(fing)
end

When(/^I accept "([^"]*)" key$/) do |host|
  system_name = get_system_name(host)
  xpath_query = "//tr[td[contains(.,'#{system_name}')]]//button[@title = 'Accept']"
  raise unless find(:xpath, xpath_query).click
end

When(/^I go to the minion onboarding page$/) do
  steps %(
    And I follow "Salt"
    And I follow "Keys"
    )
end

When(/^I go to the bootstrapping page$/) do
  steps %(
    And I follow "Systems"
    And I follow "Bootstrapping"
    )
end

When(/^I refresh page until I see "(.*?)" hostname as text$/) do |minion|
  within('#spacewalk-content') do
    steps %(
     And I wait until I see the name of "#{minion}", refreshing the page
      )
  end
end

When(/^I refresh page until I do not see "(.*?)" hostname as text$/) do |minion|
  within('#spacewalk-content') do
    steps %(
     And I wait until I do not see the name of "#{minion}", refreshing the page
      )
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

Then(/^I wait for "([^"]*)" to be uninstalled on "([^"]*)"$/) do |package, host|
  node = get_target(host)
  uninstalled = false
  output = ''
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        output, code = node.run("rpm -q #{package}", false)
        if code.nonzero?
          uninstalled = true
          break
        end
        sleep 1
      end
    end
  end
  raise "Package removal failed (Code #{$CHILD_STATUS}): #{$ERROR_INFO}: #{output}" unless uninstalled
end

Then(/^I wait for "([^"]*)" to be installed on this "([^"]*)"$/) do |package, host|
  node = get_target(host)
  node.run_until_ok("rpm -q #{package}")
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
  if ['sle-minion', 'ssh-minion', 'sle-client', 'sle-migrated-minion'].include?(host)
    target.run("test -e /usr/bin/zypper && zypper --non-interactive remove -y salt salt-minion", false)
  elsif ['ceos-minion', 'ceos-ssh-minion'].include?(host)
    target.run("test -e /usr/bin/yum && yum -y remove salt salt-minion", false)
  end
end

# minion bootstrap steps
When(/^I enter the hostname of "([^"]*)" as "([^"]*)"$/) do |host, hostname|
  system_name = get_system_name(host)
  step %(I enter "#{system_name}" as "#{hostname}")
end

When(/^I select the hostname of the proxy from "([^"]*)"$/) do |proxy|
  next if $proxy.nil?
  step %(I select "#{$proxy.full_hostname}" from "#{proxy}")
end

Then(/^I run spacecmd listevents for "([^"]*)"$/) do |host|
  system_name = get_system_name(host)
  $server.run('spacecmd -u admin -p admin clear_caches')
  $server.run("spacecmd -u admin -p admin system_listevents #{system_name}")
end

And(/^I cleanup minion "([^"]*)"$/) do |target|
  if target == 'sle-minion'
    $minion.run('rcsalt-minion stop')
    $minion.run('rm -Rf /var/cache/salt/minion')
  elsif target == 'ceos-minion' or target == 'ceos-ssh-minion'
    $ceos_minion.run('systemctl stop salt-minion')
    $ceos_minion.run('rm -Rf /var/cache/salt/minion')
  end
end
