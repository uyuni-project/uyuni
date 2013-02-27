# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

When /^I execute ncc\-sync "([^"]*)"$/ do |arg1|
    $sshout = ""
    $sshout = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST mgr-ncc-sync #{arg1} 2>&1`
    if ! $?.success?
        raise "Execute command failed: #{$!}: #{$sshout}"
    end
end

When /^I execute mgr\-bootstrap "([^"]*)"$/ do |arg1|
    arch=`uname -m`
    arch.chomp!
    if arch != "x86_64"
        arch = "i586"
    end
    $sshout = ""
    $sshout = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST mgr-bootstrap --activation-keys=1-SUSE-PKG-#{arch} #{arg1} 2>&1`
    if ! $?.success?
        raise "Execute command failed: #{$!}: #{$sshout}"
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
    $sshout = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST test -f "#{arg1}" 2>&1`
    if ! $?.success?
        raise "File #{arg1} does not exist on server"
    end
end

When /^file "([^"]*)" not exists on server$/ do |arg1|
    $sshout = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST test -f "#{arg1}" 2>&1`
    if $?.success?
        raise "File #{arg1} exists on server"
    end
end

When /^file "([^"]*)" contains "([^"]*)"$/ do |arg1, arg2|
    $sshout = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST grep "#{arg2}" "#{arg1}" 2>&1`
    if ! $?.success?
        raise "#{arg2} not found in File #{arg1}"
    end
end


Then /^I want to get "([^"]*)"$/ do |arg1|
    found = false
    $sshout.each_line() do |line|
        if line.include?(arg1)
            found = true
            break
        end
    end
    if not found
        raise "'#{arg1}' not found in output '#{$sshout}'"
    end
end

Then /^I wont get "([^"]*)"$/ do |arg1|
    found = false
    $sshout.each_line() do |line|
        if line.include?(arg1)
            found = true
            break
        end
    end
    if found
        raise "'#{arg1}' found in output '#{$sshout}'"
    end
end

Then /^I restart the spacewalk service$/ do
    $sshout = ""
    $sshout = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST spacewalk-service restart 2>&1`
    if ! $?.success?
        raise "Execute command failed: #{$!}: #{$sshout}"
    end
end

Then /^I execute spacewalk-debug on the server$/ do
    $sshout = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST spacewalk-debug 2>&1`
    if ! $?.success?
        raise "Execute command failed: #{$!}: #{$sshout}"
    end
end

When /^I copy "([^"]*)"$/ do |arg1|
    user = "root@"
    $sshout = `echo | scp -o StrictHostKeyChecking=no #{user}$TESTHOST:#{arg1} . 2>&1`
    if ! $?.success?
        raise "Execute command failed: #{$!}: #{$sshout}"
    end
end

