# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Explore the Minions page
  In Order to validate that the Minions page works
  As an authorized user
  I want to see all Minions

  Background:
    Given I am authorized as "admin" with password "admin"

  Scenario: Completeness of Minions page
    When I follow "Systems"
    When I follow "Minions"
    Then I should see a "All Minions" text
    And I should see a "Pending" text
    And I should see a "Accepted" text
    And I should see a "Rejected" text

  Scenario: Minion is visible in the Pending Section
    Given this client hostname
    When I follow "Systems"
    When I follow "Minions"
    Then I should see a this client as a minion in the Pending section

  Scenario: Visible minion is moved to the Accepted Section
    Given this client hostname
    When I follow "Systems"
    When I follow "Minions"
    And when I accept minion
    Then I should see a this client as a minion in the Accepted section

  Scenario: Accepted minion is available
    Given this client hostname
    When I follow "Systems"
    When I follow "Minions"
    And when I see the contents of the minion
    And I should see a "/usr/bin/python" text
