# Copyright 2015-2017 SUSE LLC
require 'timeout'
require 'open-uri'
require 'tempfile'

Given(/^salt-minion is configured on "(.*?)"$/) do |minion|
  if minion == "sle-minion"
    target = $minion
  elsif minion == "ceos-minion"
    target = $ceos_minion
  else
    raise "no valid name of minion given! "
  end
  # cleanup the key in case the image was reused
  # to run the test twice
  step %(I delete "#{minion}" key in the Salt master)
  step %(I stop salt-minion on "#{minion}")
  step %(I stop salt-master)
  key = '/etc/salt/pki/minion/minion_master.pub'
  if file_exists?(target, key)
    file_delete(target, key)
    puts "Key #{key} has been removed on minion"
  end
  cmd = " echo  \'master : #{$server_ip}\' > /etc/salt/minion.d/susemanager.conf"
  target.run(cmd, false)
  step %(I start salt-master)
  step %(I start salt-minion on "#{minion}")
end

Given(/^the master can reach "(.*?)"$/) do |minion|
  if minion == "sle-minion"
    target_hostname = $minion_hostname
  elsif minion == "ceos-minion"
    target_hostname = $ceos_minion_hostname
  else
    raise "no valid name of minion given! "
  end
  begin
    start = Time.now
    # 300 is the default 1st keepalive interval for the minion
    # where it realizes the connection is stuck
    KEEPALIVE_TIMEOUT = 300
    Timeout.timeout(KEEPALIVE_TIMEOUT) do
      # only try 3 times
      3.times do
        @output = sshcmd("salt #{target_hostname} test.ping", ignore_err: true)
        if @output[:stdout].include?(target_hostname) &&
           @output[:stdout].include?('True')
          finished = Time.now
          puts "Took #{finished.to_i - start.to_i} seconds to contact the minion"
          break
        end
        sleep(1)
      end
    end
  rescue Timeout::Error
      fail "Master can not communicate with #{minion}: #{@output[:stdout]}"
  end
end

Given(/^I am on the Systems overview page of "(.*?)"$/) do |minion|
  steps %(
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow "#{minion}" link
    )
end

When(/^I follow "(.*?)" link$/) do |minion|
  if minion == "sle-minion"
    target_hostname = $minion_hostname
  elsif minion == "ceos-minion"
    target_hostname = $ceos_minion_hostname
  elsif minion == "ssh-minion"
    target_hostname = $ssh_minion_hostname
  elsif minion == "sle-client"
    target_hostname = $client_hostname
  elsif minion == "sle-migrated-minion"
    target_hostname = $client_hostname
  else
    raise "no valid name of minion given! "
  end
  step %(I follow "#{target_hostname}")
end

When(/^I get the contents of the remote file "(.*?)"$/) do |filename|
  $output, _code = $server.run("cat #{filename}")
end

When(/^I stop salt-master$/) do
  $server.run("systemctl stop salt-master", false)
end

When(/^I start salt-master$/) do
  $server.run("systemctl start salt-master", false)
end

When(/^I stop salt-minion on "(.*?)"$/) do |minion|
  if minion == "sle-minion"
    target = $minion
  elsif minion == "ceos-minion"
    target = $ceos_minion
  else
    raise "no valid name of minion given! "
  end
  target.run("systemctl stop salt-minion", false)
end

When(/^I start salt-minion on "(.*?)"$/) do |minion|
  if minion == "sle-minion"
    target = $minion
  elsif minion == "ceos-minion"
    target = $ceos_minion
  else
    raise "no valid name of minion given! "
  end
  target.run("systemctl restart salt-minion", false)
end

When(/^I restart salt-minion on "(.*?)"$/) do |minion|
  if minion == "sle-minion"
    target = $minion
  elsif minion == "ceos-minion"
    target = $ceos_minion
  else
    raise "no valid name of minion given! "
  end
  target.run("systemctl restart salt-minion", false)
end

