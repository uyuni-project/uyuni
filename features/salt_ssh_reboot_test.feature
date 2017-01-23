# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test a reboot on a salt minion (ssh-managed)
         The reboot is scheduled by suse-manager 

  Scenario: Reboot a salt minion (ssh-managed) (sle)
    Given I am on the Systems overview page of this "ssh-minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    And I click on "Reboot system"
    Then I wait and check that "ssh-minion" has rebooted
