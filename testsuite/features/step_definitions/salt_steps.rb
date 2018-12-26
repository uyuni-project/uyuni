# Copyright 2015-2019 SUSE LLC
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
  node.run('systemctl stop salt-minion', false) if ['ceos-minion', 'ceos-ssh-minion', 'ubuntu-minion', 'ubuntu-ssh-minion'].include?(minion)
end

When(/^I start salt-minion on "(.*?)"$/) do |minion|
  node = get_target(minion)
  node.run('rcsalt-minion restart', false) if minion == 'sle-minion'
  node.run('systemctl restart salt-minion', false) if ['ceos-minion', 'ceos-ssh-minion', 'ubuntu-minion', 'ubuntu-ssh-minion'].include?(minion)
end

When(/^I restart salt-minion on "(.*?)"$/) do |minion|
  node = get_target(minion)
  node.run('rcsalt-minion restart', false) if minion == 'sle-minion'
  node.run('systemctl restart salt-minion', false) if ['ceos-minion', 'ceos-ssh-minion', 'ubuntu-minion', 'ubuntu-ssh-minion'].include?(minion)
end

When(/^I wait at most (\d+) seconds until Salt master sees "([^"]*)" as "([^"]*)"$/) do |key_timeout, minion, key_type|
  cmd = "salt-key --list #{key_type}"
  begin
    Timeout.timeout(key_timeout.to_i) do
      loop do
        system_name = get_system_name(minion)
        unless system_name.empty?
          output, return_code = $server.run(cmd, false)
          break if return_code.zero? && output.include?(system_name)
        end
        sleep 1
      end
    end
  rescue Timeout::Error
    raise "Minion \"#{minion}\" is not listed among #{key_type} keys on Salt master after #{key_timeout} seconds"
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

Then(/^the PXE boot minion should have been reformatted$/) do
  system_name = get_system_name('pxeboot-minion')
  output, _code = $server.run("salt #{system_name} file.file_exists /intact")
  raise 'Minion is intact' unless output.include? 'False'
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
  boxids = { 'timezone name'            => 'timezone#name',
             'language'                 => 'keyboard_and_language#language',
             'keyboard layout'          => 'keyboard_and_language#keyboard_layout',
             'disk label'               => 'partitioning#0#disklabel',
             'first filesystem format'  => 'partitioning#0#partitions#0#format',
             'first partition flags'    => 'partitioning#0#partitions#0#flags',
             'second filesystem format' => 'partitioning#0#partitions#1#format',
             'second partition flags'   => 'partitioning#0#partitions#1#flags' }
  select(value, from: boxids[box])
end

When(/^I enter the local IP address of "([^"]*)" in (.*) field$/) do |host, field|
  fieldids = { 'IP'                       => 'branch_network#ip',
               'domain name server'       => 'dhcpd#domain_name_servers#0',
               'network IP'               => 'dhcpd#subnets#0#$key',
               'dynamic IP range begin'   => 'dhcpd#subnets#0#range#0',
               'dynamic IP range end'     => 'dhcpd#subnets#0#range#1',
               'broadcast address'        => 'dhcpd#subnets#0#broadcast_address',
               'routers'                  => 'dhcpd#subnets#0#routers#0',
               'next server'              => 'dhcpd#subnets#0#next_server',
               'first reserved IP'        => 'dhcpd#hosts#0#fixed_address',
               'second reserved IP'       => 'dhcpd#hosts#1#fixed_address',
               'third reserved IP'        => 'dhcpd#hosts#2#fixed_address',
               'first A address'          => 'bind#available_zones#0#records#A#0#1',
               'second A address'         => 'bind#available_zones#0#records#A#1#1',
               'third A address'          => 'bind#available_zones#0#records#A#2#1',
               'fourth A address'         => 'bind#available_zones#0#records#A#3#1',
               'internal network address' => 'tftpd#listen_ip' }
  addresses = { 'network'     => '0',
                'client'      => '2',
                'minion'      => '3',
                'pxeboot'     => '4',
                'range begin' => '128',
                'range end'   => '253',
                'proxy'       => '254',
                'broadcast'   => '255' }
  net_prefix = $private_net.sub(%r{\.0+/24$}, ".")
  fill_in fieldids[field], with: net_prefix + addresses[host]
end

