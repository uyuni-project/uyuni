# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@slmicro62_minion
Feature: Transactional systems management
  In order to manage transactional systems safely
  As an authorized user
  I want to see their snapshots and run actions that follow the reboot flow

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Show the active and default snapshot of a transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Software" in the content area
    And I follow "Btrfs Snapshots" in the content area
    Then I should see a "Btrfs Snapshots" text
    And I should see a "Refresh Snapshots" button
    And I should see a "active" text
    And I should see a "default" text

  Scenario: Schedule a snapshot refresh on a transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Software" in the content area
    And I follow "Btrfs Snapshots" in the content area
    And I click on "Refresh Snapshots"
    Then I should see a "The action has been scheduled." text
    When I wait until event "Refresh Snapshots scheduled by admin" is completed
    And I follow "Software" in the content area
    And I follow "Btrfs Snapshots" in the content area
    Then I should see a "active" text

  Scenario: Schedule a snapshot refresh after a package install on a transactional minion
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I follow "Install"
    And I wait until I see "Installable Packages" text
    And I enter the package for "slmicro62_minion" as the filtered package name
    And I click on the filter button
    And I check the package last version for "slmicro62_minion" in the list
    And I click on "Install Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    When I wait until event "Package Install/Upgrade" is completed
    Then I wait until the system "slmicro62_minion" has a completed "Refresh Snapshots" event newer than its latest "Package Install/Upgrade" event

  Scenario: Show the waiting for reboot flag next to the completed action
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should see a "Package Install/Upgrade scheduled by admin" text
    And I should see a "Waiting for reboot" text

  Scenario: Activate the package install by rebooting the transactional minion
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
    When I follow "Details" in the content area
    Then I wait until I do not see "There is a pending transaction for this system" text, refreshing the page

  Scenario: Show a remote command run with transactional update waiting for a reboot
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Remote Command"
    And I enter "#!/bin/sh\ntouch /usr/share/mgr-transactional-test" as "script_body" text area
    And I check "use_transactional_update"
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled" text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until I see "Waiting for reboot" text, refreshing the page
    And file "/usr/share/mgr-transactional-test" should not exist on "slmicro62_minion"
    When I follow first "Remote Command on"
    Then I wait until I see "Transactional update: completed" text, refreshing the page
    And I should see a "System reboot: pending" text

  Scenario: Complete the remote command after rebooting the transactional minion
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
    When I follow first "Remote Command on"
    Then I wait until I see "Action completion: completed" text, refreshing the page
    And file "/usr/share/mgr-transactional-test" should exist on "slmicro62_minion"

  Scenario: Run a remote command on the live system without transactional update
    Given file "/tmp/mgr-live-test" should not exist on "slmicro62_minion"
    And I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Remote Command"
    And I enter "#!/bin/sh\ntouch /tmp/mgr-live-test" as "script_body" text area
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled" text
    When I wait until file "/tmp/mgr-live-test" exists on "slmicro62_minion"
    And I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should not see a "Waiting for reboot" text

  Scenario: Run the continuation at once when no reboot is needed
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I wait until the system "slmicro62_minion" has a completed "Apply highstate" event newer than its latest "Remote Command on" event
    When I refresh the page
    And I follow first "Apply highstate scheduled by admin"
    Then I should see a "Post-reboot step: completed" text
    And I should see a "Prerequisite result:" text
    And I should not see a "Waiting for reboot" text

  Scenario: Cleanup: remove the file created by the remote command
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Remote Command"
    And I enter "#!/bin/sh\nrm -f /usr/share/mgr-transactional-test" as "script_body" text area
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
    And file "/usr/share/mgr-transactional-test" should not exist on "slmicro62_minion"

  Scenario: Cleanup: remove the package installed from the package list
    Given I am on the Systems overview page of this "slmicro62_minion"
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I click on "Update Package List"
    Then I should see a "You have successfully scheduled a package profile refresh" text
    When I wait until refresh package list on "slmicro62_minion" is finished
    And I follow "List / Remove"
    And I enter the package for "slmicro62_minion" as the filtered package name
    And I click on the filter button
    And I check the package for "slmicro62_minion" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    When I wait until event "Package Removal" is completed
    And I follow "Details" in the content area
    And I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    When I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "Reboot completed." text
    When I follow "Details" in the content area
    Then I wait until I do not see "There is a pending transaction for this system" text, refreshing the page