Then(/^salt-minion should be running on "(.*?)"$/) do |minion|
  if minion == "sle-minion"
    target = $minion
  elsif minion == "ceos-minion"
    target = $ceos_minion
  else
    raise "no valid name of minion given! "
  end
  i = 0
  MAX_ITER = 40
  loop do
    _out, code = target.run("systemctl status salt-minion", false)
    break if code.zero?
    sleep 5
    puts "sleeping 5 secs, minion not active."
    i += 1
    raise "TIMEOUT; something wrong with minion status" if i == MAX_ITER
  end
end

When(/^I list "(.*?)" keys at Salt Master$/) do |key_type|
  $output, _code = $server.run("salt-key --list #{key_type}", false)
  $output.strip
end

Then(/^the list of the "(.*?)" keys should contain "(.*?)" hostname$/) do |key_type, minion|
  if minion == "sle-minion"
    target_fullhostname = $minion_fullhostname
  elsif minion == "ceos-minion"
    target_fullhostname = $ceos_minion_fullhostname
  else
    raise "no valid name of minion given! "
  end
  sleep 30
  # FIXME: find better way then to wait 30 seconds
  $output, _code = $server.run("salt-key --list #{key_type}", false)
  assert_match(target_fullhostname, $output, "minion #{target_fullhostname} is not listed as #{key_type} key on salt-master #{$output}")
end

When(/^we wait till Salt master sees "(.*?)" as "(.*?)"$/) do |minion, key_type|
  steps %(
    When I list "#{key_type}" keys at Salt Master
    Then the list of the "#{key_type}" keys should contain "#{minion}" hostname
    )
end

Given(/^"(.*?)" key is "(.*?)"$/) do |minion, key_type|
  if minion == "sle-minion"
    target_hostname = $minion_hostname
  elsif minion == "ceos-minion"
    target_hostname = $ceos_minion_hostname
  else
    raise "no valid name of minion given! "
  end
  steps %(
    Then I restart salt-minion on \"#{minion}\"
    And I list \"all\" keys at Salt Master
  )
  unless $output.include?(target_hostname)
    steps %(
      Then I accept "#{minion}" key in the Salt master
      And we wait till Salt master sees "#{minion}" as "#{key_type}"
        )
  end
end

Then(/^I wait until onboarding is completed for "([^"]*)"$/) do |system|
  steps %(
    When I navigate to "rhn/systems/Overview.do" page
    And I follow this "#{system}" link
    When I follow "Events"
    And I follow "History"
    Then I try to reload page until contains "Package List Refresh scheduled by (none)" text
    And I follow first "Package List Refresh scheduled by (none)"
    And I wait until i see "This action's status is: Completed." text, refreshing the page
    And I wait for "5" seconds
  )
end

Then(/^I wait until OpenSCAP scan is completed for "([^"]*)"$/) do |system|
  steps %(
    When I navigate to "rhn/systems/Overview.do" page
    And I follow this "#{system}" link
    When I follow "Events"
    And I follow "History"
    Then I try to reload page until contains "OpenSCAP xccdf scanning" text
    And I follow first "OpenSCAP xccdf scanning"
    And I wait until i see "This action's status is: Completed." text, refreshing the page
    And I wait for "5" seconds
  )
end

When(/^I delete "(.*?)" key in the Salt master$/) do |minion|
  if minion == "sle-minion"
    target_hostname = $minion_hostname
  elsif minion == "ceos-minion"
    target_hostname = $ceos_minion_hostname
  else
    raise "no valid name of minion given! "
  end
  $output, _code = $server.run("salt-key -y -d #{target_hostname}", false)
end

When(/^I accept "(.*?)" key in the Salt master$/) do |minion|
  if minion == "sle-minion"
    target_hostname = $minion_hostname
  elsif minion == "ceos-minion"
    target_hostname = $ceos_minion_hostname
  else
    raise "no valid name of minion given! "
  end
  $server.run("salt-key -y --accept=#{target_hostname}")
end

