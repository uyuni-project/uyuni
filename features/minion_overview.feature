# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Explore the Minions page
  In Order to validate that the Minions page works
  As an authorized user
  I want to see all Minions

  Background:
    Given I am authorized as "admin" with password "admin"

  Scenario: Minion is available in the Overview dashbord
    Given this client hostname
    Then I should see a "Manage Pending Minions (1)" link
    Then I follow "Manage Pending Minions (1)"
    Then I should see a "Pending" text
