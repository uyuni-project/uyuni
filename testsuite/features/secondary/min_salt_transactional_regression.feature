# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@sshminion
@scope_formulas
Feature: Systems that are not transactional keep their behavior
  In order to be sure the transactional support changes nothing for the other systems
  As an authorized user
  I want actions on a minion that is not transactional to run directly on the system, with no transactional options and nothing waiting for a reboot

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Show no transactional interface on a minion that is not transactional
    Given I am on the Systems overview page of this "sshminion"
    Then I should not see a "There is a pending transaction for this system" text
    When I follow "Software" in the content area
    Then I should not see a "Btrfs Snapshots" link in the content area
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    And I should not see a "Use transactional update" text

  Scenario: Run a remote command on a minion that is not transactional
    Given I am on the Systems overview page of this "sshminion"
    When I follow "Remote Command"
    Then I should not see a "Use transactional update" text
    When I enter "#!/bin/sh\ntouch /tmp/mgr-regression-test" as "script_body" text area
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled" text
    When I wait until file "/tmp/mgr-regression-test" exists on "sshminion"
    And I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should not see a "Waiting for reboot" text

  Scenario: Cleanup: remove the file created by the remote command
    When I remove "/tmp/mgr-regression-test" from "sshminion"
    Then file "/tmp/mgr-regression-test" should not exist on "sshminion"

  Scenario: Pre-requisite: create a state channel for a minion that is not transactional
    When I follow the left menu "Configuration > Channels"
    And I follow "Create State Channel"
    Then I should see a "New Config State Channel" text
    When I enter "Regression State Channel" as "cofName"
    And I enter "regressionstate" as "cofLabel"
    And I enter "State channel for the transactional regression tests" as "cofDescription"
    And I enter "touch /tmp/mgr-regression-state-test:\n  cmd.run:\n    - creates: /tmp/mgr-regression-state-test" in the editor
    And I click on "Create Config State Channel"
    Then I should see a "State Channel" text
    And I should see a "Channel Properties" text

  Scenario: Pre-requisite: subscribe the minion that is not transactional to the state channel
    Given I am on the Systems overview page of this "sshminion"
    When I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Regression State Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Execute custom states on a minion that is not transactional
    Given I am on the Systems overview page of this "sshminion"
    When I follow "States" in the content area
    And I follow "Configuration Channels" in the content area
    Then I should not see a "Use transactional update" text
    When I click on "Execute States"
    Then I should see a "Applying the config channels has been scheduled" text
    When I wait until file "/tmp/mgr-regression-state-test" exists on "sshminion"
    And I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should not see a "Waiting for reboot" text

  Scenario: Cleanup: remove the state channel and its file
    When I follow the left menu "Configuration > Channels"
    And I follow "Regression State Channel"
    And I follow "Delete Channel"
    Then I should see a "Are you sure you want to delete this config channel?" text
    When I click on "Delete Config Channel"
    Then I should see a "Channel 'Regression State Channel' has been deleted." text
    And I remove "/tmp/mgr-regression-state-test" from "sshminion"

  Scenario: Pre-requisite: install the custom formulas on the server
    When I install the custom formula "thenliveformula" on the server
    And I install the custom formula "unsupportedformula" on the server
    And I follow the left menu "Salt > Formula Catalog"
    And I enter "thenliveformula" as the filtered formula name
    Then I should see a "thenliveformula" text
    When I enter "unsupportedformula" as the filtered formula name
    Then I should see a "unsupportedformula" text

  Scenario: Pre-requisite: enable the formulas on the minion that is not transactional
    Given I am on the Systems overview page of this "sshminion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I check the "thenliveformula" formula
    And I check the "unsupportedformula" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "thenliveformula" formula should be checked
    And the "unsupportedformula" formula should be checked

  Scenario: Apply the formulas on a minion that is not transactional
    Given I am on the Systems overview page of this "sshminion"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    When I wait until file "/tmp/mgr-then-live-formula-test" exists on "sshminion"
    Then file "/usr/share/mgr-then-live-formula-test" should exist on "sshminion"
    And I wait until file "/tmp/mgr-unsupported-formula-test" exists on "sshminion"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should see a "Apply highstate scheduled by admin" text
    And I should not see a "Waiting for reboot" text

  Scenario: Remove the files created by the highstate on the minion that is not transactional
    When I remove "/usr/share/mgr-then-live-formula-test" from "sshminion"
    And I remove "/tmp/mgr-then-live-formula-test" from "sshminion"
    And I remove "/tmp/mgr-unsupported-formula-test" from "sshminion"
    Then file "/tmp/mgr-then-live-formula-test" should not exist on "sshminion"

  Scenario: Create a recurring formulas action on the minion that is not transactional
    When I am on the "Recurring Actions" page of this "sshminion"
    Then I should see a "No schedules created. Use Create to add a schedule" text
    When I click on "Create"
    And I wait until I see "Action Type" text
    And I enter "Regression formulas recurring action" as "scheduleName"
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
    And I should see a "Regression formulas recurring action" text

  Scenario: Run the recurring formulas action on the minion that is not transactional
    Given I am on the Systems overview page of this "sshminion"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    And I wait at most 420 seconds until event "Apply recurring states" is completed
    Then I wait until file "/tmp/mgr-then-live-formula-test" exists on "sshminion"
    And file "/usr/share/mgr-then-live-formula-test" should exist on "sshminion"
    And file "/tmp/mgr-unsupported-formula-test" should exist on "sshminion"
    And I should not see a "Waiting for reboot" text

  Scenario: Cleanup: delete the recurring formulas action of the minion that is not transactional
    When I am on the "Recurring Actions" page of this "sshminion"
    Then I should see a "Regression formulas recurring action" text
    When I click the "Regression formulas recurring action" item delete button
    And I wait until I see "Delete Recurring Action Schedule" text
    And I click on the red confirmation button
    Then I wait until I see "Schedule 'Regression formulas recurring action' has been deleted." text

  Scenario: Cleanup: disable the formulas on the minion that is not transactional
    Given I am on the Systems overview page of this "sshminion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I uncheck the "thenliveformula" formula
    And I uncheck the "unsupportedformula" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "thenliveformula" formula should be unchecked
    And the "unsupportedformula" formula should be unchecked

  Scenario: Cleanup: remove the files and the custom formulas
    When I remove "/usr/share/mgr-then-live-formula-test" from "sshminion"
    And I remove "/tmp/mgr-then-live-formula-test" from "sshminion"
    And I remove "/tmp/mgr-unsupported-formula-test" from "sshminion"
    And I remove the custom formula "thenliveformula" from the server
    And I remove the custom formula "unsupportedformula" from the server
    And I follow the left menu "Salt > Formula Catalog"
    And I enter "thenliveformula" as the filtered formula name
    Then I should not see a "thenliveformula" text
