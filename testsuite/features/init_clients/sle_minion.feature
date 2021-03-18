# Copyright (c) 2016-2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Bootstrap a Salt minion via the GUI

  Scenario: Bootstrap a SLES minion
     Given I am authorized
     When I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     When I enter the hostname of "sle_minion" as "hostname"
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I select the hostname of "proxy" from "proxies"
     And I click on "Bootstrap"
     And I wait until I see "Successfully bootstrapped host!" text

  Scenario: Check the new bootstrapped minion in System Overview page
    Given I am authorized
    When I go to the minion onboarding page
    Then I should see a "accepted" text
    When I am on the System Overview page
    And I wait until I see the name of "sle_minion", refreshing the page
    And I wait until onboarding is completed for "sle_minion"
    Then the Salt master can reach "sle_minion"

@proxy
  Scenario: Check connection from minion to proxy
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle_minion" hostname

@service_pack_migration
  Scenario: Migrate this minion to SLE 15 SP2
    Given I am on the Systems overview page of this "sle_spack_migrated_minion"
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

@service_pack_migration
  Scenario: Check the migration is successful for this minion
    Given I am on the Systems overview page of this "sle_spack_migrated_minion"
    When I follow "Events"
    And I follow "History"
    And I wait at most 600 seconds until event "Service Pack Migration scheduled by admin" is completed
    And I follow "Details" in the content area
    Then I should see a "SUSE Linux Enterprise Server 15 SP2" text
    And vendor change should be enabled for "SP migration" on "sle_spack_migrated_minion"

@service_pack_migration
  Scenario: Install the latest Salt on this minion
    When I enable repositories before installing Salt on this "sle_spack_migrated_minion"
    And I install Salt packages from "sle_spack_migrated_minion"
    And I disable repositories after installing Salt on this "sle_spack_migrated_minion"

  Scenario: Subscribe the SLES minion to a base channel
    Given I am on the Systems overview page of this "sle_minion"
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

  # bsc#1085436 - Apache returns 403 Forbidden after a zypper refresh on minion
  Scenario: Check the new channel is working
    When I refresh the metadata for "sle_minion"

  Scenario: Detect latest Salt changes on the SLES minion
    When I query latest Salt changes on "sle_minion"

  Scenario: Check events history for failures on SLES minion
    Given I am on the Systems overview page of this "sle_minion"
    Then I check for failed events on history event page
