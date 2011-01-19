# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

# features/running.feature
Feature: The host is running
  In Order to see if the host is running
  As a normal user
  I want to see the start page
  Scenario: Accessing home page
    Given I am not authorized
    When I go to the home page
    Then I should see something
