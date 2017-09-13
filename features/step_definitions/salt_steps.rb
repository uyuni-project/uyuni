# Copyright 2015-2017 SUSE LLC
require 'timeout'
require 'open-uri'
require 'tempfile'

Given(/^salt-minion is configured on "(.*?)"$/) do |minion|
  node = get_target(minion)
  # cleanup the key in case the image was reused
  # to run the test twice
  step %(I delete "#{minion}" key in the Salt master)
  step %(I stop salt-minion on "#{minion}")
  step %(I stop salt-master)
  key = '/etc/salt/pki/minion/minion_master.pub'
  if file_exists?(node, key)
    file_delete(node, key)
    puts "Key #{key} has been removed on minion"
  end
  cmd = " echo  \'master : #{$server_ip}\' > /etc/salt/minion.d/susemanager.conf"
  node.run(cmd, false)
  step %(I start salt-master)
  step %(I start salt-minion on "#{minion}")
end

Given(/^the salt-master can reach "(.*?)"$/) do |minion|
  node = get_target(minion)
  begin
    start = Time.now
    # 300 is the default 1st keepalive interval for the minion
    # where it realizes the connection is stuck
    KEEPALIVE_TIMEOUT = 300
    Timeout.timeout(KEEPALIVE_TIMEOUT) do
      # only try 3 times
      3.times do
        out, _code = $server.run("salt #{node.full_hostname} test.ping")
        if out.include?(node.full_hostname) && out.include?('True')
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

Given(/^I am on the Systems overview page of "(.*?)"$/) do |minion|
  steps %(
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow "#{minion}" link
    )
end

When(/^I follow "(.*?)" link$/) do |host|
  node = get_target(host)
  step %(I follow "#{node.hostname}")
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
  node.run('systemctl stop salt-minion', false) if minion == 'ceos-minion'
end

When(/^I start salt-minion on "(.*?)"$/) do |minion|
  node = get_target(minion)
  node.run('rcsalt-minion stop', false) if minion == 'sle-minion'
  node.run('systemctl stop salt-minion', false) if minion == 'ceos-minion'
end

When(/^I restart salt-minion on "(.*?)"$/) do |minion|
  node = get_target(minion)
  node.run('rcsalt-minion stop', false) if minion == 'sle-minion'
  node.run('systemctl stop salt-minion', false) if minion == 'ceos-minion'
end

Then(/^salt-minion should be running on "(.*?)"$/) do |minion|
  node = get_target(minion)
  i = 0
  # bsc 1056615
  salt_st = 'rcsalt-minion status'
  salt_st = 'systemctl status salt-minion' if minion == 'ceos-minion'
  MAX_ITER = 40
  loop do
    _out, code = node.run(salt_st, false)
    break if code.zero?
    sleep 5
    puts 'sleeping 5 secs, minion not active.'
    i += 1
    raise 'TIMEOUT; something wrong with minion status' if i == MAX_ITER
  end
end

When(/^I list "(.*?)" keys at Salt Master$/) do |key_type|
  $output, _code = $server.run("salt-key --list #{key_type}", false)
  $output.strip
end

Then(/^the list of the "(.*?)" keys should contain "(.*?)" hostname$/) do |key_type, minion|
  node = get_target(minion)
  $output, _code = $server.run("salt-key --list #{key_type}", false)
  assert_match(node.full_hostname, $output, "minion #{node.full_hostname} is not listed as #{key_type} key on salt-master #{$output}")
end

When(/^I wait until no Salt job is running on "(.*?)"$/) do |minion|
  if minion == 'sle-minion'
    target = $minion
  elsif minion == 'ceos-minion'
    target = $ceos
  elsif minion == 'sle-migrated-minion'
    target = $client
  else
    raise 'no valid name of minion given! '
  end
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        $output, _code = target.run('salt-call -lquiet saltutil.running')
        break if $output == "local:\n"
        sleep 3
      end
    end
  rescue Timeout::Error
    raise "a Salt job is still running on #{minion} after timeout"
  end
end

