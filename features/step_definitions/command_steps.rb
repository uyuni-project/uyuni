# Copyright (c) 2014-16 SUSE # Licensed under the terms of the MIT license.

require 'timeout'

Then(/^I apply highstate on Sles minion$/) do
  cmd = "salt '*min*sles12sp2*' state.highstate"
  out, code = $server.run(cmd)
  puts out
  raise "Apply highstate FAILED!"  if code.nonzero?
end

Then(/^I wait until "([^"]*)" service is up and running on "([^"]*)"$/) do |service, target|
  cmd = "systemctl is-active #{service}"
  node = get_target(target)
  Timeout.timeout(200) do
    loop do
      out, code = node.run(cmd, false, 200)
      if code.zero?
        puts "#{service} service is up and running \n #{out}"
        break
      end
    end
  end
  sleep(3)
end

When(/^I execute mgr\-sync "([^"]*)" with user "([^"]*)" and password "([^"]*)"$/) do |arg1, u, p|
  $command_output = sshcmd("echo -e '#{u}\n#{p}\n' | mgr-sync #{arg1}", ignore_err: true)[:stdout]
end

When(/^I execute mgr\-sync "([^"]*)"$/) do |arg1|
  $command_output = sshcmd("mgr-sync #{arg1}")[:stdout]
end

When(/^I remove the mgr\-sync cache file$/) do
  $command_output = sshcmd("rm -f ~/.mgr-sync")[:stdout]
end

When(/^I execute mgr\-sync refresh$/) do
  $command_output = sshcmd("mgr-sync refresh", ignore_err: true)[:stderr]
end

When(/^I execute mgr\-bootstrap "([^"]*)"$/) do |arg1|
  arch = "x86_64"
  $command_output = sshcmd("mgr-bootstrap --activation-keys=1-SUSE-PKG-#{arch} #{arg1}")[:stdout]
end

When(/^I fetch "([^"]*)" from server$/) do |arg1|
   $client.run("wget http://#{$server_ip}/#{arg1}", true, 500, 'root')
end

When(/^I execute "([^"]*)"$/) do |arg1|
  $client.run("sh ./#{arg1}", true, 600, 'root')
end

When(/^file "([^"]*)" exists on server$/) do |arg1|
  $server.run("test -f #{arg1}")
end

When(/^file "([^"]*)" not exists on server$/) do |arg1|
  $server.run("test -f #{arg1}")
end

When(/^file "([^"]*)" contains "([^"]*)"$/) do |arg1, arg2|
  output = sshcmd("grep #{arg2} #{arg1}", ignore_err: true)
  unless output[:stderr].empty?
    $stderr.write("-----\n#{output[:stderr]}\n-----\n")
    raise "#{arg2} not found in File #{arg1}"
  end
end

When(/^I check the tomcat logs for errors$/) do
  output = sshcmd("grep ERROR /var/log/tomcat6/catalina.out", ignore_err: true)[:stdout]
  output.each_line do |line|
    puts line
  end
end

When(/^I check the tomcat logs for NullPointerExceptions$/) do
  output = sshcmd("grep -n1 NullPointer /var/log/tomcat6/catalina.out", ignore_err: true)[:stdout]
  output.each_line do |line|
    puts line
  end
end

Then(/^I restart the spacewalk service$/) do
  sshcmd("spacewalk-service restart")
  sleep(5)
end

Then(/^I shutdown the spacewalk service$/) do
 $server.run("spacewalk-service stop")
end

Then(/^I execute spacewalk-debug on the server$/) do
   out, _local, _remote, _code = $server.test_and_store_results_together("spacewalk-debug", "root", 600)
   step %(I copy "/tmp/spacewalk-debug.tar.bz2")
end

When(/^I copy "([^"]*)"$/) do |arg1|
  user = "root@"
  $command_output = `echo | scp -o StrictHostKeyChecking=no #{user}$TESTHOST:#{arg1} . 2>&1`
  unless $?.success?
    raise "Execute command failed: #{$!}: #{$command_output}"
  end
end

When(/^I copy to server "([^"]*)"$/) do |arg1|
  user = "root@"
  $command_output = `echo | scp -o StrictHostKeyChecking=no #{arg1} #{user}$TESTHOST: 2>&1`
  unless $?.success?
    raise "Execute command failed: #{$!}: #{$command_output}"
  end
