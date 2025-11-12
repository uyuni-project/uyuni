# Copyright (c) 2023 SUSE LLC
# SPDX-License-Identifier: MIT

Feature: Restart the PostgreSQL service

  Scenario: Restart the PostgreSQL database service
    Given I am authorized for the "Admin" section
    When I restart the "uyuni-db" container 
    And I wait until "uyuni-db" container is active 
    # WORKAROUND for not having something to check for in the logs
    And I wait for "60" seconds
    And I am on the Systems overview page of this "sle_minion"
    And I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled by admin" is completed
    And I wait until I see "MinionActionExecutor" in file "/var/log/rhn/rhn_taskomatic_daemon.log" on "server"

