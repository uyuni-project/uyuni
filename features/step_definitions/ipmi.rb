# COPYRIGHT 2015 SUSE LLC

When /^I setup the ipmi network card$/ do
  $sshout = `echo | ssh -l root -o StrictHostKeyChecking=no $TESTHOST TERM=xterm yast lan add name=eth1 ethdevice=eth1 bootproto=dhcp && ifup eth1 2>&1`
  if ! $?.success?
    raise "Unable to change setup network card"
  end
end

When /^I should see the power is "([^"]*)"$/ do |arg1|
  within(:xpath, "//*[@for='powerStatus']/..") do
    fail if not has_content?(arg1)
  end
end
