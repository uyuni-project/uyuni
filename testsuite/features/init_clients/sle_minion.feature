# Copyright (c) 2016-2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Bootstrap a Salt minion via the GUI

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a SLES minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text

  Scenario: Check the new bootstrapped minion in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    When I follow the left menu "Systems > Overview"
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

  Scenario: Migrate this minion to SLE 15 SP3
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Product Migration" in the content area
    And I wait until I see "Target Products:" text, refreshing the page
    And I wait until I see "SUSE Linux Enterprise Server 15 SP3 x86_64" text
    And I click on "Select Channels"
    And I check "allowVendorChange"
    And I click on "Schedule Migration"
    Then I should see a "Product Migration - Confirm" text
    When I click on "Confirm"
    Then I should see a "This system is scheduled to be migrated to" text

  Scenario: Check the migration is successful for this minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Events"
    And I follow "History"
    And I wait at most 600 seconds until event "Product Migration" is completed
    And I wait until event "Package List Refresh" is completed
    And I follow "Details" in the content area
    Then I wait until I see "SUSE Linux Enterprise Server 15 SP3" text, refreshing the page
    And vendor change should be enabled for product migration on "sle_minion"

  Scenario: Install the latest Salt on this minion
    When I migrate the non-SUMA repositories on "sle_minion"
    And I enable repositories before installing Salt on this "sle_minion"
    And I install Salt packages from "sle_minion"
    And I disable repositories after installing Salt on this "sle_minion"

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
