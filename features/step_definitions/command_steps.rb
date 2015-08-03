# Copyright (c) 2014 SUSE
# Licensed under the terms of the MIT license.

When /^I execute mgr\-sync "([^"]*)" with user "([^"]*)" and password "([^"]*)"$/ do |arg1, u, p|
  $command_output = sshcmd("echo -e '#{u}\n#{p}\n' | mgr-sync #{arg1}", ignore_err: true)[:stdout]
end

When /^I execute mgr\-sync "([^"]*)"$/ do |arg1|
  $command_output = sshcmd("mgr-sync #{arg1}")[:stdout]
end

When /^I remove the mgr\-sync cache file$/ do
  $command_output = sshcmd("rm -f ~/.mgr-sync")[:stdout]
end

When /^I execute mgr\-sync refresh$/ do
  $command_output = `ssh $TESTHOST mgr-sync refresh 2>&1`
end

When /^I execute mgr\-bootstrap "([^"]*)"$/ do |arg1|
  arch=`uname -m`
  arch.chomp!
  if arch != "x86_64"
    arch = "i586"
  end
  $command_output = sshcmd("mgr-bootstrap --activation-keys=1-SUSE-PKG-#{arch} #{arg1}")[:stdout]
end

When /^I fetch "([^"]*)" from server$/ do |arg1|
  output = `curl -SkO http://$TESTHOST/#{arg1}`
  if ! $?.success?
    raise "Execute command failed: #{$!}: #{output}"
  end
end

When /^I execute "([^"]*)"$/ do |arg1|
  output = `sh ./#{arg1} 2>&1`
  if ! $?.success?
    raise "Execute command (#{arg1}) failed(#{$?}): #{$!}: #{output}"
  end
end

When /^file "([^"]*)" exists on server$/ do |arg1|
  sshcmd("test -f #{arg1}")
end

When /^file "([^"]*)" not exists on server$/ do |arg1|
  sshcmd("test -f #{arg1}")
end

When /^file "([^"]*)" contains "([^"]*)"$/ do |arg1, arg2|
  output = sshcmd("grep #{arg2} #{arg1}", ignore_err:  true)
  unless output[:stderr].empty?
    $stderr.write("-----\n#{output[:stderr]}\n-----\n")
    raise "#{arg2} not found in File #{arg1}"
  end
end

When /^I check the tomcat logs for errors$/ do
  output = sshcmd("grep ERROR /var/log/tomcat6/catalina.out", ignore_err: true)[:stdout]
  output.each_line() do |line|
    puts line
  end
end

When /^I check the tomcat logs for NullPointerExceptions$/ do
  output = sshcmd("grep -n1 NullPointer /var/log/tomcat6/catalina.out", ignore_err: true)[:stdout]
  output.each_line() do |line|
    puts line
  end
end

Then /^I restart the spacewalk service$/ do
  sshcmd("spacewalk-service restart")
end

Then /^I execute spacewalk-debug on the server$/ do
  sshcmd("spacewalk-debug")
end

When /^I copy "([^"]*)"$/ do |arg1|
  user = "root@"
  $command_output = `echo | scp -o StrictHostKeyChecking=no #{user}$TESTHOST:#{arg1} . 2>&1`
  if ! $?.success?
    raise "Execute command failed: #{$!}: #{$command_output}"
  end
end

When /^I copy to server "([^"]*)"$/ do |arg1|
  user = "root@"
  $command_output = `echo | scp -o StrictHostKeyChecking=no #{arg1} #{user}$TESTHOST: 2>&1`
  if ! $?.success?
    raise "Execute command failed: #{$!}: #{$command_output}"
  end
end

Then /^the pxe-default-profile should be enabled$/ do
  step %[file "/srv/tftpboot/pxelinux.cfg/default" contains "ONTIMEOUT\\ pxe-default-profile"]
end

Then /^the pxe-default-profile should be disabled$/ do
  step "file \"/srv/tftpboot/pxelinux.cfg/default\" contains \"ONTIMEOUT\\ local\""
end

Then /^the cobbler report contains "([^"]*)"$/ do |arg1|
  output = sshcmd("cobbler system report --name #{$myhostname}:1", ignore_err: true)[:stdout]
  unless output.include?(arg1)
    raise "Not found: #{output}"
  end
end

Then /^I clean the search index on the server$/ do
  output = sshcmd("/usr/sbin/rcrhn-search cleanindex", ignore_err: true)
  fail if output[:stdout].include?('ERROR')
end

When /^I execute spacewalk\-channel and pass "([^"]*)"$/ do |arg1|
  $command_output = `spacewalk-channel #{arg1} 2>&1`
  if ! $?.success?
    raise "spacewalk-channel with #{arg1} command failed #{$command_output}"
  end
end

When /^spacewalk\-channel fails with "([^"]*)"$/ do |arg1|
  $command_output = `spacewalk-channel #{arg1} 2>&1`
  if $?.success? #|| $command_status.exitstatus != arg1.to_i
    raise "Executed command was successful: #{$status}"
  end
end

Then /^I want to get "([^"]*)"$/ do |arg1|
  found = false
  $command_output.each_line() do |line|
    if line.include?(arg1)
      found = true
      break
    end
  end
  if not found
    raise "'#{arg1}' not found in output '#{$command_output}'"
  end
end

Then /^I wont get "([^"]*)"$/ do |arg1|
  found = false
  $command_output.each_line() do |line|
    if line.include?(arg1)
      found = true
      break
    end
  end
  if found
    raise "'#{arg1}' found in output '#{$command_output}'"
  end
end
