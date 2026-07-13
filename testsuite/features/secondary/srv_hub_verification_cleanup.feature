# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@server2
Feature: Hub peripheral deregistration and state cleanup
  In order to restore a clean state after hub testing
  As an authorized user
  I want to deregister a peripheral from both sides and verify side effects are correct (plan A-10)

  Scenario: Log in as admin user for cleanup
    Given I am authorized for the "Admin" section

  Scenario: Prerequisite - register server2 and sync a channel for deregistration tests (A-10)
    When I add "server2" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    And I configure hub to sync channel "clone-fake-rpm-suse-channel" to "server2"
    And I trigger channel sync from hub to "server2"
    And I wait at most 600 seconds until channel "clone-fake-rpm-suse-channel" has been synced on "server2"
    Then channel "clone-fake-rpm-suse-channel" should exist on "server2"

  Scenario: Log in as admin user on server2 for deregistration test (A-10)
    Given I am authorized for the "Admin" section on "server2"

  Scenario: Deregister server2 from hub initiated on the peripheral side (A-10)
    When I deregister from hub on "server2"
    Then the Hub Details page on "server2" should be empty

  Scenario: Verify server2 no longer appears in hub peripherals list after peripheral-side deregistration (A-10)
    Then I should not see "server2" in peripherals list on hub

  Scenario: Verify previously synced channels still exist on server2 after deregistration (A-10)
    Then channel "clone-fake-rpm-suse-channel" should exist on "server2"

  Scenario: Re-register server2 to hub successfully after peripheral-side deregistration (A-10)
    When I add "server2" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "server2" in peripherals list

  Scenario: Deregister server2 from hub side to verify hub-initiated path (A-10)
    When I unregister "server2" from hub
    Then I should not see the name of "server2"

  Scenario: Verify server2 no longer appears in peripherals list after hub-side deregistration (A-10)
    When I follow the left menu "Admin > Hub Configuration > Peripherals Configuration"
    Then I should not see the name of "server2"
