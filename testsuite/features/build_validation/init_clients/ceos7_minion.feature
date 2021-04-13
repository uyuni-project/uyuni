# Copyright (c) 2020-2021 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new CentOS 7 minion via salt-ssh
#  2) subscribe it to a base channel for testing

@ceos7_minion
Feature: Bootstrap a CentOS 7 Salt minion

  Scenario: Clean up sumaform leftovers on a  CentOS 7 Salt minion
    When I perform a full salt minion cleanup on "ceos7_minion"

  Scenario: Bootstrap a CentOS 7 Salt minion
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "ceos7_minion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-ceos7_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "ceos7_minion"

@proxy
  Scenario: Check connection from CentOS 7 Salt minion to proxy
    Given I am on the Systems overview page of this "ceos7_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of CentOS 7 Salt minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos7_minion" hostname

  Scenario: Check events history for failures on CentOS 7 Salt minion
    Given I am on the Systems overview page of this "ceos7_minion"
    Then I check for failed events on history event page
