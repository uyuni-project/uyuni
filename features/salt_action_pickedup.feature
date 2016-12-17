# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test the remote commands via salt
  In Order to test action picked up state
  As an authorized user
  I want to verify that the remote command show status picked up as long as the command is running

  Scenario: Run a remote command from the systems overview page
    Given I am authorized as "testing" with password "testing"
    And I follow "Systems"
    Then I follow this minion link
    When I follow "Remote Command" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      while [ ! -f /tmp/PICKED-UP.test ]
      do
        sleep 1
      done
      rm /tmp/PICKED-UP.test
      """
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled successfully" text
    And I check status "Picked Up" with spacecmd on "sle-minion"
    And I create picked-up test file on sle minion
    And I check status "Completed" with spacecmd on "sle-minion"