When(/^we wait till Salt master sees "(.*?)" as "(.*?)"$/) do |minion, key_type|
  steps %(
    When I list "#{key_type}" keys at Salt Master
    Then the list of the "#{key_type}" keys should contain "#{minion}" hostname
    )
end

Given(/^"(.*?)" key is "(.*?)"$/) do |minion, key_type|
  node = get_target(minion)
  step %(I list "#{key_type}" keys at Salt Master)
  unless $output.include?(node.hostname)
    if key_type == 'accepted'
      step %(I accept "#{minion}" key in the Salt master)
    elsif key_type == 'rejected'
      step %(I reject "#{minion}" key in the Salt master)
    elsif key_type == 'unaccepted'
      step %(I delete "#{minion}" key in the Salt master)
    else
      raise 'no valid key_type!'
    end
    steps %(
      And I restart salt-minion on "#{minion}"
      And we wait till Salt master sees "#{minion}" as "#{key_type}"
        )
  end
end

Then(/^I wait until onboarding is completed for "([^"]*)"$/) do |system|
  steps %(
    When I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "#{system}", refreshing the page
    And I follow this "#{system}" link
    When I follow "Events"
    And I follow "History"
  )
  puts 'I navigate to rhn/systems/Overview.do page, and'
  puts "I go to #{system} events history page"
  steps %(
    Then I try to reload page until contains "Package List Refresh scheduled by (none)" text
    And I follow first "Package List Refresh scheduled by (none)"
    And I wait until I see "This action's status is: Completed." text, refreshing the page
    And I wait for "5" seconds
  )
end

Then(/^I wait until OpenSCAP scan is completed for "([^"]*)"$/) do |system|
  steps %(
    When I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "#{system}", refreshing the page
    And I follow this "#{system}" link
    When I follow "Events"
    And I follow "History"
    Then I try to reload page until contains "OpenSCAP xccdf scanning" text
    And I follow first "OpenSCAP xccdf scanning"
    And I wait until I see "This action's status is: Completed." text, refreshing the page
    And I wait for "5" seconds
  )
end

When(/^I delete "(.*?)" key in the Salt master$/) do |minion|
  node = get_target(minion)
  $output, _code = $server.run("salt-key -y -d #{node.hostname}", false)
end

When(/^I accept "(.*?)" key in the Salt master$/) do |minion|
  node = get_target(minion)
  $server.run("salt-key -y --accept=#{node.hostname}")
end

When(/^I reject "(.*?)" key in the Salt master$/) do |minion|
  node = get_target(minion)
  $server.run("salt-key -y --reject=#{node.hostname}")
end

When(/^I delete all keys in the Salt master$/) do
  $server.run('salt-key -y -D')
end

When(/^I accept all Salt unaccepted keys$/) do
  $server.run('salt-key -y -A')
end

When(/^I get OS information of "(.*?)" from the Master$/) do |minion|
  node = get_target(minion)
  $output, _code = $server.run("salt #{node.full_hostname} grains.get osfullname")
end

