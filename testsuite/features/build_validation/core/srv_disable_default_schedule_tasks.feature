# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Remove schedule tasks to manually trigger them when we consider the best moment

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Delete scheduled reposyncs
    When I follow the left menu "Admin > Task Schedules"
    And I follow "mgr-sync-refresh-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    And I click on "Delete Schedule"

  Scenario: Delete scheduled sync of custom channels
    When I follow the left menu "Admin > Task Schedules"
    And I follow "channel-repodata-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    And I click on "Delete Schedule"

  Scenario: Delete scheduled sync of cobbler
    When I follow the left menu "Admin > Task Schedules"
    And I follow "cobbler-sync-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    And I click on "Delete Schedule"

  Scenario: Delete scheduled errata cache
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    And I click on "Delete Schedule"

  Scenario: Delete scheduled errata queue
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-queue-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    And I click on "Delete Schedule"
