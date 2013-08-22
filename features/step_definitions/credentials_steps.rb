# Copyright (c) 2010-2012 Novell, Inc.
# Licensed under the terms of the MIT license.

Given /^I am on the Credentials page$/ do
  step 'I am authorized as "testing" with password "testing"'
  step 'I follow "Your Account"'
  step 'I follow "Credentials"'
end