end

Then(/^the pxe-default-profile should be enabled$/) do
  sleep(1)
  step %(file "/srv/tftpboot/pxelinux.cfg/default" contains "'ONTIMEOUT pxe-default-profile'")
end

Then(/^the pxe-default-profile should be disabled$/) do
  sleep(1)
  step %(file "/srv/tftpboot/pxelinux.cfg/default" contains "'ONTIMEOUT local'")
end

Then(/^the cobbler report contains "([^"]*)"$/) do |arg1|
  output = sshcmd("cobbler system report --name #{$client_fullhostname}:1", ignore_err: true)[:stdout]
  unless output.include?(arg1)
    raise "Not found: #{output}"
  end
end

Then(/^I clean the search index on the server$/) do
  output = sshcmd("/usr/sbin/rcrhn-search cleanindex", ignore_err: true)
  fail if output[:stdout].include?('ERROR')
end

When(/^I execute spacewalk\-channel and pass "([^"]*)"$/) do |arg1|
  command = "spacewalk-channel #{arg1}"
  $command_output, _code = $client.run(command, true, 500, "root")
end

When(/^spacewalk\-channel fails with "([^"]*)"$/) do |arg1|
  command = "spacewalk-channel #{arg1}"
  # we are checking that the cmd should fail here
  $command_output, code = $client.run(command, false, 500, "root")
  raise "#{command} should fail, but hasn't" if code.zero?
end

Then(/^I want to get "([^"]*)"$/) do |arg1|
  found = false
  $command_output.each_line do |line|
    if line.include?(arg1)
      found = true
      break
    end
  end
  unless found
    raise "'#{arg1}' not found in output '#{$command_output}'"
  end
end

Then(/^I wont get "([^"]*)"$/) do |arg1|
  found = false
  $command_output.each_line do |line|
    if line.include?(arg1)
      found = true
      break
    end
  end
  if found
    raise "'#{arg1}' found in output '#{$command_output}'"
  end
end

Then(/^I wait for mgr-sync refresh is finished$/) do
  for c in 0..20
      begin
          sshcmd('ls /var/lib/spacewalk/scc/scc-data/*organizations_orders.json')
      rescue
          sleep 15
      else
          break
      end
  end
end

Then(/^I should see "(.*?)" in the output$/) do |arg1|
  assert_includes(@command_output, arg1)
end

Then(/^Service "([^"]*)" is enabled on the Server$/) do |service|
    output = sshcmd("systemctl is-enabled '#{service}'", ignore_err: true)[:stdout]
    output.chomp!
    fail if output != "enabled"
end

Then(/^Service "([^"]*)" is running on the Server$/) do |service|
    output = sshcmd("systemctl is-active '#{service}'", ignore_err: true)[:stdout]
    output.chomp!
    fail if output != "active"
end

# snapshots
When(/^I take a snapshot "([^"]*)"$/) do |name|
  $sshout = ""
  $sshout = `echo | ssh -o StrictHostKeyChecking=no root@$VHOST qemu-img snapshot -c #{name} $IMGDIR/$VMDISK.qcow2`
  puts "Creating snapsnot failed..." unless $?.success?
end

When(/^I run "([^"]*)" on "([^"]*)"$/) do |cmd, host|
  node = get_target(host)
  node.run(cmd)
end

When(/^I run "([^"]*)" on "([^"]*)" without error control$/) do |cmd, host|
  node = get_target(host)
  _out, $fail_code = node.run(cmd, false)
end

Then(/^the command should fail$/) do
   raise "Previous command must fail, but has NOT failed!" if $fail_code.zero?
end

When(/^"(.*)" exists on the filesystem of "(.*)"$/) do |file, host|
  node = get_target(host)
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        break if file_exists?(node, file)
        sleep(1)
      end
    end
  rescue Timeout::Error
    puts "timeout waiting for the file to appear"
  end
  fail unless file_exists?(node, file)
end

Then(/^I remove "(.*)" from "(.*)"$/) do |file, host|
  node = get_target(host)
  file_delete(node, file)
end

Then(/^I wait and check that "([^"]*)" has rebooted$/) do |target|
  timeout = 800
  if target == "sle-client"
    checkShutdown($client_fullhostname, timeout)
    checkRestart($client_fullhostname, get_target(target), timeout)
  elsif target == "ceos-minion"
    checkShutdown($ceos_minion_fullhostname, timeout)
    checkRestart($ceos_minion_fullhostname, get_target(target), timeout)
  elsif target == "ssh-minion"
    checkShutdown($ssh_minion_fullhostname, timeout)
    checkRestart($ssh_minion_fullhostname, get_target(target), timeout)
  elsif target == "sle-minion"
    checkShutdown($minion_fullhostname, timeout)
    checkRestart($minion_fullhostname, get_target(target), timeout)
  end
