# Copyright (c) 2010-2017 SUSE-LINUX
# Licensed under the terms of the MIT license.

When(/^I wait for "(\d+)" seconds$/) do |arg1|
  sleep(arg1.to_i)
end

When(/^I run rhn_check on this client$/) do
  $client.run("rhn_check -vvv", true, 500, 'root')
end

Then(/^I download the SSL certificate$/) do
  cert_path = "/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT"
  wget = "wget --no-check-certificate -O"
  $client.run("#{wget} #{cert_path} http://#{$server_ip}/pub/RHN-ORG-TRUSTED-SSL-CERT", true, 500, 'root')
  $client.run("ls #{cert_path}")
end

Then(/^I can see all system information for "([^"]*)"$/) do |target|
  node = get_target(target)
  node_hostname, _code = node.run("hostname -f")
  puts "i should see hostname: " + node_hostname.strip
  step %(I should see a "#{node_hostname.strip}" text)
  kernel_version, _code = node.run("uname -r")
  puts "i should see kernel version: " + kernel_version
  step %(I should see a "#{kernel_version.strip}" text)
  os_pretty_raw, _code = node.run("grep \"PRETTY\" /etc/os-release")
  os_pretty = os_pretty_raw.strip.split("=")[1].delete "\""
  puts "i should see os version: " + os_pretty
  # skip this test for centos systems
  step %(I should see a "#{os_pretty}" text) if os_pretty.include? "SUSE Linux"
end

# spacewalk errors steps
Then(/^I control that up2date logs on client under test contains no Traceback error$/) do
  cmd = "if grep \"Traceback\" /var/log/up2date ; then exit 1; else exit 0; fi"
  _out, code = $client.run(cmd)
  raise "error found, check the client up2date logs" if code.nonzero?
end

# action chains
When(/^I check radio button "(.*?)"$/) do |arg1|
   fail unless choose(arg1)
end

When(/^I enter as remote command this script in$/) do |multiline|
   within(:xpath, "//section") do
      x = find('textarea#fSptInput')
      x.set(multiline) # find("#{arg1}") #.set(lines)
   end
end

# bare metal
When(/^I check the ram value$/) do
   get_ram_value = "grep MemTotal /proc/meminfo |awk '{print $2}'"
   ram_value, _local, _remote, _code = $client.test_and_store_results_together(get_ram_value, "root", 600)
   ram_value = ram_value.gsub(/\s+/, "")
   ram_mb = ram_value.to_i / 1024
   step %(I should see a "#{ram_mb}" text)
end

When(/^I check the MAC address value$/) do
   get_mac_address = "cat /sys/class/net/eth0/address"
   mac_address, _local, _remote, _code = $client.test_and_store_results_together(get_mac_address, "root", 600)
   mac_address = mac_address.gsub(/\s+/, "")
   mac_address.downcase!
   step %(I should see a "#{mac_address}" text)
end

Then(/^I should see the CPU frequency of the client$/) do
   get_cpu_freq = "lscpu  | grep 'CPU MHz'" # | awk '{print $4}'"
   cpu_freq, _local, _remote, _code = $client.test_and_store_results_together(get_cpu_freq, "root", 600)
   get_cpu = cpu_freq.gsub(/\s+/, "")
   cpu = get_cpu.split(".")
   cpu = cpu[0].gsub(/[^\d]/, '')
   step %(I should see a "#{cpu.to_i / 1000} GHz" text)
end

When(/^I should see the power is "([^"]*)"$/) do |arg1|
  within(:xpath, "//*[@for='powerStatus']/..") do
    10.times do
      break if has_content?(arg1)
      find(:xpath, '//button[@value="Get status"]').click unless has_content?(arg1)
      sleep 3
    end
    fail unless has_content?(arg1)
  end
end

When(/^I select "(.*?)" as the origin channel$/) do |label|
  step %(I select "#{label}" from "original_id")
end

Then(/^I sync "([^"]*)" channel$/) do |channel|
    $server.run("spacewalk-repo-sync -c #{channel}", true, 130_000, "root")
