# Copyright (c) 2026 SUSE LLC
# SPDX-License-Identifier: MIT
#
# This feature can cause failures in:
# If SLES15-SP7 is not available on the hub:
# - features/hub/srv_hub_token_lifecycle.feature
# - features/hub/srv_hub_minion_on_peripheral.feature
# If SL Micro 6.2, the Server Extension 5.2 or the SL Micro bootstrap repository is not available on the hub:
# - features/hub/srv_hub_peripheral_as_minion.feature

@scope_hub
@hub_server_to_server
Feature: Hub prerequisites
  In order to run the hub tests on a standalone hub
  As an authorized user
  I want the SLES15-SP7 and SL Micro 6.2 channels, and the SL Micro 6.2 bootstrap repository, to be present on the hub

  @long_running
  Scenario: Pre-requisite: SLES15-SP7 channels are synchronized on the hub
    When I add the SLES15-SP7 product on the hub unless it is already synced
    And I wait until all synchronized channels for "sles15-sp7" have finished
    And I wait until all synchronized channels have solved their dependencies on hub
    Then all channels have been synced without errors
    And channel "sle-product-sles15-sp7-pool-x86_64" should exist on "server"

  @susemanager
  @long_running
  Scenario: Pre-requisite: SL Micro 6.2 channels are synchronized on the hub
    When I add the SL Micro 6.2 product on the hub unless it is already synced
    And I wait until all synchronized channels for "sl-micro-6.2" have finished
    And I wait until all synchronized channels have solved their dependencies on hub
    Then all channels have been synced without errors
    And channel "sl-micro-6.2-pool-x86_64" should exist on "server"

  # Normally already done by reposync/srv_sync_products.feature, which runs before the hub features.
  @susemanager
  @long_running
  Scenario: Pre-requisite: SUSE MLM Server Extension 5.2 channels are synchronized on the hub
    When I add the SUSE MLM Server Extension 5.2 product on the hub unless it is already synced
    And I wait until all synchronized channels for "suse-multi-linux-manager-server-52" have finished
    And I wait until all synchronized channels have solved their dependencies on hub
    Then all channels have been synced without errors
    And channel "suse-multi-linux-manager-server-5.2-x86_64" should exist on "server"

  @susemanager
  Scenario: Pre-requisite: the SL Micro 6.2 bootstrap repository exists on the hub
    When I create the bootstrap repository for "server" on hub
