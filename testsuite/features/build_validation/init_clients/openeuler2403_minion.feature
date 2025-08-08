# Copyright (c) 2025 SUSE LLC
# Licensed under the terms of the MIT license.

@openeuler2403_minion
Feature: Bootstrap an openEuler 24.03 x86_64 Salt minion

  Scenario: Clean up sumaform leftovers on a openEuler 24.03 x86_64 Salt minion
    When I perform a full salt minion cleanup on "openeuler2403_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap an openEuler 24.03 x86_64 Salt minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "openeuler2403_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-openeuler2403_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "openeuler2403_minion"

  Scenario: Check the new bootstrapped openEuler 24.03 x86_64 Salt minion in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    And the Salt master can reach "openeuler2403_minion"

@proxy
  Scenario: Check connection from openEuler 24.03 x86_64 Salt minion to proxy
    Given I am on the Systems overview page of this "openeuler2403_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of openEuler 24.03 x86_64 Salt minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "openeuler2403_minion" hostname

  Scenario: Check events history for failures on openEuler 24.03 x86_64 Salt minion
    Given I am on the Systems overview page of this "openeuler2403_minion"
    Then I check for failed events on history event page