end

Then(/^I add "([^"]*)" channel$/) do |channel|
    $server.run("echo -e \"admin\nadmin\n\" | mgr-sync add channel #{channel}")
end

# channel steps
arch = "x86_64"
When(/^I use spacewalk\-channel to add a valid child channel$/) do
  child_channel = "test-channel-#{arch}-child-channel"
  step %(I execute spacewalk\-channel and pass "--add -c #{child_channel} -u admin -p admin")
end

When(/^I use spacewalk\-channel to remove a valid child channel$/) do
  child_channel = "test-channel-#{arch}-child-channel"
  step %(I execute spacewalk\-channel and pass "--remove -c #{child_channel} -u admin -p admin")
end

Then(/^I want to see all valid child channels$/) do
  step %(I want to get "test-channel-#{arch}-child-channel")
end

Then(/^I wont see any of the valid child channels$/) do
  step %(I wont get "test-channel-#{arch}-child-channel")
end

Then(/^I create mock initrd if download fails$/) do
   # sometimes the download via sumaform fails. we create a fake empty img.
   # for current testing this is enough.
   initrd = "/install/Fedora_12_i386/images/pxeboot/initrd.img"
   _out, code = $server.run("test -f #{initrd}", false)
   $server.run("touch #{initrd}") if code.nonzero?
end

And(/^I navigate to "([^"]*)" page$/) do |page|
  visit("https://#{$server_fullhostname}/#{page}")
end

# nagios steps

When(/^I perform a nagios check patches$/) do
  command = "/usr/lib/nagios/plugins/check_suma_patches #{$client_fullhostname} > /tmp/nagios.out"
  $server.run(command, false, 600, 'root')
end

When(/^I perform a nagios check last event$/) do
  command = "/usr/lib/nagios/plugins/check_suma_lastevent #{$client_fullhostname} > /tmp/nagios.out"
  $server.run(command, false, 600, 'root')
end

When(/^I perform an invalid nagios check patches$/) do
  command = "/usr/lib/nagios/plugins/check_suma_patches does.not.exist > /tmp/nagios.out"
  $server.run(command, false, 600, 'root')
end

Then(/^I should see WARNING: 1 patch pending$/) do
  command = "grep \"WARNING: 1 patch(es) pending\" /tmp/nagios.out"
  $server.run(command, true, 600, 'root')
end

Then(/^I should see Completed: OpenSCAP xccdf scanning scheduled by admin/) do
  command = "grep \"Completed: OpenSCAP xccdf scanning scheduled by admin\" /tmp/nagios.out"
  $server.run(command, true, 600, 'root')
end

Then(/^I should see an unknown system message$/) do
  command = "grep -i \"^Unknown system:.*does.not.exist\" /tmp/nagios.out 2>&1"
  $server.run(command, true, 600, 'root')
end

# systemspage and clobber
Given(/^I am on the Systems page$/) do
  steps %(
  When I am authorized as "admin" with password "admin"
  And I follow "Home" in the left menu
  And I follow "Systems" in the left menu
  And I follow "Overview" in the left menu
  )
end

Given(/cobblerd is running/) do
  ct = CobblerTest.new
  unless ct.is_running
    raise "cobblerd is not running"
  end
end

Then(/create distro "([^"]*)" as user "([^"]*)" with password "([^"]*)"/) do |arg1, arg2, arg3|
  ct = CobblerTest.new
  ct.login(arg2, arg3)
  if ct.distro_exists(arg1)
    raise "distro " + arg1 + " already exists"
  end
  ct.distro_create(arg1, "/install/SLES11-SP1-x86_64/DVD1/boot/x86_64/loader/linux", "install/SLES11-SP1-x86_64/DVD1/boot/x86_64/loader/initrd")
end

Given(/distro "([^"]*)" exists/) do |arg1|
  ct = CobblerTest.new
  unless ct.distro_exists(arg1)
    raise "distro " + arg1 + " does not exist"
  end
end

