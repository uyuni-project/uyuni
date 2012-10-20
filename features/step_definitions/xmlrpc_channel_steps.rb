rpctest = XMLRPCChannelTest.new(ENV["TESTHOST"])

Given /^I am logged in via XML\-RPC\/channel as user "([^"]*)" and password "([^"]*)"$/ do |luser, password|
  fail if not rpctest.login(luser, password)
end

When /^I create a repo with label "([^"]*)" and url "([^"]*)"$/ do |label, url|
  ret = rpctest.createRepo(label, url)
  fail if not ret
end

When /^I associate repo "([^"]*)" with channel "([^"]*)"$/ do |repoLabel, channelLabel|
  ret = rpctest.associateRepo(channelLabel, repoLabel)
  fail if not ret
end

When /^I create a channel with label "([^"]*)", name "([^"]*)", summary "([^"]*)", arch "([^"]*)" and parent "([^"]*)"$/ do |label, name, summary, arch, parent|
  ret = rpctest.create(label, name, summary, arch, parent)
  fail if ret != 1
end

When /^I delete the software channel with label "([^"]*)"$/ do |label|
  ret = rpctest.delete(label)
  fail if ret != 1
end

When /^I delete the repo with label "([^"]*)"$/ do |label|
  ret = rpctest.deleteRepo(label)
  fail if ret != 1
end

Then /^something should get listed with a call of listSoftwareChannels$/ do
  fail if rpctest.getSoftwareChannelsCount() < 1
end

Then /^"([^"]*)" should get listed with a call of listSoftwareChannels$/ do |label|
  fail if not rpctest.verifyChannel(label)
end

Then /^"([^"]*)" should not get listed with a call of listSoftwareChannels$/ do |label|
  fail if rpctest.verifyChannel(label)
end

Then /^"([^"]*)" should be the parent channel of "([^"]*)"$/ do |parent, child|
  fail if not rpctest.isParentChannel(child, parent)
end

Then /^channel "([^"]*)" should have attribute "([^"]*)" from type "([^"]*)"$/ do |label, attr, type|
  ret = rpctest.getChannelDetails(label)
  fail if not ret
  c = ret[attr].class.to_s
  #print "CLASS: #{c}\n"
  fail if c != type
end 
