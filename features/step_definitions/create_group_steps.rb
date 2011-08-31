# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Given /^I am on the groups page$/ do
    Given "I am on the Systems page"
    Given "I follow \"System Groups\" in the left menu"
end

When /^I check this client$/ do
  When 'I check "' + $myhostname + '" in the list'
end

Then /^I should see this client as a link$/ do
  within(:xpath, "//td[@class='page-content']") do
    fail if not find_link("#{$myhostname}").visible?
  end
end

