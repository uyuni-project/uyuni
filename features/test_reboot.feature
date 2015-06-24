# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test single system reboot confirm page

  Scenario: Go to the system reboot confirm page
    Given I am on the Systems overview page of this client
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    # we do not schedule a reboot during a testrun
    And I should see a "Reboot system" button
