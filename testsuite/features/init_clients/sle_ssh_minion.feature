# Copyright (c) 2016-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@ssh_minion
Feature: Bootstrap a Salt host managed via salt-ssh

  Scenario: Register this SSH minion for service pack migration
    Given I am authorized
    And I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    And I check "manageWithSSH"
    And I enter the hostname of "ssh_minion" as "hostname"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I am on the System Overview page
    And I wait until I see the name of "ssh_minion", refreshing the page
    And I wait until onboarding is completed for "ssh_minion"

@proxy
@ssh_minion
  Scenario: Check connection from SSH minion to proxy
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
@ssh_minion
  Scenario: Check registration on proxy of SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ssh_minion" hostname

  Scenario: Migrate this SSH minion to SLE 15 SP2
    Given I am on the Systems overview page of this "ssh_spack_migrated_minion"
    When I follow "Software" in the content area
    And I follow "SP Migration" in the content area
    And I wait until I see "Target Products:" text, refreshing the page
    And I click on "Select Channels"
    And I check "allowVendorChange"
    And I wait until I see "SUSE Linux Enterprise Server 15 SP2 x86_64" text
    And I click on "Schedule Migration"
    And I should see a "Service Pack Migration - Confirm" text
    And I click on "Confirm"
    Then I should see a "This system is scheduled to be migrated to" text

  Scenario: Check the migration is successful for this SSH minion
    Given I am on the Systems overview page of this "ssh_spack_migrated_minion"
    When I follow "Events"
    And I follow "History"
    And I wait at most 600 seconds until event "Service Pack Migration scheduled by admin" is completed
    And I follow "Details" in the content area
    Then I should see a "SUSE Linux Enterprise Server 15 SP2" text
    And vendor change should be enabled for SP migration on "ssh_spack_migrated_minion"

@proxy
  Scenario: Check connection from SSH minion to proxy
    Given I am on the Systems overview page of this "ssh_spack_migrated_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

  Scenario: Install the latest Salt on this minion
    When I enable repositories before installing Salt on this "ssh_spack_migrated_minion"
    And I install Salt packages from "ssh_spack_migrated_minion"
    And I disable repositories after installing Salt on this "ssh_spack_migrated_minion"

  Scenario: Subscribe the SSH-managed SLES minion to a base channel
    Given I am on the Systems overview page of this "ssh_spack_migrated_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

  Scenario: Check events history for failures on SSH minion
    Given I am on the Systems overview page of this "ssh_spack_migrated_minion"
    Then I check for failed events on history event page
