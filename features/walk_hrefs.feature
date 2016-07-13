# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

@big
Feature: All HREFs are accessible
  In Order to verify that there are no broken links
  As a normal user
  I want to be able to walk all HREFs

  Scenario: Walking the HREFs
    Given I am authorized
    Then no link should be broken
