# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Recurring Actions

  Scenario: Create a minion Recurring Action
    Given I am authorized as "admin" with password "admin"
    When I am on the "States" page of this "sle_minion"
    And I follow "Recurring States" in the content area
    Then I should see a "No schedules created. Use Create to add a schedule" text
    When I click on "Create"
    Then I should see a "Schedule Name" text
    When I enter "schedule_name" as "scheduleName"
    And I check radio button "schedule-daily"
    And I pick "00:00" as time from "time-daily_time"
    And I click on the "disabled" toggler
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    Then I should see a "schedule_name" text
    And I should see a "Minion" text
    And I should see a "0 0 0 ? * *" text

  Scenario: View a minion recurring actions details
    Given I am authorized as "admin" with password "admin"
    When I am on the "States" page of this "sle_minion"
    And I follow "Recurring States" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" recurring action details button
    Then I should see a "Every day at 00:00" text
    When I click on "Back"
    Then I should see a "Schedules" text

  Scenario: Edit a minion Recurring Action
    Given I am authorized as "admin" with password "admin"
    When I am on the "States" page of this "sle_minion"
    And I follow "Recurring States" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" recurring action edit button
    Then I should see a "Update Schedule" text
    When I enter "schedule_name_minion" as "scheduleName"
    And I check radio button "schedule-weekly"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    Then I should see a "schedule_name_minion" text
    And I should see a "Minion" text
    And I should see a "0 0 0 ? * 1" text

  Scenario: Create a System group for testing
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "Recurring-Action-test-group" as "name"
    And I enter "This is for testing" as "description"
    And I click on "Create Group"
    Then I should see a "System group Recurring-Action-test-group created." text

  Scenario: Create a group Recurring Action
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "States" in the content area
    And I follow "Recurring States" in the content area
    Then I should see a "No schedules created. Use Create to add a schedule" text
    When I click on "Create"
    Then I should see a "Schedule Name" text
    When I enter "schedule_name" as "scheduleName"
    And I check radio button "schedule-daily"
    And I pick "00:00" as time from "time-daily_time"
    And I click on the "disabled" toggler
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    Then I should see a "schedule_name" text
    And I should see a "Group" text
    And I should see a "0 0 0 ? * *" text

  Scenario: View a group recurring actions details
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "States" in the content area
    And I follow "Recurring States" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" recurring action details button
    Then I should see a "Every day at 00:00" text
    When I click on "Back"
    Then I should see a "Schedules" text

  Scenario: Edit a group Recurring Action
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "States" in the content area
    And I follow "Recurring States" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" recurring action edit button
    Then I should see a "Update Schedule" text
    When I enter "schedule_name_group" as "scheduleName"
    And I check radio button "schedule-weekly"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    Then I should see a "schedule_name_group" text
    And I should see a "Group" text
    And I should see a "0 0 0 ? * 1" text
