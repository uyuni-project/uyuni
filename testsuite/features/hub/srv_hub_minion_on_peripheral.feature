# Copyright (c) 2026 SUSE LLC
# SPDX-License-Identifier: MIT
#
# This feature can cause failures in:
# If minion registration on the peripheral server fails, or if the SLES15-SP7 channels are
# missing on peripheral1 (the prerequisite scenarios below fail loudly instead of skipping):
# - features/hub/srv_hub_reporting.feature

@scope_hub
@hub_server_to_server
@peripheral1
@sle_minion
Feature: Hub - minion managed via peripheral server
  In order to verify end-to-end content delivery in a hub topology
  As an authorized user
  I want to register a peripheral, sync channels, and manage minions through the peripheral (plan B-01..B-04)

  Scenario: Pre-requisite: deregister sle_minion from the hub
    When I delete "sle_minion" system using the api from "server"
    And I perform a full salt minion cleanup on "sle_minion"
    Then "sle_minion" should not be registered

  @susemanager
  Scenario: Pre-requisite: SLES15-SP7 channels are present on peripheral1
    Then channel "sle-product-sles15-sp7-pool-x86_64" should exist on "peripheral1"
    And channel "managertools-sle15-pool-x86_64-sp7" should exist on "peripheral1"
    And channel "managertools-sle15-updates-x86_64-sp7" should exist on "peripheral1"

  Scenario: Pre-requisite: register peripheral1 as peripheral
    Given I am authorized for the "Admin" section
    When I add "peripheral1" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "peripheral1" in peripherals list

  Scenario: Sync the Fake-RPM-SUSE-Channel from hub to peripheral1 for the andromeda-dummy test package
    Given I am authorized for the "Admin" section
    When I wait until the repository metadata of channel "fake-rpm-suse-channel" is generated on the hub
    And I configure hub to sync channel "Fake-RPM-SUSE-Channel" to "peripheral1"
    And I initiate channel sync from peripheral "peripheral1"
    Then I should see a "Successfully scheduled a channels synchronization." text
    When I wait until the channel "fake-rpm-suse-channel" has been synced on peripheral1
    Then channel "fake-rpm-suse-channel" on "peripheral1" should have "3" packages

  Scenario: Create activation key on peripheral1 peripheral with hub-synced channel
    Given I am authorized for the "Admin" section on "peripheral1"
    When I create an activation key "hub-test-key" on "peripheral1" with channel "sle-product-sles15-sp7-pool-x86_64" and child channels "fake-rpm-suse-channel"
    And I follow the left menu "Systems > Activation Keys"
    Then I should see a "1-hub-test-key" text

  Scenario: Sync the SLES15-SP7 channels from hub to peripheral1 for the sle_minion bootstrap
    Given I am authorized for the "Admin" section
    When I configure hub to sync all "sles15-sp7" channels to "peripheral1"
    And I initiate channel sync from peripheral "peripheral1"
    Then I should see a "Successfully scheduled a channels synchronization." text

  Scenario: Create the bootstrap repository on peripheral1 peripheral for sle_minion
    When I wait until all synchronized channels for "sles15-sp7" have finished on peripheral1
    And I create the bootstrap repository for "sle_minion" on peripheral1

  Scenario: Bootstrap sle_minion directly to peripheral1 peripheral
    Given I am authorized for the "Admin" section on "peripheral1"
    When I bootstrap "sle_minion" to peripheral "peripheral1" using activation key "1-hub-test-key"
    And I wait until onboarding is completed for "sle_minion"
    Then I should see "sle_minion" registered on "peripheral1"

  Scenario: Verify sle_minion is not listed on the hub directly
    Then I should see "sle_minion" registered on "peripheral1"
    And I should not see "sle_minion" registered on hub

  Scenario: Install a package on sle_minion from hub-synced channel on peripheral1
    Given I am authorized for the "Admin" section on "peripheral1"
    And I am on the Systems overview page of this "sle_minion" on peripheral1
    When I follow "Software" in the content area
    And I follow "Install" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    And I wait until event "Package Install/Upgrade scheduled by admin" is completed

  Scenario: Verify andromeda-dummy is installed on sle_minion
    Given I am authorized for the "Admin" section on "peripheral1"
    And I am on the Systems overview page of this "sle_minion" on peripheral1
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    Then I should see a "andromeda-dummy" link

  Scenario: Downgrade andromeda-dummy to old version on sle_minion for errata test
    When I remove package "andromeda-dummy" from this "sle_minion" without error control
    And I install old package "andromeda-dummy-1.0" on this "sle_minion" without error control
    And I refresh the metadata for "sle_minion"
    And I refresh packages list via spacecmd on "sle_minion"
    And I wait until refresh package list on "sle_minion" is finished

  Scenario: Apply errata andromeda-dummy-6789 on sle_minion via peripheral1 peripheral API
    When I apply erratum "andromeda-dummy-6789" on "sle_minion" via "peripheral1" peripheral API
    And I wait for "andromeda-dummy-2.0-1.1" to be installed on "sle_minion"

  Scenario: Verify andromeda-dummy is updated to patched version on sle_minion
    Given I am authorized for the "Admin" section on "peripheral1"
    And I am on the Systems overview page of this "sle_minion" on peripheral1
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    Then I should see a "andromeda-dummy-2.0-1.1" link

  Scenario: Run a remote command on sle_minion via peripheral1 peripheral
    When I run a remote command "hostname" on "sle_minion" via "peripheral1"
    Then the remote command should complete

  Scenario: Verify package checksum on sle_minion matches hub content
    Then the package "andromeda-dummy" installed on "sle_minion" should have the same header digest as on hub

  Scenario: Cleanup: remove andromeda-dummy from sle_minion
    Given I am authorized for the "Admin" section on "peripheral1"
    And I am on the Systems overview page of this "sle_minion" on peripheral1
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    And I check "andromeda-dummy" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    And I wait until event "Package Removal scheduled by admin" is completed
