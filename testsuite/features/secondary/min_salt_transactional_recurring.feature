# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@slmicro62_minion
@scope_formulas
@scope_recurring_actions
Feature: Recurring actions on transactional systems
  In order to keep transactional systems configured over time
  As an authorized user
  I want the recurring formulas action to follow the transactional and the reboot flow

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: install the custom formula on the server
    When I install the custom formula "transactionalformula" on the server
    And I follow the left menu "Salt > Formula Catalog"
    And I enter "transactionalformula" as the filtered formula name
    Then I should see a "transactionalformula" text

  Scenario: Pre-requisite: enable the transactional formula on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I check the "transactionalformula" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "transactionalformula" formula should be checked

  Scenario: Create a recurring formulas action on the transactional minion
    When I am on the "Recurring Actions" page of this "slmicro62_minion"
    Then I should see a "No schedules created. Use Create to add a schedule" text
    When I click on "Create"
    And I wait until I see "Action Type" text
    And I enter "Transactional formulas recurring action" as "scheduleName"
    And I select "Custom state" from "actionTypeDescription"
    And I wait until I see "Configure states to execute" text
    And I check radio button "schedule-daily"
    And I enter 1 minutes from now as "time-daily_time"
    And I check "Formulas-cbox"
    And I click on "Save Changes"
    And I wait until I see "Edit State Ranks" text
    And I click on "Confirm"
    And I click on "Create Schedule"
    Then I wait until I see "Schedule successfully created" text
    And I should see a "Transactional formulas recurring action" text

  Scenario: Show the recurring formulas action waiting for a reboot
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until I see "Apply recurring states" text, refreshing the page
    And I wait until I see "Waiting for reboot" text, refreshing the page
    And file "/usr/share/mgr-transactional-formula-test" should not exist on "slmicro62_minion"

  Scenario: Apply the recurring formulas action after rebooting the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    Then I wait until I see "There is a pending transaction for this system" text, refreshing the page
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until I do not see "Waiting for reboot" text, refreshing the page
    And file "/usr/share/mgr-transactional-formula-test" should exist on "slmicro62_minion"

  Scenario: Cleanup: delete the recurring formulas action of the transactional minion
    When I am on the "Recurring Actions" page of this "slmicro62_minion"
    Then I should see a "Transactional formulas recurring action" text
    When I click the "Transactional formulas recurring action" item delete button
    And I wait until I see "Delete Recurring Action Schedule" text
    And I click on the red confirmation button
    Then I wait until I see "Schedule 'Transactional formulas recurring action' has been deleted." text

  Scenario: Cleanup: remove the file created by the formula
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Remote Command"
    And I enter "#!/bin/sh\nrm -f /usr/share/mgr-transactional-formula-test" as "script_body" text area
    And I check "use_transactional_update"
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled" text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until I see "Waiting for reboot" text, refreshing the page
    When I follow "Details" in the content area
    And I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text
    And file "/usr/share/mgr-transactional-formula-test" should not exist on "slmicro62_minion"

  Scenario: Create a recurring formulas action through the API
    When I create the recurring action "Transactional formulas API recurring action" applying the states "formulas" to "slmicro62_minion" in 1 minute via API
    Then I wait until the latest "Apply recurring states" event of "slmicro62_minion" is waiting for a reboot via API
    And file "/usr/share/mgr-transactional-formula-test" should not exist on "slmicro62_minion"

  Scenario: Complete the recurring formulas action created through the API after rebooting
    Given I am on the Systems overview page of this "slmicro62_minion"
    Then I wait until I see "There is a pending transaction for this system" text, refreshing the page
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until the latest "Apply recurring states" event of "slmicro62_minion" is not waiting for a reboot via API
    And file "/usr/share/mgr-transactional-formula-test" should exist on "slmicro62_minion"

  Scenario: Cleanup: delete the recurring formulas action created through the API
    When I delete the recurring action "Transactional formulas API recurring action" of "slmicro62_minion" via API
    And I am on the "Recurring Actions" page of this "slmicro62_minion"
    Then I should see a "No schedules created. Use Create to add a schedule" text

  Scenario: Cleanup: remove the file created by the formula
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Remote Command"
    And I enter "#!/bin/sh\nrm -f /usr/share/mgr-transactional-formula-test" as "script_body" text area
    And I check "use_transactional_update"
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled" text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until I see "Waiting for reboot" text, refreshing the page
    When I follow "Details" in the content area
    And I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text
    And file "/usr/share/mgr-transactional-formula-test" should not exist on "slmicro62_minion"

  Scenario: Cleanup: disable the transactional formula on the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I uncheck the "transactionalformula" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "transactionalformula" formula should be unchecked

  Scenario: Cleanup: remove the custom formula from the server
    When I remove the custom formula "transactionalformula" from the server
    And I follow the left menu "Salt > Formula Catalog"
    And I enter "transactionalformula" as the filtered formula name
    Then I should not see a "transactionalformula" text
