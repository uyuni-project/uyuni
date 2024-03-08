# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in the following features:
# - features/secondary/trad_migrate_to_sshminion.feature
# If the client fails to be bootstrapped again.

@scope_traditional_client
Feature: Migrate a traditional client into a Salt minion using bootstrap script
  In order to move away from traditional clients
  As an authorized user
  I want to migrate these clients to Salt minions and have everything as before

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Generate a re-activation key
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Reactivation"
    And I click on "Generate New Key"
    Then I should see a "Key:" text

  Scenario: Register the SLES 15 SP4 traditional client as a Salt minion
    When I bootstrap client "sle_client" using bootstrap script with activation key "1-SUSE-KEY-x86_64" and reactivation key from the proxy
    And I accept "sle_client" key in the Salt master

  Scenario: Check that the migrated system is now a minion
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:.*Salt" regex, refreshing the page

@proxy
  Scenario: Check connection from migrated minion to proxy
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of migrated minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle_client" hostname

  # bsc#1020902 - moving from traditional to Salt with bootstrap is not disabling rhnsd
  Scenario: Check that service nhsd has been stopped
    When I run "systemctl status nhsd" on "sle_client" without error control
    Then the command should fail

  Scenario: Check that channels are still the same after migration
    Given I am on the Systems overview page of this "sle_client"
    Then I should see a "SLE-Product-SLES15-SP4-Pool for x86_64" text

  Scenario: Check that events history is still the same after migration
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should see a "subscribed to channel fake-rpm-suse-channel" text

  Scenario: Install a package onto the migrated minion
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Software" in the content area
    And I follow "Install"
    And I enter "perseus-dummy-1.1-1.1" as the filtered package name
    And I click on the filter button
    And I check row with "perseus-dummy-1.1-1.1" and arch of "sle_client"
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait for "perseus-dummy-1.1-1.1" to be installed on "sle_client"

  Scenario: Run a remote script on the migrated minion
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Remote Command" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      touch /tmp/remote-command-on-migrated-test
      """
    And I click on "Schedule"
    Then I should see a "Remote Command has been scheduled successfully" text
    When I wait until file "/tmp/remote-command-on-migrated-test" exists on "sle_client"
    And I remove "/tmp/remote-command-on-migrated-test" from "sle_client"

  Scenario: Cleanup: remove package from the migrated minion
    When I remove package "perseus-dummy-1.1-1.1" from this "sle_client"

  Scenario: Cleanup: ensure the package information is up to date before migrating back
    When I refresh the metadata for "sle_client"

  Scenario: Cleanup: unregister migrated minion
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I wait until Salt client is inactive on "sle_client"
    Then "sle_client" should not be registered

@skip_if_salt_bundle
  Scenario: Cleanup: register minion again as traditional client
    When I enable the repositories "tools_update_repo tools_pool_repo" on this "sle_client"
    And I install the traditional stack utils on "sle_client"
    And I remove package "salt-minion" from this "sle_client"
    And I bootstrap traditional client "sle_client" using bootstrap script with activation key "1-SUSE-KEY-x86_64" from the proxy
    Then I should see "sle_client" via spacecmd

@salt_bundle
  Scenario: Cleanup: register minion again as traditional client
    When I enable the repositories "tools_update_repo tools_pool_repo" on this "sle_client"
    And I install the traditional stack utils on "sle_client"
    And I remove package "venv-salt-minion" from this "sle_client"
    And I bootstrap traditional client "sle_client" using bootstrap script with activation key "1-SUSE-KEY-x86_64" from the proxy
    Then I should see "sle_client" via spacecmd

  Scenario: Cleanup: check that the migrated minion is again a traditional client
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:.*Management" regex, refreshing the page
