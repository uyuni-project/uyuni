# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Explore the Minions page
  In Order to validate the minion onboarding page
  As an authorized user
  I want to verify all the minion key management features in the UI

  Background:
    Given I am authorized as "admin" with password "admin"

  Scenario: Completeness of the Minions page
    When I follow "Systems"
    And I follow "Minions"
    Then I should see a "All Minions" text
    And I should see a "Pending" text
    And I should see a "Rejected" text

  Scenario: Minion is visible in the Pending section
    When I follow "Systems"
    And I follow "Minions"
    Then I should see this client in the Pending section

  Scenario: Pending minion shows up in the Overview dashboard
    Then I should see a "Manage Pending Minions (1)" link
    When I follow "Manage Pending Minions (1)"
    Then I should see this client in the Pending section

  Scenario: Reject pending minion key
    When I follow "Systems"
    And I follow "Minions"
    And I reject this client from the Pending section
    Then I should see this client in the Rejected section

  Scenario: Delete rejected minion key
    When I follow "Systems"
    And I follow "Minions"
    And I delete this client from the Rejected section
    Then I should not see this client as a Minion anywhere

  Scenario: Accepted minion shows up as a registered system
    When I restart salt-minion
    And I wait for "2" seconds
    And I follow "Systems"
    And I follow "Minions"
    Then I should see this client in the Pending section
    When I accept this client's minion key
    # Registration takes a while
    And I wait for "15" seconds
    And I follow first "Systems"
    Then I should see this client as link
