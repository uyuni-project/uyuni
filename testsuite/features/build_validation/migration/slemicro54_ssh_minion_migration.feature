# Copyright (c) 2022-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@slemicro54_ssh_minion
Feature: Migrate a SLE Micro 5.4 Salt SSH minion to SLE Micro 5.5

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Migrate this SSH minion to SLE Micro 5.5
    Given I am on the Systems overview page of this "slemicro54_ssh_minion"
    When I follow "Software" in the content area
    And I follow "Product Migration" in the content area
    And I wait until I see "Target Products:" text, refreshing the page
    And I wait until I see "SUSE Linux Enterprise Micro 5.5 x86_64" text
    And I click on "Select Channels"
    And I check "allowVendorChange"
    And I click on "Schedule Migration"
    Then I should see a "Product Migration - Confirm" text
    When I click on "Confirm"
    Then I should see a "This system is scheduled to be migrated to" text

  Scenario: Check the migration is successful for this SSH minion
    Given I am on the Systems overview page of this "slemicro54_ssh_minion"
    When I follow "Events"
    And I follow "History"
    And I wait until event "Apply states" is completed
    And I wait at most 600 seconds until event "Product Migration" is completed
    And I wait until event "Package List Refresh" is completed
    And I reboot the "slemicro54_ssh_minion" minion through the web UI
    And I follow "Details" in the content area
    Then I should see a "SUSE Linux Enterprise Micro 5.5 x86_64" text
    And vendor change should be enabled for product migration on "slemicro54_ssh_minion"

  Scenario: Install the latest Salt on this SSH minion
    When I migrate the non-SUMA repositories on "slemicro54_ssh_minion"
    And I enable repositories before installing Salt on this "slemicro54_ssh_minion"
    And I install Salt packages from "slemicro54_ssh_minion"
    And I reboot the "slemicro54_ssh_minion" minion through the web UI
    And I disable repositories after installing Salt on this "slemicro54_ssh_minion"

  Scenario: Subscribe the SSH-managed SLE Micro minion to a base channel
    Given I am on the Systems overview page of this "slemicro54_ssh_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Base-Channel-x86_64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

  Scenario: Check events history for failures on SSH minion
    Given I am on the Systems overview page of this "slemicro54_ssh_minion"
    Then I check for failed events on history event page
