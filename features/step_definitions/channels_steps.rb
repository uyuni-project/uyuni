# Copyright (c) 2010-2017 SUSE LINUX
# Licensed under the terms of the MIT license.

Then(/^I should see package "([^"]*)"$/) do |package|
  fail unless has_xpath?("//div[@class=\"table-responsive\"]/table/tbody/tr/td/a[contains(.,'#{package}')]")
end

Given(/^I am on the manage software channels page$/) do
  step %(I am authorized as "testing" with password "testing")
  visit("https://#{$server_fullhostname}/rhn/channels/manage/Manage.do")
end

Given(/^metadata generation finished for "([^"]*)"$/) do |channel|
  for c in 0..60
      begin
          sshcmd('ls /var/cache/rhn/repodata/#{channel}/updateinfo.xml.gz')
      rescue
          sleep 2
      else
          break
      end
  end
end

When(/^I push package "([^"]*)" into "([^"]*)" channel$/) do |arg1, arg2|
  srvurl = "http://#{ENV['TESTHOST']}/APP"
  command = "rhnpush --server=#{srvurl} -u admin -p admin --nosig -c #{arg2} #{arg1} "
  $server.run(command, true, 500, 'root')
  $server.run("ls -lR /var/spacewalk/packages", true, 500, 'root')
end

Then(/^I should see package "([^"]*)" in channel "([^"]*)"$/) do |pkg, channel|
  steps %(
    And I follow "Channels > All" in the left menu
    And I follow "#{channel}"
    And I follow "Packages"
    Then I should see package "#{pkg}"
    )
end
