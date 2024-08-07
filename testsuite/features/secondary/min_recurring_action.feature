# Copyright (c) 2020-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
@scope_recurring_actions
Feature: Recurring Actions

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create an IP forwarding config state channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Create State Channel"
    And I wait until I see "New Config State Channel" text
    And I enter "My State Channel for Recurring Actions" as "cofName"
    And I enter "statechannel-recurring" as "cofLabel"
    And I enter "This is a state channel to be used in recurring actions" as "cofDescription"
    And I enter "enable_ip_forwarding:\n  cmd.run:\n    - name: echo 1 > /proc/sys/net/ipv4/conf/all/forwarding" in the editor
    And I click on "Create Config State Channel"
    Then I should see a "Channel Properties" text
    And I should see a "My State Channel for Recurring Actions" text

  Scenario: Enable IP forwarding through a custom state recurring action
    When I am on the "Recurring Actions" page of this "sle_minion"
    Then I should see a "No schedules created. Use Create to add a schedule" text
    When I click on "Create"
    And I wait until I see "Action Type" text
    And I enter "IP forwarding custom state recurring action" as "scheduleName"
    And I select "Custom state" from "actionTypeDescription"
    And I wait until I see "Configure states to execute" text
    And I check radio button "schedule-daily"
    And I enter 1 minutes from now as "time-daily_time"
    And I check "statechannel-recurring-cbox"
    And I click on "Save Changes"
    And I wait until I see "Edit State Ranks" text
    And I click on "Confirm"
    And I wait until I see "State assignments have been saved." text
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    And I should see a "IP forwarding custom state recurring action" text
    And I should see a "Minion" text
    When I follow "Events"
    And I follow "History"
    Then I wait until I see the event "Apply recurring states [manager_org_1.statechannel-recurring] scheduled" completed during last minute, refreshing the page
    And file "/proc/sys/net/ipv4/conf/all/forwarding" should contain "1" on "sle_minion"

  Scenario: Edit the IP forwarding custom state recurring action
    When I am on the "Recurring Actions" page of this "sle_minion"
    Then I should see a "IP forwarding custom state recurring action" text
    When I click the "IP forwarding custom state recurring action" item edit button
    And I wait until I see "Update Schedule" text
    And I enter "custom_state_schedule_name_changed" as "scheduleName"
    And I enter 1 minutes from now as "time-daily_time"
    And I uncheck "statechannel-recurring-cbox"
    And I check "Sync States-cbox"
    And I click on "Save Changes"
    And I wait until I see "Edit State Ranks" text
    And I click on "Confirm"
    And I wait until I see "State assignments have been saved" text
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    And I should see a "custom_state_schedule_name_changed" text
    And I should see a "Minion" text
    When I follow "Events"
    And I follow "History"
    Then I wait until I see the event "Apply recurring states [util.syncstates] scheduled" completed during last minute, refreshing the page
    And I follow the event "Apply recurring states [util.syncstates] scheduled" completed during last minute
    And I should see a "SLS: util.syncstates" text

  Scenario: Cleanup: Disable IP forwarding
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "echo 0 > /proc/sys/net/ipv4/conf/all/forwarding"
    And I enter target "sle_minion"
    And I click on preview
    And I click on run
    Then I should see "sle_minion" hostname
    And I wait until I see "show response" text
    And file "/proc/sys/net/ipv4/conf/all/forwarding" should contain "0" on "sle_minion"

  Scenario: Cleanup: Delete the minion Custom state Recurring Action
    When I am on the "Recurring Actions" page of this "sle_minion"
    Then I should see a "custom_state_schedule_name_changed" text
    When I click the "custom_state_schedule_name_changed" item delete button
    And I wait until I see "Delete Recurring Action Schedule" text
    And I click on the red confirmation button
    Then I wait until I see "Schedule 'custom_state_schedule_name_changed' has been deleted." text

  Scenario: Create a minion Highstate Recurring Action
    When I am on the "Recurring Actions" page of this "sle_minion"
    Then I should see a "No schedules created. Use Create to add a schedule" text
    When I click on "Create"
    And I wait until I see "Schedule Name" text
    And I enter "Minion Highstate Recurring Action" as "scheduleName"
    And I select "Highstate" from "actionTypeDescription"
    And I check radio button "schedule-daily"
    And I enter 1 minutes from now as "time-daily_time"
    And I click on the "disabled" toggler
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    And I should see a "Minion Highstate Recurring Action" text
    And I should see a "Minion" text
    When I follow "Events"
    And I follow "History"
    And I wait until I see the event "Apply highstate in test-mode scheduled" completed during last minute, refreshing the page

  Scenario: Edit the minion Highstate Recurring Action
    When I am on the "Recurring Actions" page of this "sle_minion"
    Then I should see a "Minion Highstate Recurring Action" text
    When I click the "Minion Highstate Recurring Action" item edit button
    And I wait until I see "Update Schedule" text
    And I enter "schedule_name_minion" as "scheduleName"
    And I check radio button "schedule-weekly"
    And I select "Wednesday" from "date_weekly"
    And I enter "01:35" as "time-weekly_time"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    And I should see a "schedule_name_minion" text
    And I should see a "Minion" text
    And I should see a "0 35 1 ? * 4" text

  Scenario: View the minion Highstate Recurring Action details
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

  Scenario: Pre-requisite: subscribe system to Fake Channel
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Fake-Base-Channel-SUSE-like"
    And I wait until I do not see "Loading..." text
    And I check "Fake-Child-Channel-SUSE-like"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed

  Scenario: Pre-requisite: downgrade milkyway-dummy to lower version
    When I enable repository "test_repo_rpm_pool" on this "sle_minion"
    And I install old package "milkyway-dummy-1.0" on this "sle_minion"
    And I refresh the metadata for "sle_minion"
    And I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Pre-requisite: check that there are updates available
    Given I am on the Systems overview page of this "sle_minion"
    And I wait until I see "Software Updates Available" text, refreshing the page

  Scenario: Create a recurring action to apply "uptodate" state to a system group
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "Recurring Actions" in the content area
    Then I should see a "No schedules created. Use Create to add a schedule" text
    When I click on "Create"
    And I wait until I see "Schedule Name" text
    And I enter "Recurring action to keep Recurring-Action-test-group uptodate" as "scheduleName"
    And I select "Custom state" from "actionTypeDescription"
    And I wait until I see "Configure states to execute" text
    And I check radio button "schedule-daily"
    And I enter 1 minutes from now as "time-daily_time"
    And I check "Update System-cbox"
    And I click on "Save Changes"
    And I wait until I see "Edit State Ranks" text
    And I click on "Confirm"
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    And I should see a "Recurring action to keep Recurring-Action-test-group uptodate" text
    And I should see a "Group" text
    When I am on the "Events" page of this "sle_minion"
    And I follow "History"
    Then I wait until I see the event "Apply recurring states [uptodate] scheduled" completed during last minute, refreshing the page
    When I am on the Systems overview page of this "sle_minion"
    Then I wait until I see "System is up to date" text, refreshing the page

