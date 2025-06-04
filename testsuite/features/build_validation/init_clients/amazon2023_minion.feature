# Copyright (c) 2025 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Amazon 2023 minion
#  2) subscribe it to a base channel for testing

@amazon2023_minion
Feature: Bootstrap a Amazon 2023 Salt minion

  Scenario: Clean up sumaform leftovers on a Amazon 2023 Salt minion
    When I perform a full salt minion cleanup on "amazon2023_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a Amazon 2023 Salt minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "amazon2023_minion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-amazon2023_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "amazon2023_minion"

@proxy
  Scenario: Check connection from Amazon 2023 Salt minion to proxy
    Given I am on the Systems overview page of this "amazon2023_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of Amazon 2023 Salt minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "amazon2023_minion" hostname

  Scenario: Check events history for failures on Amazon 2023 Salt minion
    Given I am on the Systems overview page of this "amazon2023_minion"
    Then I check for failed events on history event page
