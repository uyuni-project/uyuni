# Copyright (c) 2026 SUSE LLC
# SPDX-License-Identifier: MIT
#
# This feature can cause failures in:
# If SLES15-SP7 is not available on the hub:
# - features/hub/srv_hub_token_lifecycle.feature
# - features/hub/srv_hub_minion_on_peripheral.feature

@scope_hub
@hub_server_to_server
Feature: Hub prerequisites
  In order to run the hub tests on a standalone hub
  As an authorized user
  I want the SLES15-SP7 channels to be present and synchronized on the hub

  @long_running
  Scenario: Pre-requisite: SLES15-SP7 channels are synchronized on the hub
    When I add the SLES15-SP7 product on the hub unless it is already synced
    And I wait until all synchronized channels for "sles15-sp7" have finished
    And I wait until all synchronized channels have solved their dependencies on hub
    And channel "sle-product-sles15-sp7-pool-x86_64" should exist on "server"
