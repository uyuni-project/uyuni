# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Listing and adding/removing channels using the spacewalk-channel command
  In order to test the spacewalk-channel command
  As user root
  I want to be able to list available channels and add or remove them

  Scenario: list available channels, verify child channels
    When I execute spacewalk-channel and pass "--available-channels -u admin -p admin"
    And I should get "test-channel-x86_64-child-channel"

  Scenario: add an invalid child channel (bnc#875958)
    When spacewalk-channel fails with "--add -c test_child_channel -u admin -p admin"

  Scenario: add a valid child channel test-channel
    When I run "spacewalk-channel --add -c test-channel-x86_64-child-channel -u admin -p admin" on "sle-client"

  Scenario: list subscribed channels after adding child
    When I execute spacewalk-channel and pass "--list"
    And I should get "test-channel-x86_64-child-channel"

  Scenario: remove a valid child channel test-channel
    When I use spacewalk-channel to remove test-channel-x86_64-child-channel

  Scenario: list subscribed channels after removing child test-channel
    When I execute spacewalk-channel and pass "--list"
    And I shouldn't get "test-channel-x86_64-child-channel"
