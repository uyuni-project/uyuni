# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Test SUSE Manager generated metadata
  In Order to validate the SUSE Manager generated metadata
  As an local user
  I want to see several xml elements and attributes in the metadata

  Scenario: Check pre requires
    Given I am root
     When I refresh the metadata
     Then I should have 'pre="1"' in the metadata

    Scenario: Check for empty epoch
        Given I am root
        When I refresh the metadata
        Then I should not have 'epoch=""' in the metadata