end

When(/^I call spacewalk\-repo\-sync for channel "(.*?)" with a custom url "(.*?)"$/) do |arg1, arg2|
  @command_output = sshcmd("spacewalk-repo-sync -c #{arg1} -u #{arg2}")[:stdout]
end

When(/^I click on "([^"]+)" for "([^"]+)"$/) do |arg1, arg2|
  within(:xpath, "//section") do
    within(:xpath, "//table/tbody/tr[.//a[contains(.,'#{arg2}')]]") do
      find_link(arg1).click
    end
  end
end

When(/^I disable IPv6 forwarding on all interfaces of the SLE minion$/) do
  $minion.run("sysctl net.ipv6.conf.all.forwarding=0")
end

When(/^I enable IPv6 forwarding on all interfaces of the SLE minion$/) do
  $minion.run("sysctl net.ipv6.conf.all.forwarding=1")
end

And(/^I register the centos7 as tradclient$/) do
  cert_path = '/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT'
  wget = 'wget --no-check-certificate -O'
  register = "rhnreg_ks --username=admin --password=admin --force \\" \
              "--serverUrl=https://#{$server_ip}/XMLRPC \\" \
              "--sslCACert=/usr/share/rhn/RHN-ORG-TRUSTED-SSL-CERT \\" \
              "--activationkey=1-MINION-TEST"

  $ceos_minion.run("#{wget} #{cert_path} http://#{$server_ip}/pub/RHN-ORG-TRUSTED-SSL-CERT", true, 500)
  $ceos_minion.run(register)
end

When(/^I wait for the openSCAP audit to finish$/) do
  begin
    Timeout.timeout(30) do
      loop do
        _output, code = $minion.run('ps aux | grep "oscap\ xccdf"', false)
        unless code.zero?
          break
        end
      end
    end
  rescue Timeout::Error
    raise "process did not stop after several tries"
  end
end

$space = "spacecmd -u admin -p admin "
And(/I check status "([^"]*)" with spacecmd on "([^"]*)"$/) do |status, target|
  host = $ssh_minion_fullhostname if target == "ssh-minion"
  host = $ceos_minion_fullhostname if target == "ceos-minion"
  cmd = "#{$space} system_listevents #{host} | head -n5"
  $server.run("#{$space} clear_caches")
  out, _code = $server.run(cmd)
  unless out.include? status
    raise "#{out} should contain #{status}"
  end
end

And(/I create dockerized minions$/) do
  master, _code = $minion.run("cat /etc/salt/minion.d/susemanager.conf")
  # build everything
  distros = %w(rhel6 rhel7 sles11sp4 sles12 sles12sp1)
  distros.each do |os|
    $minion.run("docker build https://gitlab.suse.de/galaxy/suse-manager-containers.git#master:minion-fabric/#{os}/ -t #{os}", true, 2000)
    spawn_minion = "/etc/salt/minion; dbus-uuidgen > /etc/machine-id; salt-minion -l trace"
    $minion.run("docker run -h #{os} -d --entrypoint '/bin/sh' #{os} -c \"echo \'#{master}\' > #{spawn_minion}\"")
    puts "minion #{os} created and running"
  end
  # accept all the key on master, wait dinimically for last key
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        out, _code = $server.run("salt-key -l unaccepted")
        # if we see the last os, we can break
        if out.include? distros.last
          $server.run("salt-key -A -y")
          break
        end
        sleep 5
      end
    end
  rescue Timeout::Error
    raise "something wrong with creation of minion docker"
  end
end
