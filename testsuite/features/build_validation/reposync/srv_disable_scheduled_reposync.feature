# Copyright (c) 2019-2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Delete the scheduled task for mgr-sync-refresh

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Delete scheduled reposyncs
    When I follow the left menu "Admin > Task Schedules"
    And I follow "mgr-sync-refresh-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    And I click on "Delete Schedule"
