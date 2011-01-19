# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Sleep for X seconds
#
When /^I wait for "(\d+)" seconds$/ do |arg1|
  sleep(arg1.to_i)
end

When /^I run rhn_check on this client$/ do
  `rhn_check`
end

