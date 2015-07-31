rpctest = XMLRPCChannelTest.new(ENV["TESTHOST"])

Given /^I am logged in via XML\-RPC\/channel as user "([^"]*)" and password "([^"]*)"$/ do |luser, password|
  assert(rpctest.login(luser, password))
end

When /^I create a repo with label "([^"]*)" and url "([^"]*)"$/ do |label, url|
  assert(rpctest.createRepo(label, url))
end

When /^I associate repo "([^"]*)" with channel "([^"]*)"$/ do |repoLabel, channelLabel|
  assert(rpctest.associateRepo(channelLabel, repoLabel))
end

When /^I create a channel with label "([^"]*)", name "([^"]*)", summary "([^"]*)", arch "([^"]*)" and parent "([^"]*)"$/ do |label, name, summary, arch, parent|
  assert_equal(rpctest.create(label, name, summary, arch, parent), 1)
end

When /^I delete the software channel with label "([^"]*)"$/ do |label|
  assert_equal(rpctest.delete(label), 1)
end

When /^I delete the repo with label "([^"]*)"$/ do |label|
  assert_equal(rpctest.deleteRepo(label), 1)
end

Then /^something should get listed with a call of listSoftwareChannels$/ do
  assert_equal(rpctest.getSoftwareChannelsCount() < 1, false)
end

Then /^"([^"]*)" should get listed with a call of listSoftwareChannels$/ do |label|
  assert(rpctest.verifyChannel(label))
end

Then /^"([^"]*)" should not get listed with a call of listSoftwareChannels$/ do |label|
  assert_equal(rpctest.verifyChannel(label), false)
end

Then /^"([^"]*)" should be the parent channel of "([^"]*)"$/ do |parent, child|
  assert(rpctest.isParentChannel(child, parent))
end

Then /^channel "([^"]*)" should have attribute "([^"]*)" from type "([^"]*)"$/ do |label, attr, type|
  ret = rpctest.getChannelDetails(label)
  assert(ret)
  assert_equal(ret[attr].class.to_s, type)
end 

Then /^channel "([^"]*)" should not have attribute "([^"]*)"$/ do |label, attr|
  ret = rpctest.getChannelDetails(label)
  assert(ret)
  assert_equal(ret.has_key?(attr), false)
end 
