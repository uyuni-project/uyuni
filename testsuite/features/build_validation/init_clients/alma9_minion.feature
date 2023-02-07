# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Alma 9 minion
#  2) subscribe it to a base channel for testing

@alma9_minion
Feature: Bootstrap a Alma 9 Salt minion

  Scenario: Clean up sumaform leftovers on a Alma 9 Salt minion
    When I perform a full salt minion cleanup on "alma9_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a Alma 9 Salt minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "alma9_minion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-alma9_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "alma9_minion"

@proxy
  Scenario: Check connection from Alma 9 Salt minion to proxy
    Given I am on the Systems overview page of this "alma9_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of Alma 9 Salt minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "alma9_minion" hostname

  Scenario: Check events history for failures on Alma 9 Salt minion
    Given I am on the Systems overview page of this "alma9_minion"
    Then I check for failed events on history event page
