# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.


@scope_hub
Feature: Create an activation key for peripheral2
  In order to register systems to the spacewalk server
  As the testing user
  I want to use activation keys

  Scenario: Create an activation key with the channel and child channels for proxy3
    When I create an activation key including custom channels for "proxy3" via API on peripheral2

  Scenario: Create an activation key with the channel and child channels for slmicro62
    When I create an activation key including custom channels for "slmicro62_minion" via API on peripheral2

  Scenario: Create an activation key with the channel and child channels for rocky10
    When I create an activation key including custom channels for "rocky10_minion" via API on peripheral2
