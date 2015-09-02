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

  Scenario: Minion is available in the Overview dashbord
    Given this client hostname
    Then I should see a "Manage Pending Minions (1)" link
    Then the link "Manage Pending Minions (1)" should point to "/rhn/manager/minions" URI

  Scenario: Visible minion is moved to the Accepted Section
    Given this client hostname
    When I follow "Systems"
    When I follow "Minions"
    And I accept minion
    Then I should see a this client as a minion in the Accepted section

  Scenario: Accepted minion is available
    Given this client hostname
    When I follow "Systems"
    When I follow "Minions"
    And I see the contents of the minion
    And I should see a "/usr/bin/python" text

  Scenario: Delete accepted minion
    Given this client hostname
    When I follow "Systems"
    When I follow "Minions"
    And I delete this client as a minion from the Accepted section
    Then I should not see this client as a minion anywhere

  Scenario: Reject pending minion
    Given this client hostname
    When I restart a minion of this client
    And I refresh "Minions"
    And I reject this client as a minion from the Pending section
    Then I should see this client as a minion in the Rejected section

  Scenario: Remove rejected minion
    Given this client hostname
    When I follow "Systems"
    When I follow "Minions"
    And I delete this client as a minion from the Rejected section
    And I restart a minion of this client
    And I refresh "Minions"
    Then I should see a this client as a minion in the Pending section

