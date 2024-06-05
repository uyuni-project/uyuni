# Copyright (c) 2019-2024 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Delete the scheduled task for mgr-sync-refresh

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Delete scheduled mgr-sync-refresh task
    When I follow the left menu "Admin > Task Schedules"
    And I follow "mgr-sync-refresh-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    # Workaround https://bugzilla.suse.com/show_bug.cgi?id=1225740
    And I click on "Delete Schedule"

