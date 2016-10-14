# Copyright (c) 2014 SUSE
# Licensed under the terms of the MIT license.

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
  arch = `uname -m`
  arch.chomp!
  if arch != "x86_64"
    arch = "i586"
  end
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
  output = sshcmd("grep #{arg2} #{arg1}", ignore_err:  true)
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
end

Then(/^I execute spacewalk-debug on the server$/) do
   out, _local, _remote, _code = $server.test_and_store_results_together("spacewalk-debug", "root", 600)
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
  output = sshcmd("cobbler system report --name #{$myhostname}:1", ignore_err: true)[:stdout]
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
