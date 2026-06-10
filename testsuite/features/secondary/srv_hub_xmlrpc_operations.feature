# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Hub XMLRPC API operations
  Test Hub XMLRPC API multicast operations
  Verify auto-connect authentication and system listing

  Scenario: Connect to Hub XMLRPC API with auto-connect mode
    Given I am connected to the hub XMLRPC API

  Scenario: List server IDs via Hub API
    When I call hub.listServerIds via XMLRPC
    Then "peripheral_server" should be in the server IDs list

  Scenario: Register test minion to hub server
    Given I am authorized
    When I go to the bootstrapping page
    And I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-KEY-x86_64" from "activationKeys"
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "sle_minion"

  Scenario: Execute multicast system list
    Given I am connected to the hub XMLRPC API
    When I call hub.listServerIds via XMLRPC
    And I call multicast.system.list_systems via XMLRPC
    Then multicast response should have successful responses

  Scenario: Verify multicast response contains systems
    Then multicast response should contain systems from "server"

  Scenario: Logout from Hub API
    When I logout from hub XMLRPC API
