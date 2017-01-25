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

Then(/^I should see the "([^"]*)" as link$/) do |target|
  # $client_hostname here is correct, don't use the full for trad. client
  step %(I should see a "#{$client_hostname}" link) if target == "sle-client"
  step %(I should see a "#{$minion_fullhostname}" link) if target == "sle-minion"
  step %(I should see a "#{$ssh_minion_fullhostname}" link) if target == "ssh-minion"
  step %(I should see a "#{$ceos_minion_fullhostname}" link) if target == "ceos-minion"
end

Then(/^I should not see the "([^"]*)" as text$/) do |target|
  # $client_hostname here is correct, don't use the full for trad. client
  step %(I should not see a "#{$client_hostname}" text) if target == "sle-client"
  step %(I should not see a "#{$minion_fullhostname}" text) if target == "sle-minion"
  step %(I should not see a "#{$ssh_minion_fullhostname}" text) if target == "ssh-minion"
  step %(I should not see a "#{$ceos_minion_fullhostname}" text) if target == "ceos-minion"
end

Given(/^the list of distributions$/) do |table|
  @distros = table.raw.flatten
end

Then(/^calling mgr\-create\-bootstrap\-repo \-c should show no error$/) do
  @distros.each do |distro|
    command_output = sshcmd("mgr-create-bootstrap-repo -c #{distro}")[:stdout]
    refute_includes(command_output, "ERROR")
  end
end

# spacewalk errors steps
Then(/^I control that up2date logs on client under test contains no Traceback error$/) do
  cmd = "if grep \"Traceback\" /var/log/up2date ; then exit 1; else exit 0; fi"
  _out, code = $client.run(cmd)
  raise "error found, check the client up2date logs" if code.nonzero?
end

Then(/^I check the rhn logs for ERROR entries$/) do
  cmd = "if grep -R \"Error\" /var/log/rhn/ ; then exit 1; else exit 0; fi"
  out, code = $server.run(cmd)
  raise "RHN_LOGS: ERROR FOUNDS ! #{out} " if code.nonzero?
end
# action chains
When(/^I check radio button "(.*?)"$/) do |arg1|
   fail unless choose(arg1)
end

When(/^I open the action chain box$/) do
   fail unless find('#s2id_action-chain span.select2-arrow').click
end

When(/^I enter "(.*?)" in action-chain$/) do |arg1|
   find('#select2-drop input.select2-input').set(arg1)
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

# ipmi
When(/^I setup the ipmi network card$/) do
  $sshout = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST TERM=xterm yast lan add name=eth1 ethdevice=eth1 bootproto=dhcp 2>&1`
  unless $?.success?
    raise "Unable to change setup network card: #{$sshout}"
  end
  $sshout = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST TERM=xterm ifup eth1 2>&1`
  unless $?.success?
    raise "Unable to bring up the network interface: #{$sshout}"
  end
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
