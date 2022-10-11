# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@opensuse154arm_minion
Feature: Bootstrap a openSUSE 15.4 ARM Salt minion

  Scenario: Clean up sumaform leftovers on a openSUSE 15.4 ARM Salt minion
    When I perform a full salt minion cleanup on "opensuse154arm_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a openSUSE 15.4 ARM minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "opensuse154arm_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-opensuse154arm_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "opensuse154arm_minion"

  Scenario: Check the new bootstrapped openSUSE 15.4 ARM minion in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    And the Salt master can reach "opensuse154arm_minion"

@proxy
  Scenario: Check connection from openSUSE 15.4 ARM minion to proxy
    Given I am on the Systems overview page of this "opensuse154arm_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of openSUSE 15.4 ARM minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "opensuse154arm_minion" hostname

  Scenario: Check events history for failures on openSUSE 15.4 ARM minion
    Given I am on the Systems overview page of this "opensuse154arm_minion"
    Then I check for failed events on history event page