Then(/^it should contain a "(.*?)" text$/) do |content|
  assert_match(/#{content}/, $output)
end

Then(/^salt\-api should be listening on local port (\d+)$/) do |port|
  $output, _code = $server.run("ss -nta | grep #{port}")
  assert_match(/127.0.0.1:#{port}/, $output)
end

Then(/^salt\-master should be listening on public port (\d+)$/) do |port|
  $output, _code = $server.run("ss -nta | grep #{port}")
  assert_match(/\*:#{port}/, $output)
end

Then(/^the system should have a Base channel set$/) do
  step %(I should not see a "This system has no Base Software Channel. You can select a Base Channel from the list below." text)
end

And(/^"(.*?)" is not registered in Spacewalk$/) do |host|
  node = get_target(host)
  @rpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
  @rpc.login('admin', 'admin')
  sid = @rpc.listSystems.select { |s| s['name'] == node.full_hostname }.map { |s| s['id'] }.first
  @rpc.deleteSystem(sid) if sid
  refute_includes(@rpc.listSystems.map { |s| s['id'] }, node.full_hostname)
end

Given(/^"(.*?)" is registered in Spacewalk$/) do |host|
  node = get_target(host)
  @rpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
  @rpc.login('admin', 'admin')
  assert_includes(@rpc.listSystems.map { |s| s['name'] }, node.full_hostname)
end

Then(/^all local repositories are disabled$/) do
  Nokogiri::XML(`zypper -x lr`)
          .xpath('//repo-list')
          .children
          .select { |node| node.is_a?(Nokogiri::XML::Element) }
          .select { |element| element.name == 'repo' }
          .reject { |repo| repo[:alias].include?('susemanager:') }
          .map do |repo|
    assert_equal('0', repo[:enabled],
                 "repo #{repo[:alias]} should be disabled")
  end
end

When(/^I enter as remote command a script to watch a picked-up test file$/) do
  steps %(
    When I enter as remote command this script in
      """
      #!/bin/bash
      while [ ! -f /tmp/PICKED-UP-#{$PROCESS_ID}.test ]
      do
        sleep 1
      done
      rm /tmp/PICKED-UP-#{$PROCESS_ID}.test
      """)
end

# user salt steps
Given(/^I am authorized as an example user with no roles$/) do
  @rpc = XMLRPCUserTest.new(ENV['TESTHOST'])
  @rpc.login('admin', 'admin')
  @username = 'testuser' + (0...8).map { (65 + rand(26)).chr }.join.downcase
  @rpc.createUser(@username, 'linux')
  step %(I am authorized as "#{@username}" with password "linux")
end

Then(/^I can cleanup the no longer needed user$/) do
  @rpc.deleteUser(@username)
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

When(/^I should see "(.*)" hostname$/) do |host|
  node = get_target(host)
  raise unless page.has_content?(node.full_hostname)
end

When(/^I should not see "(.*)" hostname$/) do |host|
  node = get_target(host)
  raise if page.has_content?(node.full_hostname)
end

When(/^I expand the results for "(.*)"$/) do |host|
  node = get_target(host)
  find("div[id='#{node.full_hostname}']").click
end

Then(/^I enter command "([^"]*)"$/) do |cmd|
  fill_in 'command', with: cmd
end

Then(/^I enter target "([^"]*)"$/) do |minion|
  fill_in 'target', with: minion
end

Then(/^I should see "([^"]*)" in the command output for "(.*)"$/) do |text, minion|
  node = get_target(minion)
  within("pre[id='#{node.full_hostname}-results']") do
    raise unless page.has_content?(text)
  end
end

Then(/^I manually install the "([^"]*)" package in the minion$/) do |package|
  if file_exists?($minion, '/usr/bin/zypper')
    cmd = "zypper --non-interactive install -y #{package}"
  elsif file_exists?($minion, '/usr/bin/yum')
    cmd = "yum -y install #{package}"
  else
    raise 'not found: zypper or yum'
  end
  $minion.run(cmd, false)
end

Then(/^I manually remove the "([^"]*)" package in the minion$/) do |package|
  if file_exists?($minion, '/usr/bin/zypper')
    cmd = "zypper --non-interactive remove -y #{package}"
  elsif file_exists?($minion, '/usr/bin/yum')
    cmd = "yum -y remove #{package}"
  else
    raise 'not found: zypper or yum'
  end
  $minion.run(cmd, false)
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
        unless page.has_content?(text)
          found = true
          break
        end
        find(css).click
      end
    end
  rescue Timeout::Error
    raise "'#{text}' cannot be found after several tries"
  end
  raise unless found
end

# states catalog
When(/^I enter the salt state$/) do |multiline|
  within('div#content-state') do
    find('.ace_text-input', visible: false).set(multiline)
  end
end

When(/^I click on the css "(.*)"$/) do |css|
  find(css).click
end

When(/^I click on the css "(.*)" and confirm$/) do |css|
  accept_alert do
    find(css).click
  end
end

When(/^I enter "(.*)" in the css "(.*)"$/) do |input, css|
  find(css).set(input)
