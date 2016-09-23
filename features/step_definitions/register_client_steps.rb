# Copyright (c) 2010-2016 Novell, Inc.
# Licensed under the terms of the MIT license.
Given(/^I am root$/) do
  user, code = $client.run("whoami")
  if user.strip != "root"
    puts  "user on client was #{user}"
    raise "You are not root!"
  end
end

Given(/^I am on the Systems overview page of this client$/) do
  steps %(
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow this client link
    )
end

Given(/^I update the profile of this client$/) do
  $client.run("rhn-profile-sync", true, 500, 'root')
end

When(/^I register using "([^"]*)" key$/) do |arg1|
  regurl = "http://#{ENV['TESTHOST']}/XMLRPC"
  command = "rhnreg_ks --force --serverUrl=#{regurl} --activationkey=#{arg1}"
  $client.run(command, true, 500, 'root')
end

When(/^I register using an activation key$/) do
  arch, _code = $client.run('uname -m')
  arch.chomp!
  step %(I register using "1-SUSE-DEV-#{arch}" key)
end

Then(/^I should see this client in spacewalk$/) do
  steps %(
    Given I am on the Systems page
    Then I should see this client as link
    )
end

Then(/^this client should appear in spacewalk$/) do
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        begin
          steps %(
            Given I am on the Systems page
            Then I should see this client as link
                    )
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
  step %(I should see a "#{$client_hostname}" link)
end

When(/^I follow this client link$/) do
  step %(I follow "#{$client_hostname}")
end

Then(/^config-actions are enabled$/) do
  unless  file_exist($client, '/etc/sysconfig/rhn/allowed-actions/configfiles/all')
    raise "config actions are disabled: /etc/sysconfig/rhn/allowed-actions/configfiles/all does not exist on client"
  end
end

Then(/^remote-commands are enabled$/) do
  unless file_exist($client, '/etc/sysconfig/rhn/allowed-actions/script/run')
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
