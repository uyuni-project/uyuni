# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Given(/^I am on the groups page$/) do
  steps %(
    Given I am on the Systems page
    And I follow "System Groups" in the left menu
    )
end

When(/^I check this client$/) do
  step %(I check "#{$myhostname}" in the list)
end
