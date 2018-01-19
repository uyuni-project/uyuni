# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Reboot of a salt minion

  Scenario: Reboot a SLES Salt minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    And I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    Then I wait and check that "sle-minion" has rebooted
