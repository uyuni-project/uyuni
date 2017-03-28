# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test the state of salt remote commands
  In order to be able to rely on command states
  As an authorized user
  I want the remote commands to go through picked up and completed states

  Scenario: Schedule a long command on the sle minion
    Given I am authorized as "testing" with password "testing"
    And I am on the System Overview page
    When I follow this minion link
    And I follow "Remote Command" in the content area
    And I enter as remote command a script to watch a picked-up test file
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled successfully" text

  Scenario: Check that the long command gets picked up
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    And I wait for "1" seconds
    And I follow first "Run an arbitrary script scheduled by testing" in the content area
    Then I should see a "This action's status is: Picked Up" text

  Scenario: Check that the long command can complete
    Given I am on the Systems overview page of this "sle-minion"
    When I create picked-up test file on sle minion
    And I wait for "6" seconds
    When I follow "Events" in the content area
    And I follow "History" in the content area
    And I follow first "Run an arbitrary script scheduled by testing" in the content area
    Then I should see a "This action's status is: Completed" text
