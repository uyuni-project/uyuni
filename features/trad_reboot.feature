# Copyright (c) 2015-16 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Reboot a traditional client

  Scenario: Reboot a SLES tradional client
    Given I am on the Systems overview page of this "sle-client"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    And I run "rhn_check -vvv" on "sle-client"
    Then I wait and check that "sle-client" has rebooted
