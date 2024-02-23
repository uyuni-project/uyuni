# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_api
Feature: Systems errata API

  @ssh_minion
  Scenario: Check the relevant errata for a system
    When I retrieve the relevant errata for ssh_minion

  @ssh_minion
  @rhlike_minion
  @deblike_minion
  Scenario: Check the relevant errata for multiple systems
    When I retrieve the relevant errata for ssh_minion, rhlike_minion, deblike_minion
