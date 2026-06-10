# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature should run last in the hub suite — it deregisters peripherals and
# clears hub state, so it will break any subsequent feature that expects the
# peripheral to still be registered.

@scope_hub
@hub_server_to_server
@peripheral1
Feature: Hub peripheral deregistration and state cleanup
  In order to restore a clean state after hub testing
  As an authorized user
  I want to deregister a peripheral from both sides and verify side effects are correct (plan A-10)

  Scenario: Log in as admin user for cleanup
    Given I am authorized for the "Admin" section

  Scenario: Prerequisite - register peripheral1 and sync a channel for deregistration tests (A-10)
    When I add "peripheral1" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    And I configure hub to sync channel "SLE-Product-SLES15-SP7-Pool for x86_64" to "peripheral1"
    When I initiate channel sync from peripheral "peripheral1"
    Then I should see a "Successfully scheduled a channels synchronization." text
    And I wait at most 600 seconds until channel "sle-product-sles15-sp7-pool-x86_64" has been synced on "peripheral1"
    Then channel "sle-product-sles15-sp7-pool-x86_64" should exist on "peripheral1"

  Scenario: Log in as admin user on peripheral1 for deregistration test (A-10)
    Given I am authorized for the "Admin" section on "peripheral1"

  Scenario: Deregister peripheral1 from hub initiated on the peripheral side (A-10)
    When I deregister from hub on "peripheral1"
    Then the Hub Details page on "peripheral1" should be empty

  Scenario: Verify peripheral1 no longer appears in hub peripherals list after peripheral-side deregistration (A-10)
    Then I should not see "peripheral1" in peripherals list on hub

  Scenario: Verify previously synced channels still exist on peripheral1 after deregistration (A-10)
    Then channel "sle-product-sles15-sp7-pool-x86_64" should exist on "peripheral1"

  Scenario: Re-register peripheral1 to hub successfully after peripheral-side deregistration (A-10)
    When I add "peripheral1" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "peripheral1" in peripherals list

  Scenario: Deregister peripheral1 from hub side to verify hub-initiated path (A-10)
    When I unregister "peripheral1" from hub
    Then I should not see the name of "peripheral1"

  Scenario: Verify peripheral1 no longer appears in peripherals list after hub-side deregistration (A-10)
    When I follow the left menu "Admin > Hub Configuration > Peripherals Configuration"
    Then I should not see the name of "peripheral1"
