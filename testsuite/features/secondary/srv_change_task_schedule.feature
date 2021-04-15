# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_recurring_actions
Feature: Change the schedule of a task

  Scenario: Change the schedule of task sandbox-cleanup-default to weekly
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Task Schedules"
    And I follow "sandbox-cleanup-default"
    And I check radio button "weekly"
    And I select "Friday" from "date_day_week"
    And I click on "Update Schedule"
    Then I should see a "Schedule sandbox-cleanup-default has been updated." text
    When I follow the left menu "Admin > Task Schedules"
    And I follow "sandbox-cleanup-default"
    Then I should see a "Friday" text
    And radio button "weekly" is checked

  Scenario: Change the schedule of task sandbox-cleanup-default to monthly
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Task Schedules"
    And I follow "sandbox-cleanup-default"
    And I check radio button "monthly"
    And I select "17" from "date_day_month"
    And I click on "Update Schedule"
    Then I should see a "Schedule sandbox-cleanup-default has been updated." text
    When I follow the left menu "Admin > Task Schedules"
    And I follow "sandbox-cleanup-default"
    Then radio button "monthly" is checked

  Scenario: Change the schedule of task sandbox-cleanup-default back to daily
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Task Schedules"
    And I follow "sandbox-cleanup-default"
    And I check radio button "daily"
    And I click on "Update Schedule"
    Then I should see a "Schedule sandbox-cleanup-default has been updated." text
    When I follow the left menu "Admin > Task Schedules"
    And I follow "sandbox-cleanup-default"
    Then radio button "daily" is checked
