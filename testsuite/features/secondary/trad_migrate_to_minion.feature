# Copyright (c) 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_traditional_client
Feature: Migrate a traditional client into a Salt minion
  In order to move away from traditional clients
  As an authorized user
  I want to migrate these clients to Salt minions and have everything as before

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Migrate a SLES client into a Salt minion
    When I follow the left menu "Systems > Bootstrapping"
    And I enter the hostname of "sle_client" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text

  Scenario: Check that the migrated system is now a minion
    Given I am on the Systems overview page of this "sle_migrated_minion"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:     Salt" text, refreshing the page

@proxy
  Scenario: Check connection from migrated minion to proxy
    Given I am on the Systems overview page of this "sle_migrated_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of migrated minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle_migrated_minion" hostname

  # bsc#1020902 - moving from traditional to Salt with bootstrap is not disabling rhnsd
  Scenario: Check that service nhsd has been stopped
    When I run "systemctl status nhsd" on "sle_migrated_minion" without error control
    Then the command should fail

  Scenario: Check that minion has the new activation key
    Given I am on the Systems overview page of this "sle_migrated_minion"
    Then I should see a "Activation Key:	1-SUSE-KEY-x86_64" text

  Scenario: Check that channels are still the same after migration
    Given I am on the Systems overview page of this "sle_migrated_minion"
    Then I should see a "Test-Channel-x86_64" text

  Scenario: Check that events history is still the same after migration
    Given I am on the Systems overview page of this "sle_migrated_minion"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should see a "subscribed to channel test-channel-x86_64" text

  Scenario: Install a package onto the migrated minion
    Given I am on the Systems overview page of this "sle_migrated_minion"
    When I follow "Software" in the content area
    And I follow "Install"
    And I check row with "perseus-dummy-1.1-1.1" and arch of "sle_migrated_minion"
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait for "perseus-dummy-1.1-1.1" to be installed on "sle_migrated_minion"

  Scenario: Run a remote script on the migrated minion
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

  Scenario: Cleanup: remove package from the migrated minion
    When I remove package "perseus-dummy-1.1-1.1" from this "sle_migrated_minion"

  Scenario: Cleanup: ensure the package information is up to date before migrating back
    When I refresh the metadata for "sle_migrated_minion"

  Scenario: Cleanup: unregister migrated minion
    Given I am on the Systems overview page of this "sle_migrated_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_migrated_minion" should not be registered

  Scenario: Cleanup: register minion again as traditional client
    When I enable SUSE Manager tools repositories on "sle_client"
    And I install the traditional stack utils on "sle_client"
    And I remove package "salt-minion" from this "sle_client"
    And I bootstrap traditional client "sle_client" using bootstrap script with activation key "1-SUSE-KEY-x86_64" from the proxy
    Then I should see "sle_client" via spacecmd

  Scenario: Cleanup: check that the migrated minion is again a traditional client
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:     Management" text, refreshing the page

  Scenario: Cleanup: check that we still have the activation key
    Given I am on the Systems overview page of this "sle_client"
    Then I should see a "Activation Key:	1-SUSE-KEY-x86_64" text
