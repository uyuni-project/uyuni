# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_recurring_actions
Feature: Recurring Actions

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create a minion Recurring Action
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
    When I am on the "States" page of this "sle_minion"
    And I follow "Recurring States" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item details button
    Then I should see a "Every day at 00:00" text
    When I click on "Back"
    Then I should see a "Schedules" text

  Scenario: Edit a minion Recurring Action
    When I am on the "States" page of this "sle_minion"
    And I follow "Recurring States" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item edit button
    Then I should see a "Update Schedule" text
    When I enter "schedule_name_minion" as "scheduleName"
    And I check radio button "schedule-weekly"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    Then I should see a "schedule_name_minion" text
    And I should see a "Minion" text
    And I should see a "0 0 0 ? * 1" text

  Scenario: Create a System group for testing
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "Recurring-Action-test-group" as "name"
    And I enter "This is for testing" as "description"
    And I click on "Create Group"
    Then I should see a "System group Recurring-Action-test-group created." text

  Scenario: Create a group Recurring Action
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
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "States" in the content area
    And I follow "Recurring States" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item details button
    Then I should see a "Every day at 00:00" text
    When I click on "Back"
    Then I should see a "Schedules" text

  Scenario: Edit a group Recurring Action
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "States" in the content area
    And I follow "Recurring States" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item edit button
    Then I should see a "Update Schedule" text
    When I enter "schedule_name_group" as "scheduleName"
    And I check radio button "schedule-weekly"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    Then I should see a "schedule_name_group" text
    And I should see a "Group" text
    And I should see a "0 0 0 ? * 1" text

  Scenario: Create a yourorg Recurring Action
    When I follow the left menu "Home > My Organization > Recurring States"
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
    And I should see a "Organization" text
    And I should see a "0 0 0 ? * *" text

  Scenario: View a yourorg recurring actions details
    When I follow the left menu "Home > My Organization > Recurring States"
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item details button
    Then I should see a "Every day at 00:00" text
    When I click on "Back"
    Then I should see a "Schedules" text

  Scenario: Edit a yourorg Recurring Action
    When I follow the left menu "Home > My Organization > Recurring States"
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item edit button
    Then I should see a "Update Schedule" text
    When I enter "schedule_name_edit" as "scheduleName"
    And I check radio button "schedule-weekly"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    Then I should see a "schedule_name_edit" text
    And I should see a "Organization" text
    And I should see a "0 0 0 ? * 1" text

  Scenario: Delete a yourorg Recurring Action
    When I follow the left menu "Home > My Organization > Recurring States"
    Then I should see a "schedule_name_edit" text
    When I click the "schedule_name" item delete button
    Then I should see a "Delete Recurring State Schedule" text
    When I click on the red confirmation button
    Then I wait until I see "Schedule 'schedule_name_edit' has been deleted." text
    Then I should see a "No schedules created. Use Create to add a schedule" text

  Scenario: Create an admin org Recurring Action
    When I follow the left menu "Admin > Organizations"
    And I follow "SUSE Test" in the content area
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
    And I should see a "Organization" text
    And I should see a "0 0 0 ? * *" text

  Scenario: View an admin org recurring actions details
    When I follow the left menu "Admin > Organizations"
    And I follow "SUSE Test" in the content area
    And I follow "States" in the content area
    And I follow "Recurring States" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item details button
    Then I should see a "Every day at 00:00" text
    When I click on "Back"
    Then I should see a "Schedules" text

  Scenario: Edit an admin org Recurring Action
    When I follow the left menu "Admin > Organizations"
    And I follow "SUSE Test" in the content area
    And I follow "States" in the content area
    And I follow "Recurring States" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item edit button
    Then I should see a "Update Schedule" text
    When I enter "schedule_name_org" as "scheduleName"
    And I check radio button "schedule-weekly"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    Then I should see a "schedule_name_org" text
    And I should see a "Organization" text
    And I should see a "0 0 0 ? * 1" text

  Scenario: Check list of all actions
    When I follow the left menu "Schedule > Recurring States"
    Then I should not see a "Create" text
    And I should see a "schedule_name_minion" text
    And I should see a "Minion" text
    And I should see a "schedule_name_group" text
    And I should see a "Group" text
    And I should see a "schedule_name_org" text
    And I should see a "Organization" text

 Scenario: View details in list of all actions
    When I follow the left menu "Schedule > Recurring States"
    And I click the "schedule_name_minion" item details button
    Then I should see a "Every Sunday at 00:00" text
    And I should not see a "Highstate for" text in the content area
    When I click on "Edit"
    Then I should see a "Update Schedule" text
    When I click on "Back to list"
    Then I should see a "schedule_name_group" text

  Scenario: Edit in list of all actions
    When I follow the left menu "Schedule > Recurring States"
    And I wait until I see "schedule_name_org" text
    And I click the "schedule_name_org" item edit button
    Then I should see a "Update Schedule" text
    And I should not see a "Highstate for" text in the content area
    When I enter "schedule_name_edit" as "scheduleName"
    And I check radio button "schedule-monthly"
    And I click on "Update Schedule"
    Then I should not see a "schedule_name_org" text
    And I should see a "schedule_name_edit" text
    And I should see a "0 0 0 1 * ?" text

  Scenario: Delete from list of all actions
    When I follow the left menu "Schedule > Recurring States"
    When I click the "schedule_name_edit" item delete button
    Then I should see a "Delete Recurring State Schedule" text
    When I click on the red confirmation button
    Then I wait until I see "Schedule 'schedule_name_edit' has been deleted." text
    And I should not see a "Organization" text in the content area
    And I should see a "schedule_name_group" text

  Scenario: Delete a minion Recurring Action
    When I am on the "States" page of this "sle_minion"
    And I follow "Recurring States" in the content area
    Then I should see a "schedule_name_minion" text
    When I click the "schedule_name_minion" item delete button
    Then I should see a "Delete Recurring State Schedule" text
    When I click on the red confirmation button
    Then I wait until I see "Schedule 'schedule_name_minion' has been deleted." text
    Then I should see a "No schedules created. Use Create to add a schedule" text

  Scenario: Delete a group Recurring Action
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "States" in the content area
    And I follow "Recurring States" in the content area
    Then I should see a "schedule_name_group" text
    When I click the "schedule_name_group" item delete button
    Then I should see a "Delete Recurring State Schedule" text
    When I click on the red confirmation button
    Then I wait until I see "Schedule 'schedule_name_group' has been deleted." text
    Then I should see a "No schedules created. Use Create to add a schedule" text

  Scenario: Cleanup after running the tests
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "Delete Group"
    And I click on "Confirm Deletion"
    Then I should see a "Your organization has no system groups." text