end

When(/^I select the state "(.*)"$/) do |state|
  find("input##{state}-cbox").click
end

# salt formulas
When(/^I manually install the "([^"]*)" formula on the server$/) do |package|
  $server.run("zypper --non-interactive install -y #{package}-formula")
end

When(/^I ([^"]*) the "([^"]*)" formula$/) do |action, formula|
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

Then(/^the "([^"]*)" formula should be ([^"]*)$/) do |formula, action|
  # Complicated code because the checkbox is not a <input type=checkbox> but an <i>
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if action == 'checked'
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if action == 'unchecked'
  raise "Checkbox is not #{action}" if all(:xpath, xpath_query).any?
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if action == 'checked'
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if action == 'unchecked'
  assert all(:xpath, xpath_query).any?, 'Checkbox could not be found'
end

When(/^I select "([^"]*)" in (.*) field$/) do |value, box|
  boxid = case box
          when 'timezone name'
            "timezone\$name"
          when 'language'
            "keyboard_and_language\$language"
          when 'keyboard layout'
            "keyboard_and_language\$keyboard_layout"
          end
  select(value, from: boxid)
end

Then(/^the timezone on "([^"]*)" should be "([^"]*)"$/) do |minion, timezone|
  if minion == 'sle-minion'
    target = $minion
  elsif minion == 'ceos-minion'
    target = $ceos_minion
  else
    raise 'Invalid target'
  end
  output, _code = target.run('date +%Z')
  result = output.strip
  result = 'CET' if result == 'CEST'
  raise unless result == timezone
end

Then(/^the keymap on "([^"]*)" should be "([^"]*)"$/) do |minion, keymap|
  if minion == 'sle-minion'
    target = $minion
  elsif minion == 'ceos-minion'
    target = $ceos_minion
  else
    raise 'Invalid target'
  end
  output, _code = target.run('cat /etc/vconsole.conf')
  raise unless output.strip == "KEYMAP=#{keymap}"
end

Then(/^the language on "([^"]*)" should be "([^"]*)"$/) do |minion, language|
  if minion == 'sle-minion'
    target = $minion
  elsif minion == 'ceos-minion'
    target = $ceos_minion
  else
    raise 'Invalid target'
  end
  output, _code = target.run("grep 'RC_LANG=' /etc/sysconfig/language")
  raise unless output.strip == "RC_LANG=\"#{language}\""
end

When(/^I refresh the pillar data$/) do
  $server.run("salt '#{$minion_ip}' saltutil.refresh_pillar")
end

Then(/^the pillar data for "([^"]*)" should be "([^"]*)" on "([^"]*)"$/) do |key, value, minion|
  if minion == 'sle-minion'
    target = $minion_ip
    cmd = 'salt'
    extra_cmd = ''
  elsif minion == 'ssh-minion'
    target = $ssh_minion_ip
    cmd = 'salt-ssh'
    extra_cmd = '-i --roster-file=/tmp/tmp_roster_tests -w -W'
    $server.run("printf '#{target}:\n  host: #{target}\n  user: root\n  passwd: linux' > /tmp/tmp_roster_tests")
  else
    raise 'Invalid target'
  end
  output, _code = $server.run("#{cmd} '#{target}' pillar.get '#{key}' #{extra_cmd}")
  puts output
  if value == ''
    raise unless output.split("\n").length == 1
  else
    raise unless output.split("\n")[1].strip == value
  end
end

