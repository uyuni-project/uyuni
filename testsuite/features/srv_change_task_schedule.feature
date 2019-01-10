# Copyright (c) 2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Change the schedule of a task

  Scenario: Change the schedule of task mgr-sync-refresh-default to weekly
    Given I am authorized as "admin" with password "admin"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "mgr-sync-refresh-default"
    And I check radio button "weekly"
    And I select "Friday" from "date_day_week"
    And I click on "Update Schedule"
    Then I should see a "Schedule mgr-sync-refresh-default has been updated." text
    When I follow "Task Schedules"
    And I follow "mgr-sync-refresh-default"
    Then I should see a "Friday" text
    And radio button "weekly" is checked

  Scenario: Change the schedule of task mgr-sync-refresh-default to monthly
    Given I am authorized as "admin" with password "admin"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "mgr-sync-refresh-default"
    And I check radio button "monthly"
    And I select "17" from "date_day_month"
    And I click on "Update Schedule"
    Then I should see a "Schedule mgr-sync-refresh-default has been updated." text
    When I follow "Task Schedules"
    And I follow "mgr-sync-refresh-default"
    Then radio button "monthly" is checked

  Scenario: Change the schedule of task mgr-sync-refresh-default back to daily
    Given I am authorized as "admin" with password "admin"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "mgr-sync-refresh-default"
    And I check radio button "daily"
    And I click on "Update Schedule"
    Then I should see a "Schedule mgr-sync-refresh-default has been updated." text
    When I follow "Task Schedules"
    And I follow "mgr-sync-refresh-default"
    Then radio button "daily" is checked
