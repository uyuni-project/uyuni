# Copyright (c) 2020-2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Rocky 8 minion
#  2) subscribe it to a base channel for testing

@rocky8_minion
Feature: Bootstrap a Rocky 8 Salt minion

  Scenario: Clean up sumaform leftovers on a Rocky 8 Salt minion
    When I perform a full salt minion cleanup on "rocky8_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a Rocky 8 Salt minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "rocky8_minion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-rocky8_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "rocky8_minion"

@proxy
  Scenario: Check connection from Rocky 8 Salt minion to proxy
    Given I am on the Systems overview page of this "rocky8_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of Rocky 8 Salt minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "rocky8_minion" hostname

  Scenario: Check events history for failures on Rocky 8 Salt minion
    Given I am on the Systems overview page of this "rocky8_minion"
    Then I check for failed events on history event page