Then(/create profile "([^"]*)" as user "([^"]*)" with password "([^"]*)"/) do |arg1, arg2, arg3|
  ct = CobblerTest.new
  ct.login(arg2, arg3)
  if ct.profile_exists(arg1)
    raise "profile " + arg1 + " already exists"
  end
  ct.profile_create("testprofile", "testdistro", "/install/empty.xml")
end

When(/^I attach the file "(.*)" to "(.*)"$/) do |path, field|
  attach_file(field, File.join(File.dirname(__FILE__), '/../upload_files/', path))
end

When(/I view system with id "([^"]*)"/) do |arg1|
  visit Capybara.app_host + "/rhn/systems/details/Overview.do?sid=" + arg1
end

# weak deaps steps
When(/^I refresh the metadata$/) do
  $client.run("rhn_check -vvv", true, 500, 'root')
  client_refresh_metadata
end

Then(/^I should have '([^']*)' in the metadata$/) do |text|
  arch, _code = $client.run("uname -m")
  arch.chomp!
  cmd = "zgrep '#{text}' #{client_raw_repodata_dir("test-channel-#{arch}")}/primary.xml.gz"
  $client.run(cmd, true, 500, 'root')
end

Then(/^I should not have '([^']*)' in the metadata$/) do |text|
  arch, _code = $client.run("uname -m")
  arch.chomp!
  cmd = "zgrep '#{text}' #{client_raw_repodata_dir("test-channel-#{arch}")}/primary.xml.gz"
  $client.run(cmd, true, 500, 'root')
end

Then(/^"([^"]*)" should exists in the metadata$/) do |file|
  arch, _code = $client.run("uname -m")
  arch.chomp!
  fail unless file_exists?($client, "#{client_raw_repodata_dir("test-channel-#{arch}")}/#{file}")
end

Then(/^I should have '([^']*)' in the patch metadata$/) do |text|
  arch, _code = $client.run("uname -m")
  arch.chomp!
  cmd = "zgrep '#{text}' #{client_raw_repodata_dir("test-channel-#{arch}")}/updateinfo.xml.gz"
  $client.run(cmd, true, 500, 'root')
end

# channel steps
Then(/^I should see package "([^"]*)"$/) do |package|
  fail unless has_xpath?("//div[@class=\"table-responsive\"]/table/tbody/tr/td/a[contains(.,'#{package}')]")
end

Given(/^I am on the manage software channels page$/) do
  step %(I am authorized as "testing" with password "testing")
  visit("https://#{$server_fullhostname}/rhn/channels/manage/Manage.do")
end

Given(/^metadata generation finished for "([^"]*)"$/) do |channel|
  50.times do
      begin
          sshcmd("ls /var/cache/rhn/repodata/#{channel}/updateinfo.xml.gz")
      rescue
          sleep 2
      else
          break
      end
  end
end

