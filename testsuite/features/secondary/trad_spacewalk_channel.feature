# Copyright (c) 2015-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_spacewalk_utils
Feature: Listing, adding and removing channels using the spacewalk-channel command
  In order to list available channels and add or remove them
  As admin user
  I want to use the spacewalk-channel command

  Scenario: List available channels, verify child channels
    When I use spacewalk-channel to list available channels
    Then I should get "sle15-sp4-installer-updates-x86_64"

  Scenario: Add an invalid child channel
    # bsc#875958 - spacewalk-channel error code is 0 although channel adding failed
    Then spacewalk-channel should fail adding "test_child_channel"

  Scenario: Add a valid child channel
    When I use spacewalk-channel to add "sle15-sp4-installer-updates-x86_64"
    And I use spacewalk-channel to list channels
    Then I should get "sle15-sp4-installer-updates-x86_64"

  Scenario: Remove a valid child channel
    When I use spacewalk-channel to remove "sle15-sp4-installer-updates-x86_64"
    And I use spacewalk-channel to list channels
    Then I shouldn't get "sle15-sp4-installer-updates-x86_64"
