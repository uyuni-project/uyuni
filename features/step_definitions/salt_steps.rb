# Copyright 2015 SUSE LLC

Given(/^the Salt Minion is configured$/) do
  File.rename("/etc/salt/minion", "/etc/salt/minion.orig")
  File.open("/etc/salt/minion", 'w') { |file| file.write("master: #{ENV['TESTHOST']}") }
  step %[I restart salt-minion]
end

When(/^I get the contents of the remote file "(.*?)"$/) do |filename|
  @output = sshcmd("cat #{filename}")
end

When(/^I delete the key of this client$/) do
  sshcmd("yes | salt-key -d #{$myhostname}")
end

When(/^I remove possible Salt Master key "(.*?)"$/) do |filename|
  if File.exist?(filename)
    File.delete(filename)
    puts "File #{filename} has been removed"
  end
end

When(/^I restart salt-minion$/) do
  system("systemctl restart salt-minion")
end

Then(/^the Salt Minion should be running$/) do
  out = `systemctl status salt-minion`
  unless $?.success?
    raise "salt-minion status: #{out}"
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
  @output = sshcmd("salt #{$myhostname} grains.get os")
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
