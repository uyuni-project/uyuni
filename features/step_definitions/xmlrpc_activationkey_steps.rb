# COPYRIGHT 2015 SUSE LLC
rpctest = XMLRPCActivationKeyTest.new(ENV["TESTHOST"])
key = nil

Given(/^I am logged in via XML\-RPC\/activationkey as user "([^"]*)" and password "([^"]*)"$/) do |luser, password|
  fail if !rpctest.login(luser, password)
end

When(/^I create an AK with id "([^"]*)", description "([^"]*)" and limit of (\d+)$/) do |id, dscr, limit|
  key = rpctest.createKey(id, dscr, limit)
  fail if key.nil?
end

Then(/^I should get it listed with a call of listActivationKeys\.$/) do
  fail if !rpctest.verifyKey(key)
end

When(/^I call listActivationKeys I should get some\.$/) do
  fail if rpctest.getActivationKeysCount() < 1
end

Then(/^I should get key deleted\.$/) do
  fail if !rpctest.deleteKey(key)
  fail if rpctest.verifyKey(key)
end

When(/^I add config channels to a newly created key$/) do
  fail if rpctest.getConfigChannelsCount(key) > 0
end

When(/^I add config channels "([^"]*)" to a newly created key$/) do |channelName|
  fail if rpctest.addConfigChannel(key, channelName) < 1
end

Then(/^I have to see a new config channel "([^"]*)"$/) do |channelName|
  pending # express the regexp above with the code you wish you had
end

When(/^I add a child channel "([^"]*)"$/) do |channelName|
  pending # express the regexp above with the code you wish you had
end

Then(/^I can see config child has been added\.$/) do
  pending # express the regexp above with the code you wish you had
end

# Details
When(/^I call activationkey\.setDetails\(\) to the key$/) do
  fail if !rpctest.setDetails(key)
end

Then(/^I have to see them by calling activationkey\.getDetails\(\)$/) do
  fail if !rpctest.getDetails(key)
end

# ToDO
#   Scenario: Channels
#     Given I am logged in via XML-RPC/activationkey as user "admin" and password "admin"
#     When I add config channels "foo" to a newly created key
#     Then I have to see a new config channel "foo"
#
#     When I add a child channel "bar"
#     Then I can see config child has been added.
