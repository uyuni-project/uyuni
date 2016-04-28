# Copyright (c) 2010-2012 Novell, Inc.
# Licensed under the terms of the MIT license.

Given(/^I am on the Credentials page$/) do
  steps %[
    When I am authorized as "testing" with password "testing"
    And I follow "Your Account"
    And I follow "Credentials"
  ]
end
