# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test the remote commands via salt
  In Order to test the remote commands via salt
  As an authorized user
  I want to verify that the remote commands function works

  Scenario: Run a remote command
    And I am authorized as "testing" with password "testing"
    Given I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    Then I enter command "ls -lha /etc"
    And I click on preview
    Then I should see my hostname
    And I click on run
    Then I wait for "3" seconds
    And I expand the results
    Then I should see "SuSE-release" in the command output

  Scenario: Run a remote command as non authorized user
    Given I am authorized as an example user with no roles
    And I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    And I click on preview
    Then I should not see my hostname
    And I can cleanup the no longer needed user

  Scenario: Run a remote command from the systems overview page
    Given I am authorized as "testing" with password "testing"
    And I follow "Systems"
    Then I follow this minion link
    When I follow "Remote Command" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      touch /root/12345
      """
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled successfully" text
    And "/root/12345" exists on the filesystem
    And I follow "Events" in the content area
    And I follow "History" in the content area
    Then I follow "Run an arbitrary script scheduled by testing" in the content area
    And I should see a "Script executed successfully." text
    And I should see a "Return Code: 0" text

