# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.


@scope_hub
Feature: Create an activation key for sles15sp7, ubuntu2404, and proxy on peripheral1
  In order to register systems to the spacewalk server
  As the testing user
  I want to use activation keys

  Scenario: Create an activation key with the channel and child channels for sles15sp7
    When I create an activation key including custom channels for "sles15sp7_minion" via API on peripheral1

  Scenario: Create an activation key with the channel and child channels for proxy
    When I create an activation key including custom channels for "proxy2" via API on peripheral1

  Scenario: Create an activation key with the channel and child channels for ubuntu2404
    When I create an activation key including custom channels for "ubuntu2404_minion" via API on peripheral1
