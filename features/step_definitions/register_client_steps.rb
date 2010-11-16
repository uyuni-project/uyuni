

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
  Given "I am on the Systems page"
  Given "I follow \"Systems\" in element \"sidenav\""
  When  "I follow this client link"
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

  output = `#{command}`
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
  When "I register using \"1-SUSE-DEV-#{arch}\" key"
end

Then /^I should see this client in spacewalk$/ do
  Given "I am on the Systems page"
  Then "I should see this client as link"
end

Then /^I should see this client as link$/ do
  Then "I should see a \"#{$myhostname}\" link"
end

When /^I follow this client link$/ do
  When "I follow \"#{$myhostname}\""
end
