# Copyright 2015 SUSE LLC
require 'timeout'

Given(/^the Salt Minion is configured$/) do
  if File.exist?(filename)
    File.delete(filename)
    puts "File #{filename} has been removed"
  end
  File.write('/etc/salt/minion.d/master.conf', "master: #{ENV['TESTHOST']}\n")
  step %[I restart salt-minion]
end

Given(/^that the master can reach this client$/) do
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        @output = sshcmd("salt #{$myhostname} test.ping", ignore_err: true)
        break if @output[:stdout].include?($myhostname) &&
           @output[:stdout].include?('True')
        sleep(1)
      end
    end
  rescue Timeout::Error
      fail "Master can not communicate with the minion: #{@output[:stdout]}"
  end
end

When(/^I get the contents of the remote file "(.*?)"$/) do |filename|
  @output = sshcmd("cat #{filename}")
end

When(/^I delete the key of this client$/) do
  sshcmd("yes | salt-key -d #{$myhostname}")
end

When(/^I restart salt-minion$/) do
  system("systemctl restart salt-minion")
end

Then(/^the Salt Minion should be running$/) do
  begin
    Timeout.timeout(DEFAULT_TIMEOUT) do
      loop do
        out = `systemctl status salt-minion`
        break if $?.success?
        sleep(1)
      end
    end
  rescue Timeout::Error
    fail "salt-minion status: #{out}"
  end
end

When(/^I list unaccepted keys at Salt Master$/) do
  @output = sshcmd("salt-key --list unaccepted")
end

When(/^I list accepted keys at Salt Master$/) do
  @output = sshcmd("salt-key --list accepted")
end

Then(/^the list of the keys should contain this client's hostname$/) do
  fail if not @output[:stdout].include? $myhostname
end

When(/^I accept all Salt unaccepted keys$/) do
  sshcmd("yes | salt-key -A")
end

When(/^I get OS information of the Minion from the Master$/) do
  @output = sshcmd("salt #{$myhostname} grains.get osfullname")
end

Then(/^it should contain a "(.*?)" text$/) do |content|
  fail if not @output[:stdout].include? content
end

Then(/^salt\-api should be listening on local port (\d+)$/) do |port|
  fail if not sshcmd("ss -nta | grep #{port}")[:stdout].include? "127.0.0.1:#{port}"
end

Then(/^salt\-master should be listening on public port (\d+)$/) do |port|
  fail if not sshcmd("ss -nta | grep #{port}")[:stdout].include? "*:#{port}"
end

# Delete the system profile of this client
When(/^I delete this client's system profile/) do
  steps %[
    Given I am on the Systems overview page of this client
    Then I follow "Delete System"
    And I click on "Delete Profile"
  ]
end
