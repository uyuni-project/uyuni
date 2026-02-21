# Copyright (c) 2022-2025 SUSE LLC
# Licensed under the terms of the MIT license.

@slemicro54_minion
Feature: Migrate a SLE Micro 5.4 Salt minion to SLE Micro 5.5

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Prerequisite: update OS zypper to the latest version
    When I upgrade "slemicro54_minion" with the last "zypper" version

  Scenario: Prerequisite: Reboot the slemicro 5.4 after updating zypper
    When I reboot the "slemicro54_minion" host through SSH, waiting until it comes back

  Scenario: Migrate this minion to SLE Micro 5.5
    Given I am on the Systems overview page of this "slemicro54_minion"
    When I follow "Software" in the content area
    And I follow "Product Migration" in the content area
    And I wait until I see "Target Products:" text, refreshing the page
    And I wait until I see "SUSE Linux Enterprise Micro 5.5 x86_64" text
    And I click on "Select Channels"
    When I select the channel "Custom Channel for slemicro55_minion"
    And I check "allowVendorChange"
    And I click on "Schedule Migration"
    Then I should see a "Product Migration - Confirm" text
    When I click on "Confirm"
    Then I should see a "This system is scheduled to be migrated to" text

  Scenario: Check the migration is successful for this minion
    Given I am on the Systems overview page of this "slemicro54_minion"
    When I follow "Events"
    And I follow "History"
    And I wait at most 600 seconds until event "Product Migration" is completed
    And I wait until event "Package List Refresh" is completed
    And I reboot the "slemicro54_minion" minion through the web UI
    And I follow "Details" in the content area
    Then I wait until I see "SUSE Linux Enterprise Micro 5.5 x86_64" text, refreshing the page
    And vendor change should be enabled for product migration on "slemicro54_minion"

  Scenario: Verify the SLE Micro minion is subscribed to SLE Micro 5.5 child channels
    Given I am on the Systems overview page of this "slemicro54_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then I should see the child channel "ManagerTools-SLE-Micro5-Pool for x86_64 5.5" "selected"

  Scenario: Detect latest Salt changes on the SLE Micro minion
    When I query latest Salt changes on "slemicro54_minion"

  Scenario: Check events history for failures on the SLE Micro minion
    Given I am on the Systems overview page of this "slemicro54_minion"
    Then I check for failed events on history event page
