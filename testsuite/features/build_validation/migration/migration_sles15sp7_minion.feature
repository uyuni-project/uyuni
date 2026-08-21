# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@sles15sp7_minion 
@sles160_minion
Feature: Migrate a SLES 15 SP7 Salt minion to 16.0

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Prerequisite: update OS zypper to the latest version
    When I upgrade "zypper" on "sles15sp7_minion" using the API

  Scenario: Migrate this minion to SLE 16.0
    Given I am on the Systems overview page of this "sles15sp7_minion"
    When I follow "Software" in the content area
    And I follow "Product Migration" in the content area
    And I wait until I see "Target Products:" text, refreshing the page
    And I wait until I see "SUSE Linux Enterprise Server 16.0 x86_64" text
    And I click on "Select Channels"
    When I select the channel "Custom Channel for sles160_minion"
    And I check "allowVendorChange"
    And I click on "Schedule Migration"
    Then I should see a "Product Migration - Confirm" text
    When I click on "Confirm"
    Then I should see a "This system is scheduled to be migrated to" text

  Scenario: Check the migration is successful for this minion
    Given I am on the Systems overview page of this "sles15sp7_minion"
    When I follow "Events"
    And I follow "History"
    And I wait at most 600 seconds until event "Product Migration" is completed
    And I wait until event "Package List Refresh" is completed
    And I follow "Details" in the content area
    Then I wait until I see "SUSE Linux Enterprise Server 16.0" text, refreshing the page
    And vendor change should be enabled for product migration on "sles15sp7_minion"

  Scenario: Detect latest Salt changes on the SLES minion
    When I query latest Salt changes on "sles15sp7_minion"

  Scenario: Check events history for failures on SLES minion
    Given I am on the Systems overview page of this "sles15sp7_minion"
    Then I check for failed events on history event page
