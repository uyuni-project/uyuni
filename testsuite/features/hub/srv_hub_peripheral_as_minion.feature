# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Prerequisites: srv_hub_setup_registration.feature must have completed successfully
# (the peripheral host must be registered as a hub minion before bootstrap ordering can be validated).

@scope_hub
@hub_full_topology
@peripheral1
Feature: Hub peripheral host managed as a hub minion
  In order to understand the correct bootstrap ordering for hub topologies
  As an authorized user
  I want to verify that bootstrapping a peripheral as a hub minion before
  peripheral registration results in a single correct entry (plan B-01)

  # Path 2 (bootstrap after registration) shows two entries per documentation.
  # It is tested here as an observation scenario, not a failure.

  Scenario: Log in as admin user for peripheral-as-minion tests
    Given I am authorized for the "Admin" section

  Scenario: Prerequisite - register peripheral1 as peripheral of hub (B-01)
    When I add "peripheral1" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "peripheral1" in peripherals list

  Scenario: Deregister peripheral1 to set up bootstrap-before-registration path (B-01)
    When I unregister "peripheral1" from hub
    Then I should not see the name of "peripheral1"

  Scenario: Bootstrap peripheral1 host as a Salt minion of hub (B-01 path 1 - minion before peripheral)
    When I bootstrap "peripheral1" as a Salt minion of hub
    And I wait until onboarding is completed for "peripheral1"
    Then I should see "peripheral1" in hub system list as "Salt Minion" type

  Scenario: Register peripheral1 as peripheral after minion bootstrap - verify single entry (B-01)
    When I add "peripheral1" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then there should be exactly one entry for "peripheral1" in hub system list

  Scenario: Document bootstrap-after-registration behavior for path 2 (B-01)
    When I document the two-entries behavior for "peripheral1" when bootstrapped after peripheral registration

  Scenario: Cleanup - delete peripheral1 from hub system list and deregister peripheral (B-01)
    When I delete "peripheral1" system using the api
    And I unregister "peripheral1" from hub
    Then I should not see the name of "peripheral1"
