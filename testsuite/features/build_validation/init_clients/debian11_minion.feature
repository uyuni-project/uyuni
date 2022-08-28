# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Debian 11 minion
#  2) subscribe it to a base channel for testing

@debian11_minion
Feature: Bootstrap a Debian 11 Salt minion

  Scenario: Clean up sumaform leftovers on a Debian 11 Salt minion
    When I perform a full salt minion cleanup on "debian11_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a Debian 11 minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "debian11_minion" as "hostname"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I enter "22" as "port"
    And I select "1-debian11_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "debian11_minion"

@proxy
  Scenario: Check connection from Debian 11 minion to proxy
    Given I am on the Systems overview page of this "debian11_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of Debian 11 minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "debian11_minion" hostname

  Scenario: Check events history for failures on Debian 11 minion
    Given I am on the Systems overview page of this "debian11_minion"
    Then I check for failed events on history event page
