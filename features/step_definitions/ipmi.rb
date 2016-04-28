# COPYRIGHT 2015 SUSE LLC

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
      if !has_content?(arg1)
        find(:xpath, '//button[@value="Get status"]').click
      else
        break
      end
      sleep 3
    end
    fail unless has_content?(arg1)
  end
end
