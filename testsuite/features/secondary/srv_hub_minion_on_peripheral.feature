# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_full_topology
@server2
@sle_minion
Feature: Hub full topology - minion managed via peripheral server
  In order to verify end-to-end content delivery in a hub topology
  As an authorized user
  I want to register a peripheral, sync channels, and manage minions through the peripheral (plan B-01..B-04)

  # The two A-08 multicast scenarios below (borrowed from srv_hub_xmlrpc_operations.feature)
  # run here, not there, because this is the only place in run_sets/hub_full_topology.yml
  # where server2 is registered AND a real minion (sle_minion) exists at the same time.
  #
  # This feature intentionally does NOT delete sle_minion in its own cleanup below --
  # srv_hub_outage_resilience.feature (runs later, must be last in the run set) reuses
  # this same minion instance and owns its final cleanup instead.

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Register server2 as a peripheral on the hub (B-01 prerequisite)
    When I add "server2" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "server2" in peripherals list

  Scenario: Sync a channel from hub to server2 for minion bootstrap (B-03 prerequisite)
    When I configure hub to sync channel "clone-fake-rpm-suse-channel" to "server2"
    Then I should see a "Channel configuration updated" text

  Scenario: Trigger channel sync from hub to server2 and wait for completion (B-03 prerequisite)
    When I trigger channel sync from hub to "server2"
    And I wait until I see "Synchronization started" text
    And I wait at most 600 seconds until channel "clone-fake-rpm-suse-channel" has been synced on "server2"
    Then channel "clone-fake-rpm-suse-channel" should exist on "server2"

  Scenario: Create activation key on server2 peripheral with hub-synced channel (B-03)
    When I create an activation key "1-hub-test-key" on "server2" with channel "clone-fake-rpm-suse-channel"
    Then I should see a "1-hub-test-key" text

  @proxy
  Scenario: Verify proxy is registered to server2 with proxy system type before bootstrap (B-02)
    Then I should see "proxy" in "server2" system list as proxy type

  Scenario: Log in as admin user on server2 for minion bootstrap (B-03)
    Given I am authorized for the "Admin" section on "server2"

  Scenario: Bootstrap sle_minion directly to server2 peripheral (B-03)
    Given I am authorized for the "Admin" section on "server2"
    When I bootstrap "sle_minion" to peripheral "server2" using activation key "1-hub-test-key"
    And I wait until onboarding is completed for "sle_minion"
    Then I should see "sle_minion" registered on "server2"

  Scenario: Execute multicast system list across all peripherals (A-08)
    Given I am connected to the hub XMLRPC API
    When I call hub.listServerIds via XMLRPC
    And I call multicast.system.list_systems via XMLRPC
    Then multicast response should have successful responses

  Scenario: Verify multicast response contains systems from peripheral (A-08)
    Then multicast response should contain systems from "server2"

  Scenario: Verify sle_minion is not listed on the hub directly (B-03)
    Then I should not see "sle_minion" registered on hub

  @proxy
  Scenario: Bootstrap sle_minion to server2 via proxy (B-03 via-proxy path)
    Given I am authorized for the "Admin" section on "server2"
    When I bootstrap "sle_minion" to peripheral "server2" using activation key "1-hub-test-key"
    And I wait until onboarding is completed for "sle_minion"
    Then I should see "sle_minion" registered on "server2"

  Scenario: Install a package on sle_minion from hub-synced channel on server2 (B-04)
    Given I am authorized for the "Admin" section on "server2"
    And I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Install" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click the search button
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    And I wait until event "Package Install/Upgrade scheduled by admin" is completed

  Scenario: Verify andromeda-dummy is installed on sle_minion (B-04)
    Given I am authorized for the "Admin" section on "server2"
    And I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click the search button
    Then I should see a "andromeda-dummy" link

  Scenario: Downgrade andromeda-dummy to old version on sle_minion for errata test (B-04)
    When I remove package "andromeda-dummy" from this "sle_minion" without error control
    And I install old package "andromeda-dummy-1.0" on this "sle_minion" without error control
    And I refresh the metadata for "sle_minion"
    And I refresh packages list via spacecmd on "sle_minion"
    And I wait until refresh package list on "sle_minion" is finished

  Scenario: Apply errata andromeda-dummy-6789 on sle_minion via server2 peripheral API (B-04)
    When I apply erratum "andromeda-dummy-6789" on "sle_minion" via "server2" peripheral API
    And I wait for "andromeda-dummy-2.0-1.1" to be installed on "sle_minion"

  Scenario: Verify andromeda-dummy is updated to patched version on sle_minion (B-04)
    Given I am authorized for the "Admin" section on "server2"
    And I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click the search button
    Then I should see a "andromeda-dummy-2.0-1.1" link

  Scenario: Run a remote command on sle_minion via server2 peripheral (B-04)
    When I run a remote command "hostname" on "sle_minion" via "server2"
    Then the remote command should complete on "sle_minion"

  Scenario: Verify package checksum on sle_minion matches hub content (B-04)
    Then the package "andromeda-dummy" checksum on "sle_minion" should match the same package on hub

  Scenario: Cleanup - remove andromeda-dummy from sle_minion
    Given I am authorized for the "Admin" section on "server2"
    And I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click the search button
    And I check "andromeda-dummy" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    And I wait until event "Package Removal scheduled by admin" is completed

  Scenario: Cleanup - remove synced channels from server2
    When I remove synced channels from "server2"
    Then I should see a "Channel configuration updated" text

  Scenario: Cleanup - deregister server2 from hub
    When I unregister "server2" from hub
    Then I should not see the name of "server2"
