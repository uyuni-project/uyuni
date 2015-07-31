# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Explore the Minions page
  In Order to validate that the Minions page works
  As an authorized user
  I want to see all Minions

  Scenario: Completeness of Minions page
    Given I am authorized
    When I follow "Systems"
    When I follow "Minions"
    Then I should see a "All Minions" text
