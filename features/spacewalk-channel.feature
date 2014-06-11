# Copyright (c) 2014 SUSE
# Licensed under the terms of the MIT license.

Feature: Listing and adding/removing channels using the spacewalk-channel command
  In order to test the spacewalk-channel command
  As user root
  I want to be able to list available channels and add or remove them

  Scenario: list available channels
    When I execute spacewalk-channel and pass "--available-channels -u admin -p admin"
    Then it should succeed
     And I want to see all valid child channels

  Scenario: add an invalid child channel (bnc#875958)
    When I execute spacewalk-channel and pass "--add -c test_child_channel -u admin -p admin"
    Then it should fail with exit code "1"

  Scenario: add a valid child channel
    When I use spacewalk-channel to add a valid child channel
    Then it should succeed

  Scenario: list subscribed channels after adding child
    When I execute spacewalk-channel and pass "--list"
    Then it should succeed
     And I want to see all valid child channels

  Scenario: remove a valid child channel
    When I use spacewalk-channel to remove a valid child channel
    Then it should succeed

  Scenario: list subscribed channels after removing child
    When I execute spacewalk-channel and pass "--list"
    Then it should succeed
     And I wont see any of the valid child channels

