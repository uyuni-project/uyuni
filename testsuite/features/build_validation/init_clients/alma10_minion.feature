# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Alma 10 minion
#  2) subscribe it to a base channel for testing

@alma10_minion
Feature: Bootstrap an Alma 10 Salt minion

  Scenario: Clean up sumaform leftovers on an Alma 10 Salt minion
    When I perform a full salt minion cleanup on "alma10_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap an Alma 10 Salt minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "alma10_minion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-alma10_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "alma10_minion"

@proxy
  Scenario: Check connection from Alma 10 Salt minion to proxy
    Given I am on the Systems overview page of this "alma10_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of Alma 10 Salt minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "alma10_minion" hostname

  Scenario: Check events history for failures on Alma 10 Salt minion
    Given I am on the Systems overview page of this "alma10_minion"
    Then I check for failed events on history event page
