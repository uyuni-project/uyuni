# Copyright (c) 2019-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Migrate a traditional client into a Salt SSH minion
  In order to move away from traditional clients
  As an authorized user
  I want to migrate these clients to Salt SSH minions and have everything as before

  Scenario: Install a package before migration to Salt SSH minion
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Software" in the content area
    And I follow "Install"
    And I check row with "orion-dummy-1.1-1.1" and arch of "sle_client"
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    And I run "rhn_check -vvv" on "sle_client"
    Then I should see a "1 package install has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled" is completed
    Then "orion-dummy-1.1-1.1" should be installed on "sle_client"

  Scenario: Change contact method of activation key to ssh-push
    Given I am authorized with the feature's user
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test PKG Key x86_64" in the content area
    And I select "Push via SSH" from "contactMethodId"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test PKG Key x86_64 has been modified" text

  Scenario: Migrate a SLES client into a Salt SSH minion
    Given I am authorized with the feature's user
    When I go to the bootstrapping page
    And I enter the hostname of "sle_client" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-PKG-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I check "manageWithSSH"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text

  Scenario: Check that the migrated system is now a SSH minion
    Given I am on the Systems overview page of this "sle_migrated_minion"
    When I wait until I see the name of "sle_migrated_minion", refreshing the page
    And I follow "Properties" in the content area
    Then I wait until I see "Base System Type:     Salt" text, refreshing the page

@proxy
  Scenario: Check connection from migrated SSH minion to proxy
    Given I am on the Systems overview page of this "sle_migrated_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of migrated SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle_migrated_minion" hostname

  # bsc#1020902 - moving from traditional to Salt with bootstrap is not disabling rhnsd
  Scenario: Check that service nhsd has been stopped on migrated SSH minion
    When I run "systemctl status nhsd" on "sle_migrated_minion" without error control
    Then the command should fail

  # bsc#1031081 - old and new activation keys shown for the migrated client
  Scenario: Check that SSH minion only has the new activation key
    Given I am on the Systems overview page of this "sle_migrated_minion"
    Then I should see a "Activation Key:	1-SUSE-PKG-x86_64" text
    And I should not see a "1-SUSE-DEV-x86_64" text

  Scenario: Check that channels are still the same after migration to Salt SSH
    Given I am on the Systems overview page of this "sle_migrated_minion"
    Then I should see a "Test-Channel-x86_64" text

  Scenario: Check that events history is still the same after migration to Salt SSH
    Given I am on the Systems overview page of this "sle_migrated_minion"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should see a "subscribed to channel test-channel-x86_64" text

  Scenario: Install a package to the migrated SSH minion
    Given I am on the Systems overview page of this "sle_migrated_minion"
    When I follow "Software" in the content area
    And I follow "Install"
    And I check row with "perseus-dummy-1.1-1.1" and arch of "sle_migrated_minion"
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait for "perseus-dummy-1.1-1.1" to be installed on "sle_migrated_minion"

  Scenario: Run a remote script on the migrated SSH minion
    Given I am on the Systems overview page of this "sle_migrated_minion"
    When I follow "Remote Command" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      touch /tmp/remote-command-on-migrated-test
      """
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled successfully" text
    When I wait until file "/tmp/remote-command-on-migrated-test" exists on "sle_migrated_minion"
    And I remove "/tmp/remote-command-on-migrated-test" from "sle_migrated_minion"

  Scenario: Cleanup: remove packages from the migrated SSH minion
    When I remove package "perseus-dummy-1.1-1.1" from this "sle_migrated_minion"
    And I remove package "orion-dummy-1.1-1.1" from this "sle_migrated_minion"

  Scenario: Cleanup: unregister migrated SSH minion
    Given I am on the Systems overview page of this "sle_migrated_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_migrated_minion" should not be registered

  Scenario: Cleanup: register SSH minion again as traditional client
    When I enable SUSE Manager tools repositories on "sle_client"
    And I install the traditional stack utils on "sle_client"
    And I install OpenSCAP traditional dependencies on "sle_client"
    And I bootstrap traditional client "sle_client" using bootstrap script with activation key "1-SUSE-DEV-x86_64" from the proxy
    Then I should see "sle_client" via spacecmd

  Scenario: Cleanup: change contact method of activation key back to default
    Given I am authorized with the feature's user
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test PKG Key x86_64" in the content area
    And I select "Default" from "contactMethodId"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test PKG Key x86_64 has been modified" text

  Scenario: Cleanup: check that this minion is a traditional client again
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:     Management" text, refreshing the page

  Scenario: Cleanup: check that we have again the old activation key after migration
    Given I am on the Systems overview page of this "sle_client"
    Then I should see a "Activation Key:	1-SUSE-DEV-x86_64" text
    And I should not see a "1-SUSE-PKG-x86_64" text
