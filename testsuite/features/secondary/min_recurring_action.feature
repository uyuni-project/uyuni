# Copyright (c) 2020-2023 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
@scope_recurring_actions
Feature: Recurring Actions

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create a minion Custom state Recurring Action
    When I am on the "Recurring Actions" page of this "sle_minion"
    Then I should see a "No schedules created. Use Create to add a schedule" text
    When I click on "Create"
    Then I should see a "Action Type" text
    When I select "Custom state" from "actionTypeDescription"
    Then I wait until I see "Configure states to execute" text
    And I check "Sync Modules-cbox"
    And I click on "Save Changes"
    Then I should see a "Edit State Ranks" text
    And I should see a "Sync Modules" text
    And I should not see a "Sync States" text
    When I click on "Confirm"
    Then I should see a "State assignments have been saved" text
    And I enter "custom_state_schedule_name" as "scheduleName"
    And I check radio button "schedule-daily"
    And I enter 1 minutes from now as "time-daily_time"
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    Then I should see a "custom_state_schedule_name" text
    And I should see a "Minion" text
    And I follow "Events"
    And I follow "History"
    And I wait until I see the event "Apply recurring states [util.syncmodules] scheduled by admin" completed during last minute, refreshing the page
    And I follow the event "Apply recurring states [util.syncmodules] scheduled by admin" completed during last minute
    Then I should see a "SLS: util.syncmodules" text
    And I should not see a "SLS: util.syncstates" text

  Scenario: Edit a minion Custom state Recurring Action
    When I am on the "Recurring Actions" page of this "sle_minion"
    Then I should see a "custom_state_schedule_name" text
    When I click the "custom_state_schedule_name" item edit button
    Then I should see a "Update Schedule" text
    And I should see a "Reorder" text
    When I uncheck "Sync Modules-cbox"
    And I click on "Save Changes"
    Then I should see a "There are no states assigned" text
    When I click on "Confirm"
    Then I should see a "State configuration must not be empty" text
    When I uncheck "Sync Modules-cbox"
    And I check "Sync States-cbox"
    And I click on "Save Changes"
    Then I should see a "Sync States" text
    And I should not see a "Sync Modules" text
    When I click on "Confirm"
    Then I should see a "State assignments have been saved" text
    When I enter "custom_state_schedule_name_changed" as "scheduleName"
    And I enter 1 minutes from now as "time-daily_time"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    Then I should see a "custom_state_schedule_name_changed" text
    And I should see a "Minion" text
    And I follow "Events"
    And I follow "History"
    And I wait until I see the event "Apply recurring states [util.syncstates] scheduled by admin" completed during last minute, refreshing the page
    And I follow the event "Apply recurring states [util.syncstates] scheduled by admin" completed during last minute
    Then I should see a "SLS: util.syncstates" text
    And I should not see a "SLS: util.syncmodules" text

  Scenario: Cleanup: Delete a minion Custom state Recurring Action
    When I am on the "Recurring Actions" page of this "sle_minion"
    Then I should see a "custom_state_schedule_name_changed" text
    When I click the "custom_state_schedule_name_changed" item delete button
    Then I should see a "Delete Recurring Action Schedule" text
    When I click on the red confirmation button
    Then I wait until I see "Schedule 'custom_state_schedule_name_changed' has been deleted." text

  Scenario: Create a minion Highstate Recurring Action
    When I am on the "Recurring Actions" page of this "sle_minion"
    Then I should see a "No schedules created. Use Create to add a schedule" text
    When I click on "Create"
    Then I should see a "Schedule Name" text
    When I enter "schedule_name" as "scheduleName"
    And I select "Highstate" from "actionTypeDescription"
    And I check radio button "schedule-daily"
    And I enter 1 minutes from now as "time-daily_time"
    And I click on the "disabled" toggler
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    Then I should see a "schedule_name" text
    And I should see a "Minion" text
    And I follow "Events"
    And I follow "History"
    And I wait until I see the event "Apply highstate in test-mode scheduled by admin" completed during last minute, refreshing the page
    And I follow the event "Apply highstate in test-mode scheduled by admin" completed during last minute

  Scenario: Edit a minion Highstate Recurring Action
    When I am on the "Recurring Actions" page of this "sle_minion"
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item edit button
    Then I should see a "Update Schedule" text
    When I enter "schedule_name_minion" as "scheduleName"
    And I check radio button "schedule-weekly"
    And I select "Wednesday" from "date_weekly"
    And I enter "01:35" as "time-weekly_time"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    Then I should see a "schedule_name_minion" text
    And I should see a "Minion" text
    And I should see a "0 35 1 ? * 4" text

  Scenario: View a minion Highstate Recurring Action details
    When I am on the "Recurring Actions" page of this "sle_minion"
    Then I should see a "schedule_name_minion" text
    When I click the "schedule_name_minion" item details button
    Then I should see a "Every Wednesday at 01:35" text
    When I click on "Back"
    Then I should see a "Schedules" text

  Scenario: Create a System group for testing
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "Recurring-Action-test-group" as "name"
    And I enter "This is for testing" as "description"
    And I click on "Create Group"
    Then I should see a "System group Recurring-Action-test-group created." text
    When I am on the "Groups" page of this "sle_minion"
    And I follow first "Join"
    And I check the first row in the list
    And I click on "Join Selected Groups"
    Then I wait until I see "1 system groups added" text

  Scenario: Create a group Recurring Action
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "Recurring Actions" in the content area
    Then I should see a "No schedules created. Use Create to add a schedule" text
    When I click on "Create"
    Then I should see a "Schedule Name" text
    When I select "Custom state" from "actionTypeDescription"
    Then I wait until I see "Configure states to execute" text
    And I enter "schedule_name" as "scheduleName"
    And I check radio button "schedule-daily"
    And I enter 1 minutes from now as "time-daily_time"
    And I check "Sync Beacons-cbox"
    And I click on "Save Changes"
    Then I should see a "Edit State Ranks" text
    And I click on "Confirm"
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    Then I should see a "schedule_name" text
    And I should see a "Group" text
    When I am on the "Events" page of this "sle_minion"
    And I follow "History"
    Then I wait until I see the event "Apply recurring states [util.syncbeacons] scheduled by admin" completed during last minute, refreshing the page

  Scenario: Edit a group Recurring Action
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "Recurring Actions" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item edit button
    Then I should see a "Update Schedule" text
    When I enter "schedule_name_group" as "scheduleName"
    And I check radio button "schedule-hourly"
    And I enter "35" as "minutes"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    Then I should see a "schedule_name_group" text
    And I should see a "Group" text
    And I should see a "0 35 * ? * *" text

  Scenario: View a group recurring actions details
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "Recurring Actions" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item details button
    Then I should see a "Every hour at minute 35" text
    And I should see a "Recurring-Action-test-group" link
    And I should see a "Sync Beacons" text
    When I click on "Back"
    Then I should see a "Schedules" text

  Scenario: Create a yourorg Recurring Action
    When I follow the left menu "Home > My Organization > Recurring Actions"
    Then I should see a "No schedules created. Use Create to add a schedule" text
    When I click on "Create"
    Then I should see a "Schedule Name" text
    When I select "Custom state" from "actionTypeDescription"
    Then I wait until I see "Configure states to execute" text
    And I enter "schedule_name" as "scheduleName"
    And I check radio button "schedule-daily"
    And I enter 1 minutes from now as "time-daily_time"
    And I click on the "disabled" toggler
    And I check "Package Profile Update-cbox"
    And I click on "Save Changes"
    Then I should see a "Edit State Ranks" text
    And I click on "Confirm"
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    Then I should see a "schedule_name" text
    And I should see a "Organization" text
    When I am on the "Events" page of this "sle_minion"
    And I follow "History"
    Then I wait until I see the event "Apply recurring states [packages.profileupdate] scheduled by admin" completed during last minute, refreshing the page

  Scenario: Edit a yourorg Recurring Action
    When I follow the left menu "Home > My Organization > Recurring Actions"
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item edit button
    Then I should see a "Update Schedule" text
    When I enter "schedule_name_edit" as "scheduleName"
    And I check radio button "schedule-monthly"
    And I select "7" from "date_monthly"
    And I enter "05:17" as "time-monthly_time"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    Then I should see a "schedule_name_edit" text
    And I should see a "Organization" text
    And I should see a "0 17 5 7 * ?" text

  Scenario: View a yourorg recurring actions details
    When I follow the left menu "Home > My Organization > Recurring Actions"
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item details button
    Then I should see a "Every 7th of the month at 05:17" text
    And I should see a "Package Profile Update" text
    When I click on "Back"
    Then I should see a "Schedules" text

  Scenario: Cleanup: Delete a yourorg Recurring Action
    When I follow the left menu "Home > My Organization > Recurring Actions"
    Then I should see a "schedule_name_edit" text
    When I click the "schedule_name" item delete button
    Then I should see a "Delete Recurring Action Schedule" text
    When I click on the red confirmation button
    Then I wait until I see "Schedule 'schedule_name_edit' has been deleted." text
    Then I should see a "No schedules created. Use Create to add a schedule" text

  Scenario: Create an admin org Recurring Action
    When I follow the left menu "Admin > Organizations"
    And I follow "SUSE Test" in the content area
    And I follow "Recurring Actions" in the content area
    Then I should see a "No schedules created. Use Create to add a schedule" text
    When I click on "Create"
    Then I should see a "Schedule Name" text
    When I select "Custom state" from "actionTypeDescription"
    Then I wait until I see "Configure states to execute" text
    And I enter "schedule_name" as "scheduleName"
    And I check radio button "schedule-daily"
    And I enter 1 minutes from now as "time-daily_time"
    And I click on the "disabled" toggler
    And I check "Hardware Profile Update-cbox"
    And I click on "Save Changes"
    Then I should see a "Edit State Ranks" text
    And I click on "Confirm"
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    Then I should see a "schedule_name" text
    And I should see a "Organization" text
    When I am on the "Events" page of this "sle_minion"
    And I follow "History"
    Then I wait until I see the event "Apply recurring states [hardware.profileupdate] scheduled by admin" completed during last minute, refreshing the page

  Scenario: Edit an admin org Recurring Action
    When I follow the left menu "Admin > Organizations"
    And I follow "SUSE Test" in the content area
    And I follow "Recurring Actions" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item edit button
    Then I should see a "Update Schedule" text
    When I enter "schedule_name_org" as "scheduleName"
    And I check radio button "schedule-cron"
    And I enter "0 0 15 3 * ?" as "cron"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    Then I should see a "schedule_name_org" text
    And I should see a "Organization" text
    And I should see a "0 0 15 3 * ?" text

  Scenario: View an admin org recurring actions details
    When I follow the left menu "Admin > Organizations"
    And I follow "SUSE Test" in the content area
    And I follow "Recurring Actions" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item details button
    Then I should see a "Every 3rd of the month at 15:00" text
    When I click on "Back"
    Then I should see a "Schedules" text

  Scenario: View all types of recurring actions in the list of all actions
    When I follow the left menu "Schedule > Recurring Actions"
    Then I should not see a "Create" text
    And I should see a "schedule_name_minion" text
    And I should see a "Minion" text
    And I should see a "schedule_name_group" text
    And I should see a "Group" text
    And I should see a "schedule_name_org" text
    And I should see a "Organization" text

 Scenario: View details in list of all actions
    When I follow the left menu "Schedule > Recurring Actions"
    And I click the "schedule_name_minion" item details button
    Then I should see a "Every Wednesday at 01:35" text
    And I should not see a "Schedules" text in the content area
    When I click on "Back"
    Then I should see a "schedule_name_group" text

  Scenario: Cleanup: Delete an admin org Recurring Action
    When I follow the left menu "Admin > Organizations"
    And I follow "SUSE Test" in the content area
    And I follow "Recurring Actions" in the content area
    Then I should see a "schedule_name_org" text
    When I click the "schedule_name_org" item delete button
    Then I should see a "Delete Recurring Action Schedule" text
    When I click on the red confirmation button
    Then I wait until I see "Schedule 'schedule_name_org' has been deleted." text

  Scenario: Cleanup: Delete a group Recurring Action
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "Recurring Actions" in the content area
    Then I should see a "schedule_name_group" text
    When I click the "schedule_name_group" item delete button
    Then I should see a "Delete Recurring Action Schedule" text
    When I click on the red confirmation button
    Then I wait until I see "Schedule 'schedule_name_group' has been deleted." text
    Then I should see a "No schedules created. Use Create to add a schedule" text

  Scenario: Cleanup: Delete a minion Recurring Action
    When I am on the "Recurring Actions" page of this "sle_minion"
    Then I should see a "schedule_name_minion" text
    When I click the "schedule_name_minion" item delete button
    Then I should see a "Delete Recurring Action Schedule" text
    When I click on the red confirmation button
    Then I wait until I see "Schedule 'schedule_name_minion' has been deleted." text
    Then I should see a "No schedules created. Use Create to add a schedule" text

  Scenario: Cleanup: Delete system group created for group recurring action tests
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "Delete Group"
    And I click on "Confirm Deletion"
    Then I should see a "Your organization has no system groups." text
