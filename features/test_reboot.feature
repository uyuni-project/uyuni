# Copyright (c) 2013 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Test single system reboot confirm page

  Scenario: Go to the system reboot confirm page
    Given I am on the Systems overview page of this client
     When I follow "Schedule System Reboot"
     Then I should see a "System Reboot Confirmation" text
      And I should see a "Schedule Reboot" button
      # we do not schedule a reboot during a testrun
