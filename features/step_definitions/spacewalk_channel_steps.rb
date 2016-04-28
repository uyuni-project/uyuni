# Copyright (c) 2014 SUSE
# Licensed under the terms of the MIT license.

# All these steps are needed *only* to support testing for two different archs!
When(/^I use spacewalk\-channel to add a valid child channel$/) do
  arch = `uname -m`
  arch.chomp!
  if arch != "x86_64"
    arch = "i586"
  end
  child_channel = "sles11-sp3-updates-#{arch}-child-channel"
  step %[I execute spacewalk\-channel and pass "--add -c #{child_channel} -u admin -p admin"]
end

When(/^I use spacewalk\-channel to remove a valid child channel$/) do
  arch = `uname -m`
  arch.chomp!
  if arch != "x86_64"
    arch = "i586"
  end
  child_channel = "sles11-sp3-updates-#{arch}-child-channel"
  step %[I execute spacewalk\-channel and pass "--remove -c #{child_channel} -u admin -p admin"]
end

Then(/^I want to see all valid child channels$/) do
  arch = `uname -m`
  arch.chomp!
  if arch != "x86_64"
    arch = "i586"
  end
  step %[I want to get "sles11-sp3-updates-#{arch}-child-channel"]
end

Then(/^I wont see any of the valid child channels$/) do
  arch = `uname -m`
  arch.chomp!
  if arch != "x86_64"
    arch = "i586"
  end
  step %[I wont get "sles11-sp3-updates-#{arch}-child-channel"]
end