Then(/^the pillar data for "([^"]*)" should be empty on "([^"]*)"$/) do |key, minion|
  step %(the pillar data for "#{key}" should be "" on "#{minion}")
end

Given(/^I try download "([^"]*)" from channel "([^"]*)"$/) do |rpm, channel|
  url = "#{Capybara.app_host}/rhn/manager/download/#{channel}/getPackage/#{rpm}"
  url = "#{url}?#{@token}" if @token
  puts url
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
  assert_equal(code.to_i, @download_error.io.status[0].to_i)
end

Then(/^the download should get no error$/) do
  assert_nil(@download_error)
end

# Verify content
Then(/^I should not see "(.*?)" as a Minion anywhere$/) do |minion|
  node = get_target(minion)
  step %(I should not see a "#{node.hostname}" text)
end

# Perform actions
When(/^I reject "(.*?)" from the Pending section$/) do |minion|
  node = get_target(minion)
  xpath_query = "//tr[td[contains(.,'#{node.hostname}')]]//button[@title = 'reject']"
  raise unless find(:xpath, xpath_query).click
end

When(/^I delete "(.*?)" from the Rejected section$/) do |minion|
  node = get_target(minion)
  xpath_query = "//tr[td[contains(.,'#{node.hostname}')]]//button[@title = 'delete']"
  raise unless find(:xpath, xpath_query).click
end

When(/^I see "(.*?)" fingerprint$/) do |minion|
  node = get_target(minion)
  output, _code = node.run('salt-call --local key.finger')
  fing = output.split("\n")[1].strip!
  raise unless page.has_content?(fing)
end

When(/^I accept "(.*?)" key$/) do |minion|
  node = get_target(minion)
  xpath_query = "//tr[td[contains(.,'#{node.hostname}')]]//button[@title = 'accept']"
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

When(/^I should see this hostname as text$/) do
  within('#spacewalk-content') do
    raise unless page.has_content?($minion.hostname)
  end
end

When(/^I refresh page until see "(.*?)" hostname as text$/) do |minion|
  node = get_target(minion)
  within('#spacewalk-content') do
    steps %(
     And I try to reload page until contains "#{node.hostname}" text
      )
  end
end

When(/^I should see a "(.*)" text in the content area$/) do |txt|
  within('#spacewalk-content') do
    raise unless page.has_content?(txt)
  end
end

# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

require 'timeout'

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

Then(/^"([^"]*)" is not installed$/) do |package|
  uninstalled = false
  output = ''
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        output, code = $minion.run("rpm -q #{package}", false)
        if code.nonzero?
          uninstalled = true
          break
        end
        sleep 1
      end
    end
  end
  raise "exec rpm removal failed (Code #{$CHILD_STATUS}): #{$ERROR_INFO}: #{output}" unless uninstalled
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

And(/^I wait until salt\-key "(.*?)" is deleted$/) do |key|
  cmd = "salt-key -L | grep '#{key}' "
  $server.run_until_fail(cmd)
end

# salt ssh steps
SALT_PACKAGES = 'salt salt-minion'.freeze
Given(/^no Salt packages are installed on "(.*?)"$/) do |host|
  target = get_target(host)
  if ['sle-minion', 'ssh-minion', 'sle-client', 'sle-migrated-minion'].include?(host)
    target.run("test -e /usr/bin/zypper && zypper --non-interactive remove -y #{SALT_PACKAGES}", false)
  elsif ['ceos-minion'].include?(host)
    target.run("test -e /usr/bin/yum && yum -y remove #{SALT_PACKAGES}", false)
  end
end

Then(/^I enter remote ssh-minion hostname as "(.*?)"$/) do |hostname|
  step %(I enter "#{ENV['SSHMINION']}" as "#{hostname}")
end

# minion bootstrap steps
And(/^I enter the hostname of "([^"]*)" as hostname$/) do |minion|
  node = get_target(minion)
  step %(I enter "#{node.full_hostname}" as "hostname")
end

Then(/^I run spacecmd listevents for "([^"]*)"$/) do |host|
  node = get_target(host)
  $server.run('spacecmd -u admin -p admin clear_caches')
  $server.run("spacecmd -u admin -p admin system_listevents #{node.full_hostname}")
end

And(/^I cleanup minion: "([^"]*)"$/) do |target|
  if target == 'sle-minion'
    $minion.run('rcsalt-minion stop')
    $minion.run('rm -Rf /var/cache/salt/minion')
  elsif target == 'ceos-minion'
    $ceos_minion.run('systemctl stop salt-minion')
    $ceos_minion.run('rm -Rf /var/cache/salt/minion')
  end
end
