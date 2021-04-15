# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_maintenance_windows
Feature: Maintenance Windows

    Scenario: Log in as admin user
        Given I am authorized for the "Admin" section

    Scenario: Create Calendar single
        When I follow the left menu "Schedule > Maintenance Windows > Calendars"
        And I click on "Create"
        Then I should see a "Maintenance Calendar" text
        When I enter "singlecalendar" as "name"
        And I add "maintenance-windows-exchange.ics" calendar file as url
        And I click on "Create Calendar"
        Then I should see a "Calendar successfully created" text

    Scenario: Create Calendar multi
        When I follow the left menu "Schedule > Maintenance Windows > Calendars"
        And I click on "Create"
        Then I should see a "Maintenance Calendar" text
        When I enter "multicalendar" as "name"
        And I add "maintenance-windows-multi-exchange-1.ics" calendar file as url
        And I click on "Create Calendar"
        Then I should see a "Calendar successfully created" text

    Scenario: Create a single Schedule
        When I follow the left menu "Schedule > Maintenance Windows > Schedules"
        And I click on "Create"
        Then I should see a "Maintenance Schedule" text
        When I enter "singleschedule" as "name"
        And I choose "SINGLE"
        And I click on "Add Calendar"
        And I select "singlecalendar" from "calendarSelect"
        And I click on "Create Schedule"
        Then I should see a "Schedule successfully created" text

    Scenario: Create multi Schedules
        When I follow the left menu "Schedule > Maintenance Windows > Schedules"
        And I click on "Create"
        Then I should see a "Maintenance Schedule" text
        When I enter "SAP Maintenance Window" as "name"
        And I choose "MULTI"
        And I click on "Add Calendar"
        And I select "multicalendar" from "calendarSelect"
        And I click on "Create Schedule"
        Then I should see a "Schedule successfully created" text
        When I click on "Create"
        And I enter "Core Server Window" as "name"
        And I choose "MULTI"
        And I click on "Add Calendar"
        And I select "multicalendar" from "calendarSelect"
        And I click on "Create Schedule"
        Then I should see a "Schedule successfully created" text

    Scenario: Assign a single system to the single Schedule
        Given I am on the Systems overview page of this "sle_minion"
        When I follow "Properties" in the content area
        And I select "singleschedule" from "maintenance-schedule"
        And I click on "Update Properties"
        Then I should see a "System properties changed" text

    Scenario: Assign systems to a multi Schedule using SSM
        When I am on the System Overview page
        And I follow "Clear"
        And I check the "sle_client" client
        And I am on System Set Manager Overview
        And I follow "Assign" in the content area
        And I select "SAP Maintenance Window" from "scheduleId"
        And I check "cancelActions"
        And I click on "Assign All"
        And I click on "Confirm"
        Then I should see a "Maintenance schedule has been assigned" text

    Scenario: Schedule Channel Change Action
        Given I am on the Systems overview page of this "sle_minion"
        When I follow "Software" in the content area
        And I follow "Software Channels" in the content area
        And I wait until I do not see "Loading..." text
        Then radio button "Test-Channel-x86_64" is checked
        And I wait until I do not see "Loading..." text
        And I should see "Test-Channel-x86_64 Child Channel" as unchecked
        And I check "Test-Channel-x86_64 Child Channel"
        And I click on "Next"
        Then I should see a "Confirm Software Channel Change" text
        And I pick "17:30" as time
        And I click on "Confirm"
        Then I should see a "Changing the channels has been scheduled." text

    Scenario: Schedule Package installation Action
        When I remove package "virgo-dummy" from this "sle_client" without error control
        Given I am on the Systems overview page of this "sle_client"
        When I follow "Software" in the content area
        And I follow "Install"
        And I check "virgo-dummy" in the list
        And I click on "Install Selected Packages"
        And I select the next maintenance window
        And I click on "Confirm"
        Then I should see a "1 package install has been scheduled for" text

    Scenario: Detach systems from Schedules
        When I am on the System Overview page
        And I follow "Clear"
        And I check the "sle_client" client
        And I check the "sle_minion" client
        And I am on System Set Manager Overview
        And I follow "Assign" in the content area
        And I select "None - clear schedule" from "scheduleId"
        And I click on "Clear All"
        Then I should see a "Maintenance schedule has been cleared" text

    Scenario: Cleanup - Cancel all Scheduled Actions
        Given I am logged in via XML-RPC actionchain as user "admin" and password "admin"
        Then I cancel all scheduled actions

    Scenario: Delete Maintenance Schedules
        When I follow the left menu "Schedule > Maintenance Windows > Schedules"
        When I click the "Core Server Window" item delete button
        Then I should see a "Delete maintenance schedule" text
        When I click on the red confirmation button
        Then I wait until I see "Schedule 'Core Server Window' has been deleted." text
        When I click the "SAP Maintenance Window" item delete button
        Then I should see a "Delete maintenance schedule" text
        When I click on the red confirmation button
        Then I wait until I see "Schedule 'SAP Maintenance Window' has been deleted." text
        When I click the "singleschedule" item delete button
        Then I should see a "Delete maintenance schedule" text
        When I click on the red confirmation button
        Then I wait until I see "singleschedule' has been deleted." text

    Scenario: Delete Calendars
        When I follow the left menu "Schedule > Maintenance Windows > Calendars"
        When I click the "singlecalendar" item delete button
        Then I should see a "Delete maintenance calendar" text
        When I click on the red confirmation button
        Then I wait until I see "Calendar 'singlecalendar' has been deleted." text
        When I click the "multicalendar" item delete button
        Then I should see a "Delete maintenance calendar" text
        When I click on the red confirmation button
        Then I wait until I see "Calendar 'multicalendar' has been deleted." text
