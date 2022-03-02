# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@sle11sp3_terminal
@sle11sp4_buildhost
Feature: Prepare prerequisities for SLE11 SP3 terminal deployment

  Scenario: Prepare activation keys for SLE11 retail systems
    When I create an activation key including custom channels for "sle11sp4_buildhost" via XML-RPC
    And I create an activation key including custom channels for "sle11sp3_terminal" via XML-RPC

  Scenario: Prepare Kiwi profile for SLE11 SP4 build host
    When I prepare Kiwi profile for SLE11 SP4 build host

  Scenario: Configure semi-xmlrpc-tester for SLE11 SP3 terminal deployment
    When I prepare configuration for "SLE11 SP3" terminal deployment

  Scenario: Configure saltboot formula for SLE11 SP3 terminal deployment
    When I execute "saltboot" for "SLE11 SP3" via semi-xmlrpc-tester