# rubocop:disable Metrics/BlockLength
When(/^I enter "([^"]*)" in (.*) field$/) do |value, field|
  fieldids = { 'NIC'                             => 'branch_network#nic',
               'domain name'                     => 'dhcpd#domain_name',
               'listen interfaces'               => 'dhcpd#listen_interfaces#0',
               'network mask'                    => 'dhcpd#subnets#0#netmask',
               'filename'                        => 'dhcpd#subnets#0#filename',
               'first reserved hostname'         => 'dhcpd#hosts#0#$key',
               'second reserved hostname'        => 'dhcpd#hosts#1#$key',
               'third reserved hostname'         => 'dhcpd#hosts#2#$key',
               'first option'                    => 'bind#config#options#0#0',
               'first value'                     => 'bind#config#options#0#1',
               'first configured zone name'      => 'bind#configured_zones#0#$key',
               'first available zone name'       => 'bind#available_zones#0#$key',
               'first file name'                 => 'bind#available_zones#0#file',
               'first name server'               => 'bind#available_zones#0#soa#ns',
               'first contact'                   => 'bind#available_zones#0#soa#contact',
               'first A name'                    => 'bind#available_zones#0#records#A#0#0',
               'second A name'                   => 'bind#available_zones#0#records#A#1#0',
               'third A name'                    => 'bind#available_zones#0#records#A#2#0',
               'fourth A name'                   => 'bind#available_zones#0#records#A#3#0',
               'first NS'                        => 'bind#available_zones#0#records#NS#@#0',
               'first CNAME alias'               => 'bind#available_zones#0#records#CNAME#0#0',
               'first CNAME name'                => 'bind#available_zones#0#records#CNAME#0#1',
               'second CNAME alias'              => 'bind#available_zones#0#records#CNAME#1#0',
               'second CNAME name'               => 'bind#available_zones#0#records#CNAME#1#1',
               'third CNAME alias'               => 'bind#available_zones#0#records#CNAME#2#0',
               'third CNAME name'                => 'bind#available_zones#0#records#CNAME#2#1',
               'second name server'              => 'bind#available_zones#1#soa#ns',
               'second contact'                  => 'bind#available_zones#1#soa#contact',
               'second NS'                       => 'bind#available_zones#1#records#NS#@#0',
               'second for zones'                => 'bind#available_zones#1#generate_reverse#for_zones#0',
               'third configured zone name'      => 'bind#configured_zones#2#$key',
               'third available zone name'       => 'bind#available_zones#2#$key',
               'third file name'                 => 'bind#available_zones#2#file',
               'third name server'               => 'bind#available_zones#2#soa#ns',
               'third contact'                   => 'bind#available_zones#2#soa#contact',
               'TFTP base directory'             => 'tftpd#root_dir',
               'branch id'                       => 'pxe#branch_id',
               'disk id'                         => 'partitioning#0#$key',
               'disk device'                     => 'partitioning#0#device',
               'first partition id'              => 'partitioning#0#partitions#0#$key',
               'first partition size'            => 'partitioning#0#partitions#0#size_MiB',
               'first mount point'               => 'partitioning#0#partitions#0#mountpoint',
               'first OS image'                  => 'partitioning#0#partitions#0#image',
               'second partition id'             => 'partitioning#0#partitions#1#$key',
               'second partition size'           => 'partitioning#0#partitions#1#size_MiB',
               'second mount point'              => 'partitioning#0#partitions#1#mountpoint',
               'second OS image'                 => 'partitioning#0#partitions#1#image' }
  fill_in fieldids[field], with: value
end
# rubocop:enable Metrics/BlockLength

When(/^I enter the hostname of "([^"]*)" in (.*) field$/) do |host, field|
  system_name = get_system_name(host)
  fieldids = { 'third CNAME name'   => 'bind#available_zones#0#records#CNAME#2#1',
               'third name server'  => 'bind#available_zones#2#soa#ns',
               'fifth A name'       => 'bind#available_zones#2#records#A#0#0',
               'third NS'           => 'bind#available_zones#2#records#NS#@#0' }
  fill_in fieldids[field], with: "#{system_name}."
end

When(/^I enter the IP address of "([^"]*)" in (.*) field$/) do |host, field|
  node = get_target(host)
  output, _code = node.run("ip address show dev eth0")
  ip = output.split("\n")[2].split[1].split('/')[0]
  fieldids = { 'fifth A address' => 'bind#available_zones#2#records#A#0#1' }
  fill_in fieldids[field], with: ip
end

When(/^I enter the MAC address of "([^"]*)" in (.*) field$/) do |host, field|
  if host == 'pxeboot-minion'
    mac = $pxeboot_mac
  else
    node = get_target(host)
    output, _code = node.run("ip link show dev eth1")
    mac = output.split("\n")[1].split[1]
  end
  fieldids = { 'first reserved MAC'  => 'dhcpd#hosts#0#hardware',
               'second reserved MAC' => 'dhcpd#hosts#1#hardware',
               'third reserved MAC'  => 'dhcpd#hosts#2#hardware' }
  fill_in fieldids[field], with: 'ethernet ' + mac
