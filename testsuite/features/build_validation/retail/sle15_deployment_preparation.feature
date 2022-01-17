# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@sle15sp3_terminal
@sle15sp3_buildhost
Feature: Prepare prerequisities for SLE15 SP3 terminal deployment

  Scenario: Prepare activation keys for SLE15 SP3 retail systems
    When I create an activation key including custom channels for "sle15sp3_buildhost" via XML-RPC
    And I create an activation key including custom channels for "sle15sp3_terminal" via XML-RPC

  Scenario: Configure semi-xmlrpc-tester for SLE15 SP3 terminal deployment
    When I prepare configuration for "SLE15 SP3" terminal deployment

  Scenario: Configure saltboot formula for SLE15 SP3 terminal deployment
    When I execute "saltboot" for "SLE15 SP3" via semi-xmlrpc-tester