When(/^I reject "(.*?)" key in the Salt master$/) do |minion|
  if minion == "sle-minion"
    target_hostname = $minion_hostname
  elsif minion == "ceos-minion"
    target_hostname = $ceos_minion_hostname
  else
    raise "no valid name of minion given! "
  end
  $server.run("salt-key -y --reject=#{target_hostname}")
end

When(/^I delete all keys in the Salt master$/) do
  $server.run("salt-key -y -D")
end

When(/^I accept all Salt unaccepted keys$/) do
  $server.run("salt-key -y -A")
end

When(/^I get OS information of "(.*?)" from the Master$/) do |minion|
  if minion == "sle-minion"
    target_fullhostname = $minion_fullhostname
  elsif minion == "ceos-minion"
    target_fullhostname = $ceos_minion_fullhostname
  elsif minion == "ssh-minion"
    target_fullhostname = $ssh_minion_fullhostname
  elsif minion == "sle-client"
    target_fullhostname = $client_fullhostname
  elsif minion == "sle-migrated-minion"
    target_fullhostname = $client_fullhostname
  else
    raise "no valid name of minion given! "
  end
  $output, _code = $server.run("salt #{target_fullhostname} grains.get osfullname")
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

And(/^"(.*?)" is not registered in Spacewalk$/) do |minion|
  if minion == "sle-minion"
    target_fullhostname = $minion_fullhostname
  elsif minion == "ceos-minion"
    target_fullhostname = $ceos_minion_fullhostname
  elsif minion == "ssh-minion"
    target_fullhostname = $ssh_minion_fullhostname
  elsif minion == "sle-client"
    target_fullhostname = $client_fullhostname
  elsif minion == "sle-migrated-minion"
    target_fullhostname = $client_fullhostname
  else
    raise "no valid name of minion given! "
  end
  @rpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
  @rpc.login('admin', 'admin')
  sid = @rpc.listSystems.select { |s| s['name'] == target_fullhostname }.map { |s| s['id'] }.first
  @rpc.deleteSystem(sid) if sid
  refute_includes(@rpc.listSystems.map { |s| s['id'] }, target_fullhostname)
end

