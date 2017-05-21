# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Migrate a traditional client into a salt minion
  In order to move away from traditional clients
  As an authorized user
  I want to migrate these clients to salt minions and have everything as before

  Scenario: Migrate a sles client into a salt minion
     Given I am authorized
     When I follow "Salt"
     And I follow "Bootstrapping"
     # sle-migrated-minion = traditional sles client
     And I enter the hostname of "sle-migrated-minion" as hostname
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I select "1-SUSE-PKG-x86_64" from "activationKeys"
     And I click on "Bootstrap"
     And I wait for "100" seconds
     Then I wait until i see "Successfully bootstrapped host! " text

  Scenario: Verify the minion was bootstrapped with activation key
     Given I am on the Systems overview page of this "sle-migrated-minion"
     Then I should see a "Activation Key: 	1-SUSE-PKG" text

  Scenario: Check that service nhsd has been stopped
     # bsc#1020902 - moving from traditional to salt with bootstrap is not disabling rhnsd
     When I run "systemctl status nhsd" on "sle-migrated-minion" without error control
     Then the command should fail

  Scenario: Check that the migrated system is now a minion
     Given I am on the Systems overview page of this "sle-migrated-minion"
     When I follow "Properties" in the content area
     Then I should see a "Base System Type:     Salt" text

  Scenario: Check that channels are still the same after migration
     Given I am on the Systems overview page of this "sle-migrated-minion"
     Then I should see a "Test-Channel-x86_64" text

  Scenario: Check that events history is still the same after migration
     Given I am on the Systems overview page of this "sle-migrated-minion"
     When I follow "Events" in the content area
     And I follow "History" in the content area
     Then I should see a "System reboot scheduled by admin" text

  Scenario: Install a package onto the migrated minion
     Given I am on the Systems overview page of this "sle-migrated-minion"
     When I follow "Software" in the content area
     And I follow "Install"
     And I check "perseus-dummy-1.1-1.1" in the list
     And I click on "Install Selected Packages"
     And I click on "Confirm"
     And I wait for "5" seconds
     Then I should see a "1 package install has been scheduled for" text
     And I wait for "perseus-dummy-1.1-1.1" to be installed on this "sle-migrated-minion"

  Scenario: Run a remote script on the migrated minion
     Given I am on the Systems overview page of this "sle-migrated-minion"
     When I follow "Remote Command" in the content area
     And I enter as remote command this script in
      """
      #!/bin/bash
      touch /tmp/remote-command-on-migrated-test
      """
     And I click on "Schedule"
     Then I should see a "Remote Command has been scheduled successfully" text
     And "/tmp/remote-command-on-migrated-test" exists on the filesystem of "sle-migrated-minion"
     And I remove "/tmp/remote-command-on-migrated-test" from "sle-migrated-minion"
