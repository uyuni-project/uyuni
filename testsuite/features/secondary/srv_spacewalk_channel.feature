# Copyright (c) 2015-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_spacewalk_utils
Feature: Listing, adding and removing channels using the spacewalk-channel command
  In order to list available channels and add or remove them
  As admin user
  I want to use the spacewalk-channel command

  Scenario: List available channels, verify child channels
    When I use spacewalk-channel to list available channels
    Then I should get "test-channel-x86_64-child-channel"

  Scenario: Add an invalid child channel
    # bsc#875958 - spacewalk-channel error code is 0 although channel adding failed
    Then spacewalk-channel should fail adding "test_child_channel"

  Scenario: Add a valid child channel
    When I use spacewalk-channel to add "test-channel-x86_64-child-channel"
    And I use spacewalk-channel to list channels
    Then I should get "test-channel-x86_64-child-channel"

  Scenario: Remove a valid child channel
    When I use spacewalk-channel to remove "test-channel-x86_64-child-channel"
    And I use spacewalk-channel to list channels
    Then I shouldn't get "test-channel-x86_64-child-channel"
