# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Given /^I am root$/ do
  uid = `id -u`
  if ! $?.success? || uid.to_i != 0
    raise "You are not root!"
  end
  if $myhostname == "linux"
    raise "Invalid hostname"
  end
end

Given /^I am on the Systems overview page of this client$/ do
  steps %[
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow this client link
  ]
end

Given /^I update the profile of this client$/ do
  `rhn-profile-sync`
  if ! $?.success?
    raise "Profile sync failed"
  end
end

When /^I register using "([^"]*)" key$/ do |arg1|
  # remove systemid file
  `rm -f /etc/sysconfig/rhn/systemid`

  regurl = "http://#{ENV['TESTHOST']}/XMLRPC"

  command = "rhnreg_ks --serverUrl=#{regurl} --activationkey=#{arg1}"
  #print "Command: #{command}\n"

  output = `#{command} 2>&1`
  if ! $?.success?
    raise "Registration failed '#{command}' #{$!}: #{output}"
  end
end

When /^I register using an activation key$/ do
  arch=`uname -m`
  arch.chomp!
  if arch != "x86_64"
    arch = "i586"
  end
  step %[I register using "1-SUSE-DEV-#{arch}" key]
end

Then /^I should see this client in spacewalk$/ do
  steps %[
    Given I am on the Systems page
    Then I should see this client as link
  ]
end

Then /^I should see this client as link$/ do
  step %[I should see a "#{$myhostname}" link]
end

When /^I follow this client link$/ do
  step %[I follow "#{$myhostname}"]
end

Then /^config-actions are enabled$/ do
  if not File.exists?('/etc/sysconfig/rhn/allowed-actions/configfiles/all')
    raise "config actions are disabled: /etc/sysconfig/rhn/allowed-actions/configfiles/all does not exist"
  end
end

Then /^remote-commands are enabled$/ do
  if not File.exists?('/etc/sysconfig/rhn/allowed-actions/script/run')
    raise "remote-commands are disabled: /etc/sysconfig/rhn/allowed-actions/script/run does not exist"
  end
end

When /^I wait for the data update$/ do
  for c in 0..6
    if page.has_content?(debrand_string("Software Updates Available"))
      break
    end
    sleep 30
  end
end