@susemanager
  Scenario: Cleanup: subscribe system back to default base channel
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I disable repository "test_repo_rpm_pool" on this "sle_minion" without error control
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check default base channel radio button of this "sle_minion"
    And I wait for child channels to appear
    And I include the recommended child channels
    And I wait until "SLE-Module-Basesystem15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Basesystem15-SP4-Updates for x86_64" has been checked
    And I wait until "SLE-Module-Server-Applications15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Server-Applications15-SP4-Updates for x86_64" has been checked
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I wait until "SLE-Module-DevTools15-SP4-Updates for x86_64" has been checked
    And I wait until "SLE-Module-Desktop-Applications15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Desktop-Applications15-SP4-Updates for x86_64" has been checked
    And I check "SLE-Module-Containers15-SP4-Pool for x86_64"
    And I wait until "SLE-Module-Containers15-SP4-Updates for x86_64" has been checked
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed

@uyuni
  Scenario: Cleanup: subscribe system back to default base channel
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I disable repository "test_repo_rpm_pool" on this "sle_minion" without error control
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check default base channel radio button of this "sle_minion"
    And I wait for child channels to appear
    And I check "openSUSE 15.5 non oss (x86_64)"
    And I check "openSUSE Leap 15.5 non oss Updates (x86_64)"
    And I check "openSUSE Leap 15.5 Updates (x86_64)"
    And I check "Update repository of openSUSE Leap 15.5 Backports (x86_64)"
    And I check "Update repository with updates from SUSE Linux Enterprise 15 for openSUSE Leap 15.5 (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap 15.5 (x86_64) (Development)"
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed

  Scenario: Edit the group Recurring Action
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "Recurring Actions" in the content area
    Then I should see a "Recurring action to keep Recurring-Action-test-group uptodate" text
    When I click the "Recurring action to keep Recurring-Action-test-group uptodate" item edit button
    And I wait until I see "Update Schedule" text
    And I enter "schedule_name_group" as "scheduleName"
    And I check radio button "schedule-hourly"
    And I enter "35" as "minutes"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    And I should see a "schedule_name_group" text
    And I should see a "Group" text
    And I should see a "0 35 * ? * *" text

  Scenario: View the group recurring actions details
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "Recurring Actions" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item details button
    Then I should see a "Every hour at minute 35" text
    And I should see a "Recurring-Action-test-group" link
    And I should see a "Update System" text
    When I click on "Back"
    Then I should see a "Schedules" text

  Scenario: Create a yourorg Recurring Action
    When I follow the left menu "Home > My Organization > Recurring Actions"
    Then I should see a "No schedules created. Use Create to add a schedule" text
    When I click on "Create"
    And I wait until I see "Schedule Name" text
    And I enter "schedule_name" as "scheduleName"
    And I select "Custom state" from "actionTypeDescription"
    And I wait until I see "Configure states to execute" text
    And I check radio button "schedule-daily"
    And I enter 1 minutes from now as "time-daily_time"
    And I click on the "disabled" toggler
    And I check "Package Profile Update-cbox"
    And I click on "Save Changes"
    And I wait until I see "Edit State Ranks" text
    And I click on "Confirm"
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    And I should see a "schedule_name" text
    And I should see a "Organization" text
    When I am on the "Events" page of this "sle_minion"
    And I follow "History"
    Then I wait until I see the event "Apply recurring states [packages.profileupdate] scheduled" completed during last minute, refreshing the page

  Scenario: Edit the yourorg Recurring Action
    When I follow the left menu "Home > My Organization > Recurring Actions"
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item edit button
    And I wait until I see "Update Schedule" text
    And I enter "schedule_name_edit" as "scheduleName"
    And I check radio button "schedule-monthly"
    And I select "7" from "date_monthly"
    And I enter "05:17" as "time-monthly_time"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    And I should see a "schedule_name_edit" text
    And I should see a "Organization" text
    And I should see a "0 17 5 7 * ?" text

  Scenario: View the yourorg recurring actions details
    When I follow the left menu "Home > My Organization > Recurring Actions"
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item details button
    Then I should see a "Every 7th of the month at 05:17" text
    And I should see a "Package Profile Update" text
    When I click on "Back"
    Then I should see a "Schedules" text

  Scenario: Cleanup: Delete the yourorg Recurring Action
    When I follow the left menu "Home > My Organization > Recurring Actions"
    Then I should see a "schedule_name_edit" text
    When I click the "schedule_name" item delete button
    And I wait until I see "Delete Recurring Action Schedule" text
    And I click on the red confirmation button
    Then I wait until I see "Schedule 'schedule_name_edit' has been deleted." text
    And I should see a "No schedules created. Use Create to add a schedule" text

  Scenario: Create an admin org Recurring Action
    When I follow the left menu "Admin > Organizations"
    And I follow "SUSE Test" in the content area
    And I follow "Recurring Actions" in the content area
    Then I should see a "No schedules created. Use Create to add a schedule" text
    When I click on "Create"
    And I wait until I see "Schedule Name" text
    And I select "Custom state" from "actionTypeDescription"
    And I wait until I see "Configure states to execute" text
    And I enter "schedule_name" as "scheduleName"
    And I check radio button "schedule-daily"
    And I enter 1 minutes from now as "time-daily_time"
    And I click on the "disabled" toggler
    And I check "Hardware Profile Update-cbox"
    And I click on "Save Changes"
    And I wait until I see "Edit State Ranks" text
    And I click on "Confirm"
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    And I should see a "schedule_name" text
    And I should see a "Organization" text
    When I am on the "Events" page of this "sle_minion"
    And I follow "History"
    Then I wait until I see the event "Apply recurring states [hardware.profileupdate] scheduled" completed during last minute, refreshing the page

  Scenario: Edit the admin org Recurring Action
    When I follow the left menu "Admin > Organizations"
    And I follow "SUSE Test" in the content area
    And I follow "Recurring Actions" in the content area
    Then I should see a "schedule_name" text
    When I click the "schedule_name" item edit button
    And I wait until I see "Update Schedule" text
    And I enter "schedule_name_org" as "scheduleName"
    And I check radio button "schedule-cron"
    And I enter "0 0 15 3 * ?" as "cron"
    And I click on "Update Schedule"
    Then I wait until I see "Schedule successfully updated" text
    And I should see a "schedule_name_org" text
    And I should see a "Organization" text
    And I should see a "0 0 15 3 * ?" text

  Scenario: View the admin org recurring actions details
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

  Scenario: Cleanup: Delete the admin org Recurring Action
    When I follow the left menu "Admin > Organizations"
    And I follow "SUSE Test" in the content area
    And I follow "Recurring Actions" in the content area
    Then I should see a "schedule_name_org" text
    When I click the "schedule_name_org" item delete button
    And I wait until I see "Delete Recurring Action Schedule" text
    And I click on the red confirmation button
    Then I wait until I see "Schedule 'schedule_name_org' has been deleted." text

  Scenario: Cleanup: Delete the group Recurring Action
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "Recurring Actions" in the content area
    Then I should see a "schedule_name_group" text
    When I click the "schedule_name_group" item delete button
    And I wait until I see "Delete Recurring Action Schedule" text
    And I click on the red confirmation button
    Then I wait until I see "Schedule 'schedule_name_group' has been deleted." text
    And I should see a "No schedules created. Use Create to add a schedule" text

  Scenario: Cleanup: Delete the minion Highstate Recurring Action
    When I am on the "Recurring Actions" page of this "sle_minion"
    Then I should see a "schedule_name_minion" text
    When I click the "schedule_name_minion" item delete button
    And I wait until I see "Delete Recurring Action Schedule" text
    And I click on the red confirmation button
    Then I wait until I see "Schedule 'schedule_name_minion' has been deleted." text
    And I should see a "No schedules created. Use Create to add a schedule" text

  Scenario: Cleanup: Delete the system group created for group recurring action tests
    When I follow the left menu "Systems > System Groups"
    And I follow "Recurring-Action-test-group"
    And I follow "Delete Group"
    And I click on "Confirm Deletion"
    Then I should see a "Your organization has no system groups." text

  Scenario: Cleanup: Remove "My State Channel for Recurring Actions" config channel
    When I follow the left menu "Configuration > Channels"
    And I follow "My State Channel for Recurring Actions"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"
    Then I should see a "Channel 'My State Channel for Recurring Actions' has been deleted" text
