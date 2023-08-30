# Copyright (c) 2022-2023 SUSE LLC
# Licensed under the terms of the MIT License.
#
# This feature can cause failures in the following features:
# All following features
# If the server fails to reboot properly
# or the cleanup fails and renders the server unreachable.

Feature: Reconfigure the server's hostname
  As admin user
  In order to change the server's hostname
  I want to use the tool spacewalk-hostname-rename.

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Change hostname and reboot server
    When I change the server's short hostname from hosts and hostname files
    And I reboot the server through SSH
    And I run spacewalk-hostname-rename command on the server

  Scenario: Do some minimal smoke test on the renamed server
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Remote Command" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      date
      """
    And I click on "Schedule"
    And I follow "Events" in the content area
    And I follow "Pending" in the content area
    And I wait at most 180 seconds until I do not see "Remote Command on" text, refreshing the page
    And I follow "History" in the content area
    And I wait until I see the event "Remote Command on" completed during last minute, refreshing the page

  Scenario: Change hostname back and reboot server
    When I change back the server's hostname
    And I reboot the server through SSH
    And I run spacewalk-hostname-rename command on the server
