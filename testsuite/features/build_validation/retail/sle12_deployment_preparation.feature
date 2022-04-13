# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@sle12sp5_terminal
@sle12sp5_buildhost
Feature: Prepare prerequisities for SLE12 SP5 terminal deployment

  Scenario: Prepare activation keys for SLE12 SP5 retail systems
    When I am logged in API as user "admin" and password "admin"
    And I create an activation key including custom channels for "sle12sp5_buildhost" via API
    And I create an activation key including custom channels for "sle12sp5_terminal" via API
    And I logout from API

  Scenario: Configure semi-xmlrpc-tester for SLE12 SP5 terminal deployment
    When I prepare configuration for "SLE12 SP5" terminal deployment

  Scenario: Configure saltboot formula for SLE12 SP5 terminal deployment
    When I execute "saltboot" for "SLE12 SP5" via semi-xmlrpc-tester
