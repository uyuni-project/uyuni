# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@peripheral2
@slmicro62_minion
Feature: Bootstrap a SL Micro 6.2 Salt minion on peripheral2

  Scenario: Clean up sumaform leftovers on a SL Micro 6.2 Salt minion
    When I perform a full salt minion cleanup on "slmicro62_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section on "peripheral2"

  Scenario: Bootstrap a SL Micro 6.2 minion on peripheral2
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "slmicro62_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-slmicro62_minion_key" from "activationKeys"
    And I select the hostname of "proxy3" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "slmicro62_minion"

  Scenario: Check the new bootstrapped SL Micro 6.2 minion in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    And the Salt master can reach "slmicro62_minion" on peripheral2

@proxy3
  Scenario: Check connection from SL Micro 6.2 minion to proxy
    Given I am on the Systems overview page of this "slmicro62_minion" on peripheral2
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy3" short hostname

@proxy3
  Scenario: Check registration on proxy of SL Micro 6.2 minion
    Given I am on the Systems overview page of this "proxy3" on peripheral2
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "slmicro62_minion" hostname

  Scenario: Check events history for failures on SL Micro 6.2 minion
    Given I am on the Systems overview page of this "slmicro62_minion" on peripheral2
    Then I check for failed events on history event page