Given(/^"(.*?)" is registered in Spacewalk$/) do |minion|
  if minion == "sle-minion"
    target_fullhostname = $minion_fullhostname
  elsif minion == "ceos-minion"
    target_fullhostname = $ceos_minion_fullhostname
  elsif minion == "ssh-minion"
    target_fullhostname = $ssh_minion_fullhostname
  elsif minion == "sle-client"
    target_fullhostname = $client_fullhostname
  elsif minion == "sle-migrated-minion"
    target_fullhostname = $client_fullhostname
  else
    raise "no valid name of minion given! "
  end
  @rpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
  @rpc.login('admin', 'admin')
  assert_includes(@rpc.listSystems.map { |s| s['name'] }, target_fullhostname)
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
      while [ ! -f /tmp/PICKED-UP-#{$$}.test ]
      do
        sleep 1
      done
      rm /tmp/PICKED-UP-#{$$}.test
      """)
end

Then(/^I should see "(.*?)" hostname$/) do |minion|
 if minion == "sle-minion"
    step %(I should see a "#{$minion_fullhostname}" text)
 elsif minion == "ceos-minion"
    step %(I should see a "#{$ceos_minion_fullhostname}" text)
 else
    raise "no valid name of minion given! "
  end
end

# user salt steps
Given(/^I am authorized as an example user with no roles$/) do
  @rpc = XMLRPCUserTest.new(ENV["TESTHOST"])
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
      fail "Run button not found"
  end
end

When(/^I should see my hostname$/) do
  fail unless page.has_content?($minion_hostname)
end

When(/^I should not see my hostname$/) do
  fail if page.has_content?($minion_hostname)
end

When(/^I expand the results$/) do
   find("div[id='#{$minion_fullhostname}']").click
end

When(/^I expand the results for "(.*)"$/) do |host|
 find("div[id='#{$ceos_minion_fullhostname}']").click if host == "ceos-minion"
 find("div[id='#{$ssh_minion_fullhostname}']").click if host == "ssh-minion"
 find("div[id='#{$minion_fullhostname}']").click if host == "sle-minion"
end

Then(/^I enter command "([^"]*)"$/) do |arg1|
  fill_in "command", with: arg1
end

Then(/^I should see "([^"]*)" in the command output$/) do |text|
  within("pre[id='#{$minion_fullhostname}-results']") do
    fail unless page.has_content?(text)
  end
end

Then(/^I manually install the "([^"]*)" package in the minion$/) do |package|
  if file_exists?($minion, "/usr/bin/zypper")
    cmd = "zypper --non-interactive install -y #{package}"
  elsif file_exists?($minion, "/usr/bin/yum")
    cmd = "yum -y install #{package}"
  else
    fail "not found: zypper or yum"
  end
  $minion.run(cmd, false)
end

Then(/^I manually remove the "([^"]*)" package in the minion$/) do |package|
  if file_exists?($minion, "/usr/bin/zypper")
    cmd = "zypper --non-interactive remove -y #{package}"
  elsif file_exists?($minion, "/usr/bin/yum")
    cmd = "yum -y remove #{package}"
  else
    fail "not found: zypper or yum"
  end
  $minion.run(cmd, false)
end

Then(/^I click on the css "(.*)" until page does not contain "([^"]*)" text$/) do |css, arg1|
  not_found = false
  begin
    Timeout.timeout(30) do
      loop do
        unless page.has_content?(debrand_string(arg1))
          not_found = true
          break
        end
        find(css).click
      end
    end
  rescue Timeout::Error
    raise "'#{arg1}' cannot be found after several tries"
  end
  fail unless not_found
end
# states catalog
When(/^I enter the salt state$/) do |multiline|
  within(:xpath, "//section") do
    x = find('textarea[name="content"]')
    x.set(multiline) # find("#{arg1}") #.set(lines)
  end
end

When(/^I click on the css "(.*)"$/) do |css|
  find(css).click
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
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if action == "check"
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if action == "uncheck"
  if all(:xpath, xpath_query).any?
    fail unless find(:xpath, xpath_query).click
  else
    xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if action == "check"
    xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if action == "uncheck"
    assert all(:xpath, xpath_query).any?, "Checkbox could not be found"
  end
end

Then(/^the "([^"]*)" formula should be ([^"]*)$/) do |formula, action|
  # Complicated code because the checkbox is not a <input type=checkbox> but an <i>
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if action == "checked"
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if action == "unchecked"
  if all(:xpath, xpath_query).any?
    fail "Checkbox is not #{action}"
  end
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']" if action == "checked"
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']" if action == "unchecked"
  assert all(:xpath, xpath_query).any?, "Checkbox could not be found"
end

When(/^I select "([^"]*)" in (.*) field$/) do |value, box|
  boxid = case box
    when "timezone name"
      "timezone\$name"
    when "language"
      "keyboard_and_language\$language"
    when "keyboard layout"
      "keyboard_and_language\$keyboard_layout"
  end
  select(value, :from => boxid)
end

Then(/^the timezone on "([^"]*)" should be "([^"]*)"$/) do |minion, timezone|
  if minion == "sle-minion"
    target = $minion
  elsif minion == "ceos-minion"
    target = $ceos_minion
  else
    fail "Invalid target"
  end
  output, _code = target.run("date +%Z")
  result = output.strip
  result = "CET" if result == "CEST"
  fail unless result == timezone
end

Then(/^the keymap on "([^"]*)" should be "([^"]*)"$/) do |minion, keymap|
  if minion == "sle-minion"
    target = $minion
  elsif minion == "ceos-minion"
    target = $ceos_minion
  else
    fail "Invalid target"
  end
  output, _code = target.run("cat /etc/vconsole.conf")
  fail unless output.strip == "KEYMAP=#{keymap}"
end

Then(/^the language on "([^"]*)" should be "([^"]*)"$/) do |minion, language|
  if minion == "sle-minion"
    target = $minion
  elsif minion == "ceos-minion"
    target = $ceos_minion
  else
    fail "Invalid target"
  end
  output, _code = target.run("grep 'RC_LANG=' /etc/sysconfig/language")
  fail unless output.strip == "RC_LANG=\"#{language}\""
end

When(/^I refresh the pillar data$/) do
  $server.run("salt '#{$minion_ip}' saltutil.refresh_pillar")
end

Then(/^the pillar data for "([^"]*)" should be "([^"]*)" on "([^"]*)"$/) do |key, value, minion|
  if minion == "sle-minion"
    target = $minion_ip
    cmd = "salt"
    extra_cmd = ""
  elsif minion == "ssh-minion"
    target = $ssh_minion_ip
    cmd = "salt-ssh"
    extra_cmd = "-i --roster-file=/tmp/tmp_roster_tests -w -W"
    $server.run("printf '#{target}:\n  host: #{target}\n  user: root\n  passwd: linux' > /tmp/tmp_roster_tests")
  else
    fail "Invalid target"
  end
  output, _code = $server.run("#{cmd} '#{target}' pillar.get '#{key}' #{extra_cmd}")
  puts output
  if value == ""
    fail unless output.split("\n").length == 1
  else
    fail unless output.split("\n")[1].strip == value
  end
end

Then(/^the pillar data for "([^"]*)" should be empty on "([^"]*)"$/) do |key, minion|
  step %(the pillar data for "#{key}" should be "" on "#{minion}")
end

Given(/^I try download "([^"]*)" from channel "([^"]*)"$/) do |rpm, channel|
  url = "#{Capybara.app_host}/rhn/manager/download/#{channel}/getPackage/#{rpm}"
  if @token
    url = "#{url}?#{@token}"
  end
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
  if minion == "sle-minion"
    target_hostname = $minion_hostname
  elsif minion == "ceos-minion"
    target_hostname = $ceos_minion_hostname
  else
    raise "no valid name of minion given! "
  end
  step %(I should not see a "#{target_hostname}" text)
end

# Perform actions
When(/^I reject "(.*?)" from the Pending section$/) do |minion|
  if minion == "sle-minion"
    target_hostname = $minion_hostname
  elsif minion == "ceos-minion"
    target_hostname = $ceos_minion_hostname
  else
    raise "no valid name of minion given! "
  end
  xpath_query = "//tr[td[contains(.,'#{target_hostname}')]]//button[@title = 'reject']"
  if all(:xpath, xpath_query).any?
    fail unless find(:xpath, xpath_query).click
  else
    raise "reject key button not found for #{target_hostname}"
  end
end

When(/^I delete "(.*?)" from the Rejected section$/) do |minion|
  if minion == "sle-minion"
    target_hostname = $minion_hostname
  elsif minion == "ceos-minion"
    target_hostname = $ceos_minion_hostname
  else
    raise "no valid name of minion given! "
  end
  xpath_query = "//tr[td[contains(.,'#{target_hostname}')]]//button[@title = 'delete']"
  if all(:xpath, xpath_query).any?
    fail unless find(:xpath, xpath_query).click
  else
    raise "delete key button not found for #{target_hostname}"
  end
end

When(/^I see "(.*?)" fingerprint$/) do |minion|
  if minion == "sle-minion"
    target = $minion
  elsif minion == "ceos-minion"
    target = $ceos_minion
  else
    raise "no valid name of minion given! "
  end
  output, _code = target.run("salt-call --local key.finger")
  fing = output.split("\n")[1].strip!

  fail unless page.has_content?(fing)
end

When(/^I accept "(.*?)" key$/) do |minion|
  if minion == "sle-minion"
    target_hostname = $minion_hostname
  elsif minion == "ceos-minion"
    target_hostname = $ceos_minion_hostname
  else
    raise "no valid name of minion given! "
  end
  xpath_query = "//tr[td[contains(.,'#{target_hostname}')]]//button[@title = 'accept']"
  if all(:xpath, xpath_query).any?
    fail unless find(:xpath, xpath_query).click
  else
    raise "accept key button not found for #{target_hostname}"
  end
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
    fail unless page.has_content?($minion_hostname)
  end
end

When(/^I refresh page until see "(.*?)" hostname as text$/) do |minion|
  if minion == "sle-minion"
    target_hostname = $minion_hostname
  elsif minion == "ceos-minion"
    target_hostname = $ceos_minion_hostname
  else
    raise "no valid name of minion given! "
  end
  within('#spacewalk-content') do
    steps %(
     And I try to reload page until contains "#{target_hostname}" text
      )
  end
end

When(/^I should see a "(.*)" text in the content area$/) do |txt|
  within('#spacewalk-content') do
    fail unless page.has_content?(txt)
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
  output = ""
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
  raise "exec rpm removal failed (Code #{$?}): #{$!}: #{output}" unless uninstalled
end

Then(/^I wait for "([^"]*)" to be installed on this "([^"]*)"$/) do |package, host|
  node = get_target(host)
  installed = false
  output = ""
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        output, code = node.run("rpm -q #{package}", false)
        if code.zero?
          installed = true
          break
        end
        sleep 1
      end
    end
  rescue Timeout::Error
    raise "exec rpm installation failed: timeout"
  end
  raise "exec rpm installation failed (Code #{$?}): #{$!}: #{output}" unless installed
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
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        cmd = "salt-key -L | grep '#{key}' "
        _output, code = $server.run(cmd, false)
        if code.nonzero?
          puts 'key has been deleted'
          puts $server.run("salt-key -L", false)
          break
        end
        sleep 5
      end
    end
  rescue Timeout::Error
    raise "FATAL: salt-key #{key} was not deleted"
  end
end
# salt ssh steps

SALT_PACKAGES = "salt salt-minion".freeze

Given(/^no Salt packages are installed on remote "(.*)"$/) do |host|
  if host == "ssh-minion"
    $ssh_minion.run("test -e /usr/bin/zypper && zypper --non-interactive remove -y #{SALT_PACKAGES}", false)
  end
  if host == "centos"
    $ceos_minion.run("test -e /usr/bin/yum && yum -y remove #{SALT_PACKAGES}", false)
  end
end

Then(/^I enter remote ssh-minion hostname as "(.*?)"$/) do |hostname|
  step %(I enter "#{ENV['SSHMINION']}" as "#{hostname}")
end

Then(/^I should see remote ssh-minion hostname as link$/) do
  step %(I should see a "#{ENV['SSHMINION']}" link)
end

Then(/^I should see centos ssh-minion hostname as link$/) do
  step %(I should see a "#{ENV['CENTOSMINION']}" link)
end

Then(/^I follow centos ssh-minion hostname$/) do
  step %(I follow "#{ENV['CENTOSMINION']}")
end

Then(/^I follow remote ssh-minion hostname$/) do
  step %(I follow "#{ENV['SSHMINION']}")
end

# minion bootstrap steps
And(/^I enter the hostname of "([^"]*)" as hostname$/) do |minion|
  case minion
  when "sle-minion"
    step %(I enter "#{$minion_fullhostname}" as "hostname")
  when "ceos-minion"
    step %(I enter "#{$ceos_minion_fullhostname}" as "hostname")
  when "sle-migrated-minion"
    step %(I enter "#{$client_fullhostname}" as "hostname")
  else
    raise "No valid target."
  end
end

Then(/^I run spacecmd listevents for sle-minion$/) do
  $server.run("spacecmd -u admin -p admin clear_caches")
  $server.run("spacecmd -u admin -p admin system_listevents #{$minion_fullhostname}")
end

And(/^I cleanup minion: "([^"]*)"$/) do |target|
  if target == "sle-minion"
    $minion.run("systemctl stop salt-minion")
    $minion.run("rm -Rf /var/cache/salt/minion")
  elsif target == "ceos-minion"
    $ceos_minion.run("systemctl stop salt-minion")
    $ceos_minion.run("rm -Rf /var/cache/salt/minion")
   end
end
