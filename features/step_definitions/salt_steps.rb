# Copyright 2015-2017 SUSE LLC
require 'timeout'

Given(/^the Salt Minion is configured$/) do
  # cleanup the key in case the image was reused
  # to run the test twice
  step %(I delete this minion key in the Salt master)
  step %(I stop salt-minion)
  step %(I stop salt-master)
  key = '/etc/salt/pki/minion/minion_master.pub'
  if file_exists?($minion, key)
    file_delete($minion, key)
    puts "Key #{key} has been removed on minion"
  end
  cmd = " echo  \'master : #{$server_ip}\' > /etc/salt/minion.d/susemanager.conf"
  $minion.run(cmd, false)
  step %(I start salt-master)
  step %(I start salt-minion)
end

Given(/^that the master can reach this client$/) do
  begin
    start = Time.now
    # 300 is the default 1st keepalive interval for the minion
    # where it realizes the connection is stuck
    Timeout.timeout(DEFAULT_TIMEOUT + 300) do
      # only try 3 times
      3.times do
        @output = sshcmd("salt #{$minion_hostname} test.ping", ignore_err: true)
        if @output[:stdout].include?($minion_hostname) &&
           @output[:stdout].include?('True')
          finished = Time.now
          puts "Took #{finished.to_i - start.to_i} seconds to contact the minion"
          break
        end
        sleep(1)
      end
    end
  rescue Timeout::Error
      fail "Master can not communicate with the minion: #{@output[:stdout]}"
  end
end

Given(/^I am on the Systems overview page of this minion$/) do
  steps %(
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow this minion link
    )
end

When(/^I follow this minion link$/) do
  step %(I follow "#{$minion_hostname}")
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

When(/^I stop salt-minion$/) do
  $minion.run("systemctl stop salt-minion", false)
end

When(/^I start salt-minion$/) do
  $minion.run("systemctl restart salt-minion", false)
end

When(/^I restart salt-minion$/) do
  $minion.run("systemctl restart salt-minion", false)
end

Then(/^the Salt Minion should be running$/) do
  i = 0
  MAX_ITER = 40
  loop do
    _out, code = $minion.run("systemctl status salt-minion", false)
    break if code.zero?
    sleep 5
    puts "sleeping 5 secs, minion not active."
    i += 1
    raise "TIMEOUT; something wrong with minion status" if i == MAX_ITER
  end
end

When(/^I list unaccepted keys at Salt Master$/) do
  $output, _code = $server.run("salt-key --list unaccepted", false)
  $output.strip
end

When(/^I list accepted keys at Salt Master$/) do
  $output, _code = $server.run("salt-key --list accepted", false)
  $output.strip
end

When(/^I list rejected keys at Salt Master$/) do
  $output, _code = $server.run("salt-key --list rejected", false)
  $output.strip
end

Then(/^the list of the keys should contain this client's hostname$/) do
  sleep 30
  # FIXME: find better way then to wait 30 seconds
  $output, _code = $server.run("salt-key --list all", false)
  assert_match($minion_hostname, $output, "minion #{$minion_hostname} is not listed on salt-master #{$output}")
end

Given(/^this minion key is unaccepted$/) do
  step "I list unaccepted keys at Salt Master"
  unless $output.include? $minion_hostname
    steps %(
      Then I delete this minion key in the Salt master
      And I restart salt-minion
      And we wait till Salt master sees this minion as unaccepted
        )
     end
end

When(/^we wait till Salt master sees this minion as unaccepted$/) do
  steps %(
    When I list unaccepted keys at Salt Master
    Then the list of the keys should contain this client's hostname
    )
end

Given(/^this minion key is accepted$/) do
  step "I list accepted keys at Salt Master"
  unless $output.include? $minion_hostname
    steps %(
      Then I accept this minion key in the Salt master
      And we wait till Salt master sees this minion as accepted
        )
  end
end

When(/^we wait till Salt master sees this minion as accepted$/) do
  steps %(
    When I list accepted keys at Salt Master
    Then the list of the keys should contain this client's hostname
    )
end

Given(/^this minion key is rejected$/) do
  step "I list rejected keys at Salt Master"
  unless $output.include? $minion_hostname
    steps %(
      Then I reject this minion key in the Salt master
      And we wait till Salt master sees this minion as rejected
        )
  end
end

When(/^we wait till Salt master sees this minion as rejected$/) do
  steps %(
    When I list rejected keys at Salt Master
    Then the list of the keys should contain this client's hostname
    )
end

When(/^I delete this minion key in the Salt master$/) do
  $output, _code = $server.run("salt-key -y -d #{$minion_hostname}", false)
end

When(/^I accept this minion key in the Salt master$/) do
  $server.run("salt-key -y --accept=#{$minion_hostname}")
end

When(/^I reject this minion key in the Salt master$/) do
  $server.run("salt-key -y --reject=#{$minion_hostname}")
end

When(/^I delete all keys in the Salt master$/) do
  $server.run("salt-key -y -D")
end

When(/^I accept all Salt unaccepted keys$/) do
  $server.run("salt-key -y -A")
end

When(/^I get OS information of the Minion from the Master$/) do
  $output, _code = $server.run("salt #{$minion_fullhostname} grains.get osfullname")
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

Then(/^this minion should have a Base channel set$/) do
  step %(I should not see a "This system has no Base Software Channel. You can select a Base Channel from the list below." text)
end

And(/^this minion is not registered in Spacewalk$/) do
  @rpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
  @rpc.login('admin', 'admin')
  sid = @rpc.listSystems.select { |s| s['name'] == $minion_fullhostname }.map { |s| s['id'] }.first
  @rpc.deleteSystem(sid) if sid
  refute_includes(@rpc.listSystems.map { |s| s['id'] }, $minion_fullhostname)
end

Given(/^that this minion is registered in Spacewalk$/) do
  @rpc = XMLRPCSystemTest.new(ENV['TESTHOST'])
  @rpc.login('admin', 'admin')
  assert_includes(@rpc.listSystems.map { |s| s['name'] }, $minion_fullhostname)
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

And(/^I follow the sle minion$/) do
 step %(I follow "#{$minion_fullhostname}")
end

And(/^I create picked-up test file on sle minion$/) do
  $minion.run("touch /tmp/PICKED-UP.test")
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
  sleep(30)
  find('button#run').click
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
  $server.run("zypper --non-interactive install -y #{package}-formula", false)
end

When(/^I check the "([^"]*)" formula$/) do |formula|
  # Complicated code because the checkbox is not a <input type=checkbox> but an <i>
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']"
  if all(:xpath, xpath_query).any?
    fail unless find(:xpath, xpath_query).click
  else
    xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']"
    assert all(:xpath, xpath_query).any?, "Checkbox could not be found"
  end
end

Then(/^the "([^"]*)" formula should be checked$/) do |formula|
  # Complicated code because the checkbox is not a <input type=checkbox> but an <i>
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-square-o']"
  if all(:xpath, xpath_query).any?
    fail "Checkbox is not checked"
  end
  xpath_query = "//a[@id = '#{formula}']/i[@class = 'fa fa-lg fa-check-square-o']"
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
  fail unless output.strip == timezone
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
  output, _code = target.run("grep 'LANG=' /etc/locale.conf")
  fail unless output.strip == "LANG=#{language}"
end

When(/^I refresh the pillar data$/) do
  $server.run("salt '*' saltutil.refresh_pillar")
end

Then(/^the pillar data for "([^"]*)" should be "([^"]*)"$/) do |key, value|
  output, _code = $server.run("salt '#{$minion_ip}' pillar.get '#{key}'")
  fail unless output.split("\n")[1].strip == value
end
