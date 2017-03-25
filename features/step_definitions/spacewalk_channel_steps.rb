# Copyright (c) 2014-17 SUSE
# Licensed under the terms of the MIT license.

arch = "x86_64"
When(/^I use spacewalk\-channel to add a valid child channel$/) do
  child_channel = "test-channel-#{arch}-child-channel"
  step %(I execute spacewalk\-channel and pass "--add -c #{child_channel} -u admin -p admin")
end

When(/^I use spacewalk\-channel to remove a valid child channel$/) do
  child_channel = "test-channel-#{arch}-child-channel"
  step %(I execute spacewalk\-channel and pass "--remove -c #{child_channel} -u admin -p admin")
end

Then(/^I want to see all valid child channels$/) do
  step %(I want to get "test-channel-#{arch}-child-channel")
end

Then(/^I wont see any of the valid child channels$/) do
  step %(I wont get "test-channel-#{arch}-child-channel")
end
