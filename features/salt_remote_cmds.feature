# Copyright (c) 2015-16 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test the remote commands via salt
  In Order to test the remote commands via salt
  As an authorized user
  I want to verify that the remote commands function works

  Scenario: Run a remote command and expand results for sles-minion
    Given I am authorized as "testing" with password "testing"
    And I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    Then I enter command "ls -lha /etc"
    And I click on preview
    Then I should see "sle-minion" hostname
    And I click on run
    Then I wait for "3" seconds
    And I expand the results for "sle-minion"
    Then I should see "SuSE-release" in the command output for "sle-minion"

  Scenario: Run a remote command as non authorized user for sles-minion
    Given I am authorized as an example user with no roles
    And I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    And I click on preview
    Then I should not see "sle-minion" hostname
    And I can cleanup the no longer needed user

  Scenario: Run a remote command from the systems overview page sles-minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Remote Command"
    And I enter as remote command this script in
      """
      #!/bin/bash
      touch /root/12345
      """
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled successfully" text
    And "/root/12345" exists on the filesystem of "sle-minion"
    And I follow "Events"
    And I follow "History"
    Then I follow "Run an arbitrary script scheduled by admin" in the content area
    And I should see a "Script executed successfully." text
    And I should see a "Return Code: 0" text