end

When(/^I enter the local zone name in (.*) field$/) do |field|
  fieldids = { 'second configured zone name' => 'bind#configured_zones#1#$key',
               'second available zone name'  => 'bind#available_zones#1#$key' }
  a = $private_net.split('.')
  reverse_net = a[2] + '.' + a[1] + '.' + a[0] + '.in-addr.arpa'
  STDOUT.puts "#{$private_net} => #{reverse_net}"
  fill_in fieldids[field], with: reverse_net
end

When(/^I enter the local file name in (.*) field$/) do |field|
  fieldids = { 'second file name' => 'bind#available_zones#1#file' }
  a = $private_net.split('.')
  reverse_filename = 'master/db.' + a[2] + '.' + a[1] + '.' + a[0] + '.in-addr.arpa'
  STDOUT.puts "#{$private_net} => #{reverse_filename}"
  fill_in fieldids[field], with: reverse_filename
end

When(/^I enter the local network in (.*) field$/) do |field|
  fieldids = { 'second generate reverse network' => 'bind#available_zones#1#generate_reverse#net' }
  fill_in fieldids[field], with: $private_net
end

When(/^I press "Add Item" in (.*) section$/) do |section|
  sectionids = { 'host reservations' => 'dhcpd#hosts#add_item',
                 'config options'    => 'bind#config#options#add_item',
                 'configured zones'  => 'bind#configured_zones#add_item',
                 'available zones'   => 'bind#available_zones#add_item',
                 'first A'           => 'bind#available_zones#0#records#A#add_item',
                 'first NS'          => 'bind#available_zones#0#records#NS#@#add_item',
                 'first CNAME'       => 'bind#available_zones#0#records#CNAME#add_item',
                 'second NS'         => 'bind#available_zones#1#records#NS#@#add_item',
                 'second for zones'  => 'bind#available_zones#1#generate_reverse#for_zones#add_item',
                 'third A'           => 'bind#available_zones#2#records#A#add_item',
                 'third NS'          => 'bind#available_zones#2#records#NS#@#add_item',
                 'partitions'        => 'partitioning#0#partitions#add_item' }
  find(:xpath, "//button[@id='#{sectionids[section]}']").click
end

When(/^I press "Remove Item" in (.*) section$/) do |section|
  sectionids = { 'first CNAME'       => 'bind#available_zones#0#records#CNAME#0',
                 'second CNAME'      => 'bind#available_zones#0#records#CNAME#0',
                 'third CNAME'       => 'bind#available_zones#0#records#CNAME#0' }
  find(:xpath, "//div[@id='#{sectionids[section]}']/button").click
end

When(/^I check (.*) box$/) do |box|
  boxids = { 'include forwarders' => 'bind#config#include_forwarders' }
  check boxids[box]
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
  elsif ['ssh-minion', 'ceos-minion', 'ceos-ssh-minion', 'ubuntu-minion', 'ubuntu-ssh-minion'].include?(minion)
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
  elsif ['ubuntu-minion', 'ubuntu-ssh-minion'].include?(host)
    target.run("test -e /usr/bin/apt && apt -y remove salt-common salt-minion", false)
  end
end

When(/^I install Salt packages from "(.*?)"$/) do |host|
  target = get_target(host)
  if ['sle-minion', 'ssh-minion', 'sle-client', 'sle-migrated-minion'].include?(host)
    target.run("test -e /usr/bin/zypper && zypper --non-interactive install -y salt salt-minion", false)
  elsif ['ceos-minion'].include?(host)
    target.run("test -e /usr/bin/yum && yum -y install salt salt-minion", false)
  elsif ['ubuntu-minion', 'ubuntu-ssh-minion'].include?(host)
    target.run("test -e /usr/bin/apt && apt -y install salt-common salt-minion", false)
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

And(/^I cleanup minion "([^"]*)"$/) do |minion|
  node = get_target(minion)
  if minion == 'sle-minion'
    node.run('rcsalt-minion stop')
    node.run('rm -Rf /var/cache/salt/minion')
  elsif ['ceos-minion', 'ceos-ssh-minion', 'ubuntu-minion', 'ubuntu-ssh-minion'].include?(minion)
    node.run('systemctl stop salt-minion')
    node.run('rm -Rf /var/cache/salt/minion')
  end
end
