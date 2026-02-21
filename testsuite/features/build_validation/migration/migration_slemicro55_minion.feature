# Copyright (c) 2025 SUSE LLC
# Licensed under the terms of the MIT license.

@slemicro55_minion
Feature: Migrate a SLE Micro 5.5 Salt minion to SL Micro 6.1

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Prerequisite: update OS zypper to the latest version
    When I upgrade "slemicro55_minion" with the last "zypper" version

  Scenario: Prerequisite: Reboot the slemicro 5.5 after updating zypper
    When I reboot the "slemicro55_minion" host through SSH, waiting until it comes back
    
  Scenario: Migrate this minion to SL Micro 6.1
    Given I am on the Systems overview page of this "slemicro55_minion"
    When I follow "Software" in the content area
    And I follow "Product Migration" in the content area
    And I wait until I see "Target Products:" text, refreshing the page
    And I wait until I see "SUSE Linux Micro 6.1 x86_64" text
    When I choose "SUSE Linux Micro 6.1 x86_64" radio button
    And I click on "Select Channels"
    When I select the channel "Custom Channel for slmicro61_minion"
    And I select the channel "ManagerTools-SL-Micro-6.1 for x86_64"
    And I check "allowVendorChange"
    And I click on "Schedule Migration"
    Then I should see a "Product Migration - Confirm" text
    When I click on "Confirm"
    Then I should see a "This system is scheduled to be migrated to" text

  Scenario: Check the migration is successful for this minion
    Given I am on the Systems overview page of this "slemicro55_minion"
    When I follow "Events"
    And I follow "History"
    And I wait at most 600 seconds until event "Product Migration" is completed
    And I wait until event "Package List Refresh" is completed
    And I reboot the "slemicro55_minion" minion through the web UI
    And I follow "Details" in the content area
    Then I wait until I see "SUSE Linux Micro 6.1 x86_64" text, refreshing the page
    And vendor change should be enabled for product migration on "slemicro55_minion"

  Scenario: Verify the SLE Micro minion is subscribed to SL Micro 6.1 child channels
    Given I am on the Systems overview page of this "slemicro55_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then I should see the child channel "ManagerTools-SL-Micro-6.1 for x86_64" "selected"

  Scenario: Detect latest Salt changes on the SLE Micro minion
    When I query latest Salt changes on "slemicro55_minion"

  Scenario: Check events history for failures on the SLE Micro minion
    Given I am on the Systems overview page of this "slemicro55_minion"
    Then I check for failed events on history event page
