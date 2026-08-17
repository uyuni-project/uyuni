# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@server2
@sles15sp7_minion
Feature: Bootstrap a SLES 15 SP7 Salt minion

  Scenario: Clean up sumaform leftovers on a SLES 15 SP7 Salt minion
    When I perform a full salt minion cleanup on "sles15sp7_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section on "server2"

  Scenario: Bootstrap a SLES 15 SP7 minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sles15sp7_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-sles15sp7_minion_key" from "activationKeys"
    And I select the hostname of "proxy2" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "sles15sp7_minion"

  Scenario: Check the new bootstrapped SLES 15 SP7 minion in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    And the Salt master can reach "sles15sp7_minion" on server2

@proxy2
  Scenario: Check connection from SLES 15 SP7 minion to proxy
    Given I am on the Systems overview page of this "sles15sp7_minion" on server2
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy2" short hostname

@proxy2
  Scenario: Check registration on proxy of SLES 15 SP7 minion
    Given I am on the Systems overview page of this "proxy2" on server2
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sles15sp7_minion" hostname

  Scenario: Check events history for failures on SLES 15 SP7 minion
    Given I am on the Systems overview page of this "sles15sp7_minion" on server2
    Then I check for failed events on history event page