When(/^I push package "([^"]*)" into "([^"]*)" channel$/) do |arg1, arg2|
  srvurl = "http://#{ENV['TESTHOST']}/APP"
  command = "rhnpush --server=#{srvurl} -u admin -p admin --nosig -c #{arg2} #{arg1} "
  $server.run(command, true, 500, 'root')
  $server.run("ls -lR /var/spacewalk/packages", true, 500, 'root')
end

Then(/^I should see package "([^"]*)" in channel "([^"]*)"$/) do |pkg, channel|
  steps %(
    And I follow "Channels > All" in the left menu
    And I follow "#{channel}"
    And I follow "Packages"
    Then I should see package "#{pkg}"
    )
end

# setup wizard

When(/^I make the credentials primary$/) do
  fail unless find('i.fa-star-o').click
end

When(/^I refresh scc$/) do
  sshcmd('echo -e "admin\nadmin\n" | mgr-sync refresh', ignore_err: true)
end

When(/^I delete the primary credentials$/) do
  fail unless find('i.fa-trash-o', :match => :first).click
  step 'I click on "Delete"'
end

When(/^I view the primary subscription list$/) do
  fail unless find('i.fa-th-list', :match => :first).click
end

When(/^I view the primary subscription list for asdf$/) do
  within(:xpath, "//h3[contains(text(), 'asdf')]/../..") do
    fail unless find('i.fa-th-list', :match => :first).click
  end
end

When(/^I select "([^\"]*)" as a product for the "([^\"]*)" architecture$/) do |product, architecture|
  within(:xpath, "(//span[contains(text(), '#{product}')]/ancestor::tr[td[contains(text(), '#{architecture}')]])[1]") do
    fail unless find("button.product-add-btn").click
    begin
      # wait to finish scheduling
      Timeout.timeout(DEFAULT_TIMEOUT) do
        loop do
          begin
            break unless find("button.product-add-btn").visible?
            sleep 2
          rescue Capybara::ElementNotFound
            break
          end
        end
      end
    rescue Timeout::Error
      puts "timeout reached"
    end
  end
end

When(/^I select the addon "(.*?)" for the product "(.*?)" with arch "(.*?)"$/) do |addon, product, archi|
  # xpath query is too long, so breaking up on multiple lines.
  xpath =  "//span[contains(text(), '#{product}')]/"
  xpath += "ancestor::tr[td[contains(text(), '#{archi}')]]/following::span"
  xpath += "[contains(text(), '#{addon}')]/../.."
  within(:xpath, xpath) do
    fail unless find("button.product-add-btn").click
    begin
      # wait to finish scheduling
      Timeout.timeout(DEFAULT_TIMEOUT) do
        loop do
          begin
            break unless find("button.product-add-btn").visible?
            sleep 2
          rescue Capybara::ElementNotFound
            break
          end
        end
      end
    rescue Timeout::Error
      puts "timeout reached"
    end
  end
end

When(/^I click the Add Product button$/) do
  fail unless find("button#synchronize").click
end

When(/^I verify the products were added$/) do
  output = sshcmd('echo -e "admin\nadmin\n" | mgr-sync list channels', ignore_err: true)
  fail unless output[:stdout].include? '[I] SLES12-SP2-Pool for x86_64 SUSE Linux Enterprise Server 12 SP2 x86_64 [sles12-sp2-pool-x86_64]'
  fail unless output[:stdout].include? '[I] SLE-Manager-Tools12-Pool x86_64 SP2 SUSE Manager Tools [sle-manager-tools12-pool-x86_64-sp2]'
  fail unless output[:stdout].include? '[I] SLE-Module-Legacy12-Updates for x86_64 SP2 Legacy Module 12 x86_64 [sle-module-legacy12-updates-x86_64-sp2]'
end

When(/^I click the channel list of product "(.*?)" for the "(.*?)" architecture$/) do |product, architecture|
  within(:xpath, "//span[contains(text(), '#{product}')]/ancestor::tr[td[contains(text(), '#{architecture}')]]") do
    fail unless find('.product-channels-btn').click
  end
end

Then(/^I see verification succeeded/) do
  find("i.text-success")
end

# configuration steps

When(/^I change the local file "([^"]*)" to "([^"]*)"$/) do |filename, content|
    $client.run("echo \"#{content}\" > #{filename}", true, 600, 'root')
end

Then(/^I should see a table line with "([^"]*)", "([^"]*)", "([^"]*)"$/) do |arg1, arg2, arg3|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
      fail unless find_link(arg2)
      fail unless find_link(arg3)
  end
end

Then(/^I should see a table line with "([^"]*)", "([^"]*)"$/) do |arg1, arg2|
  within(:xpath, "//div[@class=\"table-responsive\"]/table/tbody/tr[.//td[contains(.,'#{arg1}')]]") do
      fail unless find_link(arg2)
  end
end

Then(/^On this client the File "([^"]*)" should exists$/) do |arg1|
    $client.run("test -f #{arg1}", true)
end

Then(/^On this client the File "([^"]*)" should have the content "([^"]*)"$/) do |filename, content|
    $client.run("test -f #{filename}")
    $client.run("grep #{content} #{filename}")
end
