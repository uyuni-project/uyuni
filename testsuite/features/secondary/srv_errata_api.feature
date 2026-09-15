# Copyright (c) 2024 SUSE LLC
# SPDX-License-Identifier: MIT

@scope_api
Feature: Systems errata API

  @sshminion
  Scenario: Check the relevant errata for a system
    When I retrieve the relevant errata for sshminion

  @sshminion
  @rhlike_minion
  @deblike_minion
  Scenario: Check the relevant errata for multiple systems
    When I retrieve the relevant errata for sshminion, rhlike_minion, deblike_minion
