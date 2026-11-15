# Copyright (c) 2026 SUSE LLC
# SPDX-License-Identifier: MIT

@scope_recurring_actions
Feature: Recurring Actions with user-created custom states
  Custom state recurring actions that use state channels created by the user:
  organization scope, test mode, execution order and editing the selected states.

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create the first state channel for custom state recurring actions
    When I follow the left menu "Configuration > Channels"
    And I follow "Create State Channel"
    And I wait until I see "New Config State Channel" text
    And I enter "Recurring first state" as "cofName"
    And I enter "recurring-first" as "cofLabel"
    And I enter "State writing first to a marker file" as "cofDescription"
    And I enter "recurring_first:\n  cmd.run:\n    - name: echo first >> /tmp/recurring_order" in the editor
    And I click on "Create Config State Channel"
    Then I wait until I see "Channel Properties" text

  Scenario: Create the second state channel for custom state recurring actions
    When I follow the left menu "Configuration > Channels"
    And I follow "Create State Channel"
    And I wait until I see "New Config State Channel" text
    And I enter "Recurring second state" as "cofName"
    And I enter "recurring-second" as "cofLabel"
    And I enter "State writing second to a marker file" as "cofDescription"
    And I enter "recurring_second:\n  cmd.run:\n    - name: echo second >> /tmp/recurring_order" in the editor
    And I click on "Create Config State Channel"
    Then I wait until I see "Channel Properties" text

  Scenario: Create a System group for custom state recurring actions
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "Recurring-Custom-State-group" as "name"
    And I enter "This is for testing" as "description"
    And I click on "Create Group"
    Then I should see a "System group Recurring-Custom-State-group created." text
    When I am on the "Groups" page of this "sle_minion"
    And I follow first "Join"
    And I check the first row in the list
    And I click on "Join Groups"
    Then I wait until I see "1 system groups added" text

  Scenario: Run a user-created custom state from an organization recurring action
    Given I run "rm -f /tmp/recurring_order" on "sle_minion"
    And I follow the left menu "Home > My Organization > Recurring Actions"
    And I click on "Create"
    And I wait until I see "Schedule Name" text
    And I enter "org_custom_state" as "scheduleName"
    And I select "Custom state" from "actionTypeDescription"
    And I wait until I see "Configure states to execute" text
    And I check radio button "schedule-daily"
    And I enter 3 minutes from now as "time-daily_time"
    And I check "recurring-first-cbox"
    And I click on "Save Changes"
    And I wait until I see "Edit State Ranks" text
    And I click on "Confirm"
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    And I should see a "org_custom_state" text
    And I should see a "Organization" text
    When I am on the "Events" page of this "sle_minion"
    And I follow "History"
    Then I wait until I see the event "Apply recurring states [manager_org_1.recurring-first] schedule" completed during last minute, refreshing the page
    And file "/tmp/recurring_order" should contain "first" on "sle_minion"

  Scenario: Cleanup: Delete the organization custom state Recurring Action
    When I follow the left menu "Home > My Organization > Recurring Actions"
    Then I should see a "org_custom_state" text
    When I click the "org_custom_state" item delete button
    And I wait until I see "Delete Recurring Action Schedule" text
    And I click on the red confirmation button
    Then I wait until I see "Schedule 'org_custom_state' has been deleted." text

  Scenario: Cleanup: Remove the marker file from all Salt minions
    When I run "salt '*' cmd.run 'rm -f /tmp/recurring_order'" on "server" without error control

  Scenario: Test mode does not apply a user-created custom state
    Given I run "rm -f /tmp/recurring_order" on "sle_minion"
    When I store the current last event id for "sle_minion"
    And I am on the "Recurring Actions" page of this "sle_minion"
    And I click on "Create"
    And I wait until I see "Action Type" text
    And I enter "custom_state_test_mode" as "scheduleName"
    And I select "Custom state" from "actionTypeDescription"
    And I wait until I see "Configure states to execute" text
    And I check radio button "schedule-daily"
    And I enter 3 minutes from now as "time-daily_time"
    And I click on the "disabled" toggler
    And I check "recurring-first-cbox"
    And I click on "Save Changes"
    And I wait until I see "Edit State Ranks" text
    And I click on "Confirm"
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    When I wait until a new "Apply recurring states [manager_org_1.recurring-first]" event is completed for "sle_minion"
    Then file "/tmp/recurring_order" should not exist on "sle_minion"

  Scenario: Cleanup: Delete the test mode custom state Recurring Action
    When I am on the "Recurring Actions" page of this "sle_minion"
    Then I should see a "custom_state_test_mode" text
    When I click the "custom_state_test_mode" item delete button
    And I wait until I see "Delete Recurring Action Schedule" text
    And I click on the red confirmation button
    Then I wait until I see "Schedule 'custom_state_test_mode' has been deleted." text

  Scenario: Apply several user-created custom states in the order of the ranking list on a system group
    Given I run "rm -f /tmp/recurring_order" on "sle_minion"
    When I store the current last event id for "sle_minion"
    And I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Custom-State-group"
    And I follow "Recurring Actions" in the content area
    And I click on "Create"
    And I wait until I see "Schedule Name" text
    And I enter "group_custom_states_ordered" as "scheduleName"
    And I select "Custom state" from "actionTypeDescription"
    And I wait until I see "Configure states to execute" text
    And I check radio button "schedule-daily"
    And I enter 3 minutes from now as "time-daily_time"
    And I check "recurring-first-cbox"
    And I check "recurring-second-cbox"
    And I click on "Save Changes"
    And I wait until I see "Edit State Ranks" text
    And I drag the ranking item "recurring-first" below "recurring-second"
    And I click on "Confirm"
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    And I should see a "Group" text
    When I wait until a new "Apply recurring states" event is completed for "sle_minion"
    Then I run "paste -sd, /tmp/recurring_order | grep -x second,first" on "sle_minion"

  Scenario: Edit the selected custom states of the group Recurring Action
    Given I run "rm -f /tmp/recurring_order" on "sle_minion"
    When I store the current last event id for "sle_minion"
    And I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Custom-State-group"
    And I follow "Recurring Actions" in the content area
    Then I should see a "group_custom_states_ordered" text
    When I click the "group_custom_states_ordered" item edit button
    And I wait until I see "Update Schedule" text
    And I enter 3 minutes from now as "time-daily_time"
    And I uncheck "recurring-second-cbox"
    And I click on "Save Changes"
    And I wait until I see "Edit State Ranks" text
    And I click on "Confirm"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    When I wait until a new "Apply recurring states [manager_org_1.recurring-first]" event is completed for "sle_minion"
    Then I run "paste -sd, /tmp/recurring_order | grep -x first" on "sle_minion"

  Scenario: Cleanup: Delete the group custom states Recurring Action
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Custom-State-group"
    And I follow "Recurring Actions" in the content area
    Then I should see a "group_custom_states_ordered" text
    When I click the "group_custom_states_ordered" item delete button
    And I wait until I see "Delete Recurring Action Schedule" text
    And I click on the red confirmation button
    Then I wait until I see "Schedule 'group_custom_states_ordered' has been deleted." text

  Scenario: Cleanup: Delete the system group for custom state recurring actions
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Custom-State-group"
    And I follow "Delete Group"
    And I click on "Confirm Deletion"
    Then I should see a "Your organization has no system groups." text

  Scenario: Cleanup: Remove the state channels and the marker file
    When I run "rm -f /tmp/recurring_order" on "sle_minion"
    And I follow the left menu "Configuration > Channels"
    And I follow "Recurring first state"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"
    Then I should see a "Channel 'Recurring first state' has been deleted" text
    When I follow the left menu "Configuration > Channels"
    And I follow "Recurring second state"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"
    Then I should see a "Channel 'Recurring second state' has been deleted" text
