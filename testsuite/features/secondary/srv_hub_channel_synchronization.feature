# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Hub channel synchronization
  Synchronize software channels from hub to peripheral server
  Test vendor-style, custom, and cloned channel types

  Scenario: Prerequisites - peripheral is registered
    Given I am authorized
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Peripherals Configuration"
    Then I should see the name of "peripheral_server"

  Scenario: Clone a channel on hub for testing
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "Fake-RPM-SUSE-Channel" as the origin channel
    And I choose "current"
    And I click on "Clone Channel"
    And I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    And I click on "Clone Channel"
    Then I should see a "Clone of Fake-RPM-SUSE-Channel" text

  Scenario: Verify cloned channel has packages
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone of Fake-RPM-SUSE-Channel"
    And I follow "Packages"
    Then I should see a "Package List" text
    And I should see package "andromeda-dummy"

  Scenario: Configure channel sync from hub to peripheral
    When I configure hub to sync channel "clone-fake-rpm-suse-channel" to "peripheral_server"
    Then I should see a "Channel configuration updated" text

  Scenario: Trigger synchronization from hub
    When I trigger channel sync from hub to "peripheral_server"
    And I wait until I see "Synchronization started" text
    Then I should see a "Background" text

  Scenario: Wait for channel sync to complete
    When I wait at most 600 seconds until channel "clone-fake-rpm-suse-channel" has been synced on "peripheral_server"
    Then channel "clone-fake-rpm-suse-channel" should exist on "peripheral_server"

  Scenario: Verify channel on peripheral has packages
    Then channel "clone-fake-rpm-suse-channel" on "peripheral_server" should have "4" packages

  Scenario: Create custom channel on hub
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Test Hub Custom Channel" as "Channel Name"
    And I enter "test-hub-custom-channel" as "Channel Label"
    And I select "x86_64" from "Channel Architecture"
    And I click on "Create Channel"
    Then I should see a "Channel Test Hub Custom Channel created" text

  Scenario: Configure custom channel sync to peripheral
    When I configure hub to sync channel "test-hub-custom-channel" to "peripheral_server"
    And I select target organization "Test Default Organization" for channel "test-hub-custom-channel" on "peripheral_server"
    Then I should see a "Channel configuration updated" text

  Scenario: Trigger sync for custom channel
    When I trigger channel sync from hub to "peripheral_server"
    And I wait until I see "Synchronization started" text
    And I wait at most 300 seconds until channel "test-hub-custom-channel" has been synced on "peripheral_server"
    Then channel "test-hub-custom-channel" should exist on "peripheral_server"

  Scenario: Test sync initiated from peripheral
    When I initiate channel sync from peripheral "peripheral_server"
    And I wait until I see "Synchronization started" text
    Then I should see a "Background" text
