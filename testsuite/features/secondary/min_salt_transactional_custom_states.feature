# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@slmicro62_minion
@scope_configuration_channels
Feature: Custom states on transactional systems
  In order to configure transactional systems through Salt
  As an authorized user
  I want to execute custom states in a transaction or on the live system

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: create a state channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Create State Channel"
    Then I should see a "New Config State Channel" text
    When I enter "Transactional State Channel" as "cofName"
    And I enter "transactionalstate" as "cofLabel"
    And I enter "State channel for the transactional systems tests" as "cofDescription"
    And I enter "touch /usr/share/mgr-transactional-state-test:\n  cmd.run:\n    - creates: /usr/share/mgr-transactional-state-test\n\ntouch /tmp/mgr-live-state-test:\n  cmd.run:\n    - creates: /tmp/mgr-live-state-test" in the editor
    And I click on "Create Config State Channel"
    Then I should see a "State Channel" text
    And I should see a "Channel Properties" text

  Scenario: Pre-requisite: subscribe the transactional minion to the state channel
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Transactional State Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Show the custom states run with transactional update waiting for a reboot
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "States" in the content area
    And I follow "Configuration Channels" in the content area
    And I check "Use transactional update"
    And I click on "Execute States"
    Then I should see a "Applying the config channels has been scheduled" text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until I see "Waiting for reboot" text, refreshing the page
    And file "/usr/share/mgr-transactional-state-test" should not exist on "slmicro62_minion"

  Scenario: Complete the custom states after rebooting the transactional minion
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
    And file "/usr/share/mgr-transactional-state-test" should exist on "slmicro62_minion"

  Scenario: Execute the custom states on the live system without transactional update
    Given file "/tmp/mgr-live-state-test" should not exist on "slmicro62_minion"
    And I am on the Systems overview page of this "slmicro62_minion"
    When I follow "States" in the content area
    And I follow "Configuration Channels" in the content area
    And I click on "Execute States"
    Then I should see a "Applying the config channels has been scheduled" text
    When I wait until file "/tmp/mgr-live-state-test" exists on "slmicro62_minion"
    And I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should not see a "Waiting for reboot" text

  Scenario: Cleanup: remove the file created by the custom states
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Remote Command"
    And I enter "#!/bin/sh\nrm -f /usr/share/mgr-transactional-state-test" as "script_body" text area
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
    And file "/usr/share/mgr-transactional-state-test" should not exist on "slmicro62_minion"

  Scenario: Cleanup: remove the state channel and the live file
    When I follow the left menu "Configuration > Channels"
    And I follow "Transactional State Channel"
    And I follow "Delete Channel"
    Then I should see a "Are you sure you want to delete this config channel?" text
    When I click on "Delete Config Channel"
    Then I should see a "Channel 'Transactional State Channel' has been deleted." text
    And I remove "/tmp/mgr-live-state-test" from "slmicro62_minion"
