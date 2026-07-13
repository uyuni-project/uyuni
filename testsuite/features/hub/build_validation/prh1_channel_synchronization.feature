# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@server2
Feature: Hub ISSv3 channel synchronization to peripheral
  In order to distribute content from a hub to peripheral servers
  As an authorized user
  I want to synchronize channels via the hub UI and peripheral UI (plan A-06)

  Scenario: Log in as admin user for channel sync tests
    Given I am authorized for the "Admin" section

  Scenario: Sync the SLES 15 SP7 base channel and its modules from hub to server2 for minion bootstrap
    When I configure hub to sync all "-SP7-" channels to "server2"

  Scenario: Wait for SLE-Product-SLES15-SP7-Pool channel to be synchronized on server2 (A-06)
    And I wait until all synchronized channels for "sles15-sp7" have finished on server2

  Scenario: Verify all channels are solved
    When I wait until all synchronized channels have solved their dependencies on server2
    Then all channels have been synced without errors

  Scenario: Verify SLE-Product-SLES15-SP7-Pool channel on server2 has expected packages (A-06)
    Then channel "sle-product-sles15-sp7-pool-x86_64" on "server2" should have "3" packages
