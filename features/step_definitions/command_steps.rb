# Copyright (c) 2014 SUSE
# Licensed under the terms of the MIT license.

When /^I execute ncc\-sync "([^"]*)"$/ do |arg1|
  $command_output = ""
  $command_output = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST mgr-ncc-sync #{arg1} 2>&1`
  raise "Execute command failed: #{$!}: #{$command_output}" unless $?.success?
end

When /^I execute mgr\-bootstrap "([^"]*)"$/ do |arg1|
  arch=`uname -m`
  arch.chomp!
  if arch != "x86_64"
    arch = "i586"
  end
  $command_output = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST mgr-bootstrap --activation-keys=1-SUSE-PKG-#{arch} #{arg1} 2>&1`
  if ! $?.success?
    raise "Execute command failed: #{$!}: #{$command_output}"
  end
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
  $command_output = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST test -f "#{arg1}" 2>&1`
  if ! $?.success?
    raise "File #{arg1} does not exist on server"
  end
end

When /^file "([^"]*)" not exists on server$/ do |arg1|
  $command_output = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST test -f "#{arg1}" 2>&1`
  if $?.success?
    raise "File #{arg1} exists on server"
  end
end

When /^file "([^"]*)" contains "([^"]*)"$/ do |arg1, arg2|
  $command_output = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST grep "#{arg2}" "#{arg1}" 2>&1`
  if ! $?.success?
    $stderr.write("-----\n#{$command_output}\n-----\n")
    raise "#{arg2} not found in File #{arg1}"
  end
end

When /^I check the tomcat logs for errors$/ do
  $command_output = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST grep ERROR /var/log/tomcat6/catalina.out 2>&1`
  $command_output.each_line() do |line|
    puts line
  end
end

When /^I check the tomcat logs for NullPointerExceptions$/ do
  $command_output = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST grep -n1 NullPointer /var/log/tomcat6/catalina.out 2>&1`
  $command_output.each_line() do |line|
    puts line
  end
end

Then /^I restart the spacewalk service$/ do
  $command_output = ""
  $command_output = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST spacewalk-service restart 2>&1`
  if ! $?.success?
    raise "Execute command failed: #{$!}: #{$command_output}"
  end
end

Then /^I execute spacewalk-debug on the server$/ do
  $command_output = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST spacewalk-debug 2>&1`
  if ! $?.success?
    raise "Execute command failed: #{$!}: #{$command_output}"
  end
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
  step "file \"/srv/tftpboot/pxelinux.cfg/default\" contains \"ONTIMEOUT\\ pxe-default-profile\""
end

Then /^the pxe-default-profile should be disabled$/ do
  step "file \"/srv/tftpboot/pxelinux.cfg/default\" contains \"ONTIMEOUT\\ local\""
end

Then /^the cobbler report contains "([^"]*)"$/ do |arg1|
  $command_output = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST cobbler system report --name "#{$myhostname}:1" 2>&1`
  if ! $?.success?
    raise "Execute command failed: #{$!}: #{$command_output}"
  end
  if ! $command_output.include?(arg1)
    raise "Not found: #{$command_output}"
  end
end

Then /^I clean the search index on the server$/ do
  $command_output = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST /usr/sbin/rcrhn-search cleanindex 2>&1`
  if ! $?.success?
    raise "Execute command failed: #{$!}: #{$command_output}"
  end
end

When /^I execute spacewalk\-channel and pass "([^"]*)"$/ do |arg1|
  $command_output = `spacewalk-channel #{arg1} 2>&1`
  $command_status = $?
end

Then /^it should succeed$/ do
  if ! $command_status.success?
    raise "Executed command failed: #{$command_status}"
  end
end

Then /^it should fail with exit code "([^"]*)"$/ do |arg1|
  if $command_status.success? || $command_status.exitstatus != arg1.to_i
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
      found = strue
      break
    end
  end
  if found
    raise "'#{arg1}' found in output '#{$command_output}'"
  end
end

