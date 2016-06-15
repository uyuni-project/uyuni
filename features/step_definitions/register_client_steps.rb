#Copyright (c) 2010-2016 Novell, Inc.
# Licensed under the terms of the MIT license.
Given(/^I am root$/) do
  user, local, remote, code = $client.test_and_store_results_together("whoami", "root", 500)
  if  user.strip != "root"
    puts  "user on client was #{user}" 
    raise "You are not root!"
  end
end



Given(/^I am on the Systems overview page of this client$/) do
  steps %[
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow this client link
  ]
end

Given(/^I update the profile of this client$/) do
  local, remote, code = $client.test_and_print_results("rhn-profile-sync", "root", 500)
  if code != 0
    raise "Profile sync failed"
  end
end

When(/^I register using "([^"]*)" key$/) do |arg1|
  #FIXME: for moment this exist? first time it doesn't exist. remove systemid file
  "rm -f /etc/sysconfig/rhn/systemid"
  regurl = "http://#{ENV['TESTHOST']}/XMLRPC"
  command ="rhnreg_ks --serverUrl=#{regurl} --activationkey=#{arg1}"
  out , local, remote, code = $client.test_and_store_results_together(command, "root", 600)
  puts out
  if code != 0
    out , local, remote, code = $client.test_and_store_results_together("cat /var/log/up2date", "root", 600)
    puts out
    raise "Profil registration failed"
  end
  puts "registration client ok ! #{out}"
end

When(/^I register using an activation key$/) do
  arch, local, remote, code = $client.test_and_store_results_together("uname -m", "root", 600)
  step %[I register using "1-SUSE-DEV-#{arch}" key]
end

Then(/^I should see this client in spacewalk$/) do
  steps %[
    Given I am on the Systems page
    Then I should see this client as link
  ]
end

Then(/^this client should appear in spacewalk$/) do
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        begin
          steps %[
            Given I am on the Systems page
            Then I should see this client as link
          ]
          break
        rescue Capybara::ElementNotFound
          sleep(1)
        end
      end
    end
  rescue Timeout::Error
      fail "The minion never showed up in Spacewalk"
  end
end

Then(/^I should see this client as link$/) do
  step %[I should see a "#{$client_hostname}" link]
end

When(/^I follow this client link$/) do
  step %[I follow "#{$client_hostname}"]
end

Then(/^config-actions are enabled$/) do
  if not File.exist?('/etc/sysconfig/rhn/allowed-actions/configfiles/all')
    raise "config actions are disabled: /etc/sysconfig/rhn/allowed-actions/configfiles/all does not exist"
  end
end

Then(/^remote-commands are enabled$/) do
  if not File.exist?('/etc/sysconfig/rhn/allowed-actions/script/run')
    raise "remote-commands are disabled: /etc/sysconfig/rhn/allowed-actions/script/run does not exist"
  end
end

When(/^I wait for the data update$/) do
  for c in 0..6
    if page.has_content?(debrand_string("Software Updates Available"))
      break
    end
    sleep 30
    visit current_url
  end
end
