# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@peripheral2
Feature: Hub ISSv3 channel synchronization to peripheral2
  In order to distribute content from a hub to peripheral servers
  As an authorized user
  I want to synchronize channels via the hub UI and peripheral UI

  Scenario: Log in as admin user for channel sync tests
    Given I am authorized for the "Admin" section

  Scenario: Sync the SL Micro 6.2 channels from hub to peripheral2 for minion bootstrap
    When I configure hub to sync all "sl-micro-6.2" channels to "peripheral2"

  Scenario: Sync the Rocky Linux 10 channels from hub to peripheral2 for minion bootstrap
    When I configure hub to sync all "rockylinux-10" channels to "peripheral2"

  Scenario: Trigger channel sync from hub to peripheral2
    Given I am authorized for the "Admin" section on "peripheral2"
    When I initiate channel sync from peripheral "peripheral2"
    Then I should see a "Successfully scheduled a channels synchronization." text

  Scenario: Wait for SL Micro 6.2 channels to be synchronized on peripheral2
    And I wait until all synchronized channels for "sl-micro-6.2" have finished on peripheral2

  Scenario: Wait for Rocky Linux 10 channels to be synchronized on peripheral2
    And I wait until all synchronized channels for "rockylinux10" have finished on peripheral2

  Scenario: Verify all channels are solved
    When I wait until all synchronized channels have solved their dependencies on peripheral2
    Then all channels have been synced without errors
