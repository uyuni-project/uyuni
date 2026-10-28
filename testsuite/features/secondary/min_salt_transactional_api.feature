# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@slmicro62_minion
@scope_configuration_channels
Feature: Transactional update through the API
  In order to automate the management of transactional systems
  As an API user
  I want to run scripts and states in a transaction and see when they wait for a reboot

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: create a state channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Create State Channel"
    Then I should see a "New Config State Channel" text
    When I enter "Transactional API State Channel" as "cofName"
    And I enter "transactionalapistate" as "cofLabel"
    And I enter "State channel for the transactional API tests" as "cofDescription"
    And I enter "touch /usr/share/mgr-transactional-api-state-test:\n  cmd.run:\n    - creates: /usr/share/mgr-transactional-api-state-test" in the editor
    And I click on "Create Config State Channel"
    Then I should see a "State Channel" text
    And I should see a "Channel Properties" text

  Scenario: Pre-requisite: subscribe the transactional minion to the state channel
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Transactional API State Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Schedule a script with transactional update through the API
    When I schedule the script "#!/bin/sh\ntouch /usr/share/mgr-transactional-api-test" labeled "Transactional API script" with transactional update for "slmicro62_minion" via API
    Then I wait until the latest "Transactional API script" event of "slmicro62_minion" is waiting for a reboot via API
    And file "/usr/share/mgr-transactional-api-test" should not exist on "slmicro62_minion"

  Scenario: Complete the script after rebooting the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text
    Then I wait until the latest "Transactional API script" event of "slmicro62_minion" is not waiting for a reboot via API
    And file "/usr/share/mgr-transactional-api-test" should exist on "slmicro62_minion"

  Scenario: Schedule the custom states with transactional update through the API
    When I schedule the states "custom" with transactional update for "slmicro62_minion" via API
    Then I wait until the latest "Apply states [custom]" event of "slmicro62_minion" is waiting for a reboot via API
    And file "/usr/share/mgr-transactional-api-state-test" should not exist on "slmicro62_minion"

  Scenario: Complete the custom states after rebooting the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text
    Then I wait until the latest "Apply states [custom]" event of "slmicro62_minion" is not waiting for a reboot via API
    And file "/usr/share/mgr-transactional-api-state-test" should exist on "slmicro62_minion"

  Scenario: Cleanup: remove the files created through the API
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Remote Command"
    And I enter "#!/bin/sh\nrm -f /usr/share/mgr-transactional-api-test /usr/share/mgr-transactional-api-state-test" as "script_body" text area
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
    And file "/usr/share/mgr-transactional-api-test" should not exist on "slmicro62_minion"
    And file "/usr/share/mgr-transactional-api-state-test" should not exist on "slmicro62_minion"

  Scenario: Cleanup: remove the state channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Transactional API State Channel"
    And I follow "Delete Channel"
    Then I should see a "Are you sure you want to delete this config channel?" text
    When I click on "Delete Config Channel"
    Then I should see a "Channel 'Transactional API State Channel' has been deleted." text
