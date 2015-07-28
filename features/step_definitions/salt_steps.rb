# Copyright 2015 SUSE LLC

When(/^I get a content of a file "(.*?)"$/) do |filename|
  $output = sshcmd("cat #{filename}")
end

Then(/^it should contain "(.*?)" text$/) do |content|
  fail if not $output[:stdout].include? content
end
