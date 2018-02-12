# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Listing, adding and removing channels using the spacewalk-channel command
  Ino order to list available channels and add or remove them
  As root user
  I want to be able to use the spacewalk-channel command

  Scenario: List available channels, verify child channels
    When I execute spacewalk-channel and pass "--available-channels -u admin -p admin"
    And I should get "test-channel-x86_64-child-channel"

  Scenario: Add an invalid child channel
    # bsc#875958 - spacewalk-channel error code is 0 although channel adding failed
    Then spacewalk-channel fails with "--add -c test_child_channel -u admin -p admin"

  Scenario: Add a valid child channel
    When I run "spacewalk-channel --add -c test-channel-x86_64-child-channel -u admin -p admin" on "sle-client"
    And I execute spacewalk-channel and pass "--list"
    Then I should get "test-channel-x86_64-child-channel"

  Scenario: Remove a valid child channel
    When I use spacewalk-channel to remove test-channel-x86_64-child-channel
    And I execute spacewalk-channel and pass "--list"
    Then I shouldn't get "test-channel-x86_64-child-channel"
