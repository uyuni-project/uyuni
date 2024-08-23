# Copyright (c) 2022-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@sle15sp3_ssh_minion
Feature: Migrate a SLES 15 SP3 Salt SSH minion to 15 SP4

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Migrate this SSH minion to SLE 15 SP4
    Given I am on the Systems overview page of this "sle15sp3_ssh_minion"
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

  Scenario: Check the migration is successful for this SSH minion
    Given I am on the Systems overview page of this "sle15sp3_ssh_minion"
    When I follow "Events"
    And I follow "History"
    And I wait until event "Apply states" is completed
    And I wait at most 600 seconds until event "Product Migration" is completed
    And I wait until event "Package List Refresh" is completed
    And I follow "Details" in the content area
    Then I should see a "SUSE Linux Enterprise Server 15 SP4" text
    And vendor change should be enabled for product migration on "sle15sp3_ssh_minion"

  Scenario: Install the latest Salt on this SSH minion
    When I migrate the non-SUMA repositories on "sle15sp3_ssh_minion"
    And I enable repositories before installing Salt on this "sle15sp3_ssh_minion"
    And I install Salt packages from "sle15sp3_ssh_minion"
    And I disable repositories after installing Salt on this "sle15sp3_ssh_minion"

  Scenario: Subscribe the SSH-managed SLES minion to a SLES 15 SP4 child channel
    Given I am on the Systems overview page of this "sle15sp3_ssh_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I should see the child channel "SLE15-SP4-Installer-Updates for x86_64" "unselected"
    When I select the child channel "SLE15-SP4-Installer-Updates for x86_64"
    Then I should see the child channel "SLE15-SP4-Installer-Updates for x86_64" "selected"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

  Scenario: Check events history for failures on SSH minion
    Given I am on the Systems overview page of this "sle15sp3_ssh_minion"
    Then I check for failed events on history event page
