# Copyright (c) 2026 SUSE LLC
# SPDX-License-Identifier: MIT
#
# Prerequisites: srv_hub_setup_registration.feature must have completed successfully
# (the peripheral host must be registered as a hub minion before bootstrap ordering can be validated).

## WORKAROUND: https://github.com/SUSE/spacewalk/issues/32847
@skip
@scope_hub
@hub_server_to_server
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

  Scenario: Pre-requisite: peripheral1 is neither a system nor a peripheral on the hub
    When I delete "peripheral1" system using the api
    And I unregister "peripheral1" from hub if registered
    And I delete "peripheral1" key in the Salt master
    And I perform a full salt minion cleanup on "peripheral1"
    Then I should not see "peripheral1" hostname

  @susemanager
  Scenario: Create activation key on the hub for the peripheral1 server
    When I create an activation key "hub-server-key" on "server" with channel "sl-micro-6.2-pool-x86_64" and child channels "multi-linux-managertools-sle-16-x86_64-6.2, suse-multi-linux-manager-server-5.2-x86_64"
    And I follow the left menu "Systems > Activation Keys"
    Then I should see a "1-hub-server-key" text

  Scenario: Bootstrap peripheral1 host as a Salt minion of hub
    When I bootstrap "peripheral1" as a Salt minion of hub using activation key "1-hub-server-key"

  # peripheral1 is an SL Micro host: the bootstrap only enables venv-salt-minion in a new snapshot and
  # never reboots, so the minion only starts after a reboot. Unlike the proxy and build_validation SL Micro
  # features, no second reboot is done: the registration states left no pending transaction on peripheral1.
  Scenario: Reboot peripheral1 through SSH to start the Salt minion
    When I reboot the "peripheral1" host through SSH, waiting until it comes back

  Scenario: Wait until peripheral1 is registered and its refreshes are completed
    When I wait at most 600 seconds until "peripheral1" is registered and its refreshes are completed

  Scenario: Wait until the registration states are applied on peripheral1
    When I wait until the registration states are applied on "peripheral1"
    Then I should see "peripheral1" in hub system list as "Salt Minion" type

  Scenario: Register peripheral1 as peripheral after minion bootstrap - verify single entry
    When I add "peripheral1" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then there should be exactly one entry for "peripheral1" in hub system list

  Scenario: Cleanup: delete peripheral1 from hub system list and deregister peripheral
    When I delete "peripheral1" system using the api
    And I unregister "peripheral1" from hub
    And I delete "peripheral1" key in the Salt master
    And I perform a full salt minion cleanup on "peripheral1"
    Then I should not see "peripheral1" hostname
