# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

# features/walk_hrefs.feature
@big
Feature: All HREFs are accessible
  In Order to verify that there are no broken links
  As a normal user
  I want to be able to walk all HREFs
  Scenario: Walking the HREFs
    Given I am authorized
    Then no link should be broken
