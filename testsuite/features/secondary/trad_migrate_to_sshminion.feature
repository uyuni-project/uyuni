# Copyright (c) 2019-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_traditional_client
Feature: Migrate a traditional client into a Salt SSH minion
  In order to move away from traditional clients
  As an authorized user
  I want to migrate these clients to Salt SSH minions and have everything as before

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Install a package before migration to Salt SSH minion
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Software" in the content area
    And I follow "Install"
    And I check row with "orion-dummy-1.1-1.1" and arch of "sle_client"
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    And I run "rhn_check -vvv" on "sle_client"
    Then I should see a "1 package install has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed
    Then "orion-dummy-1.1-1.1" should be installed on "sle_client"

  Scenario: Change contact method of activation key to ssh-push
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key x86_64" in the content area
    And I select "Push via SSH" from "contactMethodId"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key x86_64 has been modified" text

  Scenario: Migrate a SLES client into a Salt SSH minion
    When I follow the left menu "Systems > Bootstrapping"
    And I enter the hostname of "sle_client" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I check "manageWithSSH"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text

  Scenario: Check that the migrated system is now a SSH minion
    Given I am on the Systems overview page of this "sle_client"
    When I wait until I see the name of "sle_client", refreshing the page
    And I follow "Properties" in the content area
    Then I wait until I see "Base System Type:.*Salt" regex, refreshing the page

@proxy
  Scenario: Check connection from migrated SSH minion to proxy
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of migrated SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle_client" hostname

  # bsc#1020902 - moving from traditional to Salt with bootstrap is not disabling rhnsd
  Scenario: Check that service nhsd has been stopped on migrated SSH minion
    When I run "systemctl status nhsd" on "sle_client" without error control
    Then the command should fail

  Scenario: Check that channels are still the same after migration to Salt SSH
    Given I am on the Systems overview page of this "sle_client"
    Then I should see a "Test-Channel-x86_64" text

  Scenario: Check that events history is still the same after migration to Salt SSH
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should see a "subscribed to channel test-channel-x86_64" text

  Scenario: Install a package to the migrated SSH minion
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Software" in the content area
    And I follow "Install"
    And I check row with "perseus-dummy-1.1-1.1" and arch of "sle_client"
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait for "perseus-dummy-1.1-1.1" to be installed on "sle_client"

  Scenario: Run a remote script on the migrated SSH minion
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

  Scenario: Cleanup: remove packages from the migrated SSH minion
    When I remove package "perseus-dummy-1.1-1.1" from this "sle_client"
    And I remove package "orion-dummy-1.1-1.1" from this "sle_client"

  Scenario: Cleanup: ensure the package information is up to date before migrating back
    When I refresh the metadata for "sle_client"

  Scenario: Cleanup: unregister migrated SSH minion
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_client" should not be registered

  Scenario: Cleanup: register SSH minion again as traditional client
    When I enable client tools repositories on "sle_client"
    And I install the traditional stack utils on "sle_client"
    And I bootstrap traditional client "sle_client" using bootstrap script with activation key "1-SUSE-KEY-x86_64" from the proxy
    Then I should see "sle_client" via spacecmd

  Scenario: Cleanup: change contact method of activation key back to default
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key x86_64" in the content area
    And I select "Default" from "contactMethodId"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key x86_64 has been modified" text

  Scenario: Cleanup: check that the migrated SSH minion is a traditional client again
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Properties" in the content area
    Then I wait until I see "Base System Type:.*Management" regex, refreshing the page
