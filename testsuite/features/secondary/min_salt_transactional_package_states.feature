# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@slmicro62_minion
Feature: Package states on transactional systems
  In order to install software on transactional systems
  As an authorized user
  I want to apply package states without a failure

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Apply a package state on a transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "States" in the content area
    And I follow "Packages"
    And I follow "Search"
    And I should see a "Package States" text
    And I list packages with "jq"
    And I wait until I see "jq" text
    Then "jq" should not be installed on "slmicro62_minion"
    When I remember the latest "Apply states" event of "slmicro62_minion"
    And I change the state of "jq" to "Installed" and ""
    Then I should see a "1 Change" text
    When I click save
    Then I wait until I see "Package states have been saved." text
    When I click apply
    Then I wait until the system "slmicro62_minion" has a completed "Apply states" event newer than the remembered one

  Scenario: Complete the package install by rebooting the transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    Then I wait until I see "There is a pending transaction for this system" text, refreshing the page
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text
    And "jq" should be installed on "slmicro62_minion"

  Scenario: Cleanup: stop selecting the package in the package states
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "States" in the content area
    And I follow "Packages"
    And I follow "Search"
    And I list packages with "jq"
    And I wait until I see "jq" text
    And I change the state of "jq" to "Unmanaged" and ""
    And I click save
    Then I wait until I see "Package states have been saved." text

  Scenario: Cleanup: remove the package installed by the package state
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Remote Command"
    And I enter "#!/bin/sh\nzypper --non-interactive remove jq" as "script_body" text area
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
    And "jq" should not be installed on "slmicro62_minion"
