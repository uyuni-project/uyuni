# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Restart the PostgreSQL service

  Scenario: Restart the PostgreSQL database service
    Given I am authorized for the "Admin" section
    When I restart the "postgresql.service" service on "server"
    And I wait until "postgresql" service is active on "server"
    And I am on the Systems overview page of this "sle_minion"
    And I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled by admin" is completed

