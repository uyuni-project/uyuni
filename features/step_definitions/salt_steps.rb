# Copyright 2015 SUSE LLC

Given(/^this client hostname$/) do
  @this_client_hostname = `hostname -f`.chomp
end

When(/^I get a content of a file "(.*?)"$/) do |filename|
  @output = sshcmd("cat #{filename}")
end

When(/^I delete key of this client$/) do
  sshcmd("yes | salt-key -d #{@this_client_hostname}")
  system("rcsalt-minion restart")
  puts "Waiting for the longest RSA key re-issue (10 secons)"
  # Longest key re-issue is 10 seconds. We sleep here 15.
  sleep(15)
end

When(/^I remove possible Salt Master key "(.*?)"$/) do |filename|
  if File.exist?(filename)
    File.delete(filename)
    puts "File #{filename} has been removed"
  end
end

When(/^when I restart Salt Minion$/) do
  system("rcsalt-minion stop")
  system("rcsalt-minion start")
end

Then(/^the Salt Minion should be running$/) do
  fail if not `rcsalt-minion status | grep Active`.chomp.include? "active (running) since"
end

When(/^I list unaccepted keys at Salt Master$/) do
  @output = sshcmd("salt-key --list unaccepted")
end

When(/^I list accepted keys at Salt Master$/) do
  @output = sshcmd("salt-key --list accepted")
end

Then(/^the list of the keys should contain this client hostname$/) do
  fail if not @output[:stdout].include? @this_client_hostname
end

When(/^I accept all Salt unaccepted keys$/) do
  sshcmd("yes | salt-key -A")
end

When(/^I ping client machine from the Master$/) do
  @output = sshcmd("salt #{@this_client_hostname} test.ping")
end

When(/^I get OS information of the client machine from the Master$/) do
  sleep(15)
  @output = sshcmd("salt #{@this_client_hostname} grains.get os")
end

Then(/^it should contain "(.*?)" text$/) do |content|
  fail if not @output[:stdout].include? content
end

Then(/^it should contain testsuite hostname$/) do
  fail if not @output[:stdout].include? @this_client_hostname
end

Then(/^the Salt rest\-api should be listening on local port (\d+)$/) do |port|
  fail if not sshcmd("ss -nta | grep #{port}")[:stdout].include? "127.0.0.1:#{port}"
end

Then(/^the salt\-master should be listening on public port (\d+)$/) do |port|
  fail if not sshcmd("ss -nta | grep #{port}")[:stdout].include? "*:#{port}"
end
