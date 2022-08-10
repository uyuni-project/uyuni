# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

@sle15sp3_minion
Feature: Migrate a SLES 15 SP3 Salt minion to 15 SP4

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Migrate this minion to SLE 15 SP4
    Given I am on the Systems overview page of this "sle15sp3_minion"
    When I follow "Software" in the content area
    And I follow "Product Migration" in the content area
    And I wait until I see "Target Products:" text, refreshing the page
    And I wait until I see "SUSE Linux Enterprise Server 15 SP4 x86_64" text
    And I click on "Select Channels"
    And I check "allowVendorChange"
    And I click on "Schedule Migration"
    Then I should see a "Product Migration - Confirm" text
    When I click on "Confirm"
    Then I should see a "This system is scheduled to be migrated to" text

  Scenario: Check the migration is successful for this minion
    Given I am on the Systems overview page of this "sle15sp3_minion"
    When I follow "Events"
    And I follow "History"
    And I wait at most 600 seconds until event "Product Migration" is completed
    And I wait until event "Package List Refresh" is completed
    And I follow "Details" in the content area
    Then I wait until I see "SUSE Linux Enterprise Server 15 SP4" text, refreshing the page
    And vendor change should be enabled for product migration on "sle15sp3_minion"

  Scenario: Install the latest Salt on this minion
    When I migrate the non-SUMA repositories on "sle15sp3_minion"
    And I enable repositories before installing Salt on this "sle15sp3_minion"
    And I install Salt packages from "sle15sp3_minion"
    And I disable repositories after installing Salt on this "sle15sp3_minion"

  Scenario: Subscribe the SLES minion to a base channel
    Given I am on the Systems overview page of this "sle15sp3_minion"
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
    When I refresh the metadata for "sle15sp3_minion"

  Scenario: Detect latest Salt changes on the SLES minion
    When I query latest Salt changes on "sle15sp3_minion"

  Scenario: Check events history for failures on SLES minion
    Given I am on the Systems overview page of this "sle15sp3_minion"
    Then I check for failed events on history event page
