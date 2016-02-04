# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Explore the Minions page
  In Order to validate the minion onboarding page
  As an authorized user
  I want to verify all the minion key management features in the UI

  Background:
    Given I am authorized as "testing" with password "testing"

  Scenario: Completeness of the Minions page
    And I go to the minion onboarding page
    Then I should see a "All Minions" text
    And I should see a "Pending" text
    And I should see a "Rejected" text

  Scenario: Minion is visible in the Pending section
    Given this minion key is unaccepted
    And I go to the minion onboarding page
    Then I should see this client in the Pending section

  Scenario: Pending minion shows up in the Overview dashboard
    Given this minion key is unaccepted
# Temp work around
#   Then I should see a "Manage Pending Minions (1)" text
#   When I follow "Manage Pending Minions (1)"
    And I go to the minion onboarding page
    Then I should see this client in the Pending section

  Scenario: Reject and delete the pending minion key
    Given this minion key is unaccepted
    And I go to the minion onboarding page
    And I reject this client from the Pending section
    And we wait till Salt master sees this minion as rejected
    Then I should see this client in the Rejected section
    And I delete this client from the Rejected section
    Then I should not see this client as a Minion anywhere

  Scenario: Accepted minion shows up as a registered system
    Given this minion key is unaccepted
    And this minion is not registered in Spacewalk
    And I go to the minion onboarding page
    Then I should see this client in the Pending section
    When I accept this client's minion key
    And we wait till Salt master sees this minion as accepted
    # Registration takes a while
    And I wait for "15" seconds
    And I follow first "Systems"
    Then I should see this client as link

  Scenario: The minion communicates with the master
    # It takes a while before we can get the grains and registration is done
    Given that the master can reach this client
    When I get OS information of the Minion from the Master
    Then it should contain a "SLES" text
