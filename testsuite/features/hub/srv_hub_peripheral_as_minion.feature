# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_full_topology
@server2
Feature: Hub peripheral host managed as a hub minion
  In order to understand the correct bootstrap ordering for hub topologies
  As an authorized user
  I want to verify that bootstrapping a peripheral as a hub minion before
  peripheral registration results in a single correct entry (plan B-01)

  # Path 2 (bootstrap after registration) shows two entries per documentation.
  # It is tested here as an observation scenario, not a failure.

  Scenario: Log in as admin user for peripheral-as-minion tests
    Given I am authorized for the "Admin" section

  Scenario: Prerequisite - register server2 as peripheral of hub (B-01)
    When I add "server2" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "server2" in peripherals list

  Scenario: Deregister server2 to set up bootstrap-before-registration path (B-01)
    When I unregister "server2" from hub
    Then I should not see the name of "server2"

  Scenario: Bootstrap server2 host as a Salt minion of hub (B-01 path 1 - minion before peripheral)
    When I bootstrap "server2" as a Salt minion of hub
    And I wait until onboarding is completed for "server2"
    Then I should see "server2" in hub system list as "Salt Minion" type

  Scenario: Register server2 as peripheral after minion bootstrap - verify single entry (B-01)
    When I add "server2" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then there should be exactly one entry for "server2" in hub system list

  Scenario: Document bootstrap-after-registration behavior for path 2 (B-01)
    When I document the two-entries behavior for "server2" when bootstrapped after peripheral registration

  Scenario: Cleanup - delete server2 from hub system list and deregister peripheral (B-01)
    When I delete "server2" system using the api
    And I unregister "server2" from hub
    Then I should not see the name of "server2"
