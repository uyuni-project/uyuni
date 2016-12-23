# Copyright (c) 2015-16 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test single system reboot confirm page

  Scenario: Go to the system reboot confirm page
    Given I am on the Systems overview page of this client
    When I follow "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    And I click on "Reboot system"
    # TODO: IMPLEMENT THAT FUNCTION
    And I wait and check that the system has rebooted
