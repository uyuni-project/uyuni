# Copyright 2015 SUSE LLC
$testsuite_hostname = `hostname -f`.chomp

When(/^I get a content of a file "(.*?)"$/) do |filename|
  $output = sshcmd("cat #{filename}")
end

When(/^I issue local command "(.*?)"$/) do |command|
  $local_output = `#{command}`
  $output = {:stdout => $local_output}
end

When(/^I delete key of the testsuite hostname$/) do
  sshcmd("yes | salt-key -d #{$testsuite_hostname}")
  `rcsalt-minion restart`

  puts "Waiting for the longest RSA key re-issue (10 secons)"
  # Longest key re-issue is 10 seconds. We sleep here 15.
  sleep(15)
end

When(/^I ping client machine from the Master$/) do
  $output = sshcmd("salt #{$testsuite_hostname} test.ping")
end

When(/^I get OS information of the client machine from the Master$/) do
  $output = sshcmd("salt #{$testsuite_hostname} grains.get os")
end

Then(/^it should contain "(.*?)" text$/) do |content|
  fail if not $output[:stdout].include? content
end

Then(/^it should contain testsuite hostname$/) do
  fail if not $output[:stdout].include? $testsuite_hostname
end

Then(/^the Salt rest\-api should be listening on local port (\d+)$/) do |port|
  fail if not sshcmd("ss -nta | grep #{port}")[:stdout].include? "127.0.0.1:#{port}"
end

Then(/^the salt\-master should be listening on public port (\d+)$/) do |port|
  fail if not sshcmd("ss -nta | grep #{port}")[:stdout].include? "*:#{port}"
end
