# Copyright (c) 2020-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@sle11sp4_minion
Feature: Bootstrap a SLES 11 SP4 Salt minion

  Scenario: Clean up sumaform leftovers on a SLES 11 SP4 Salt minion
    When I perform a full salt minion cleanup on "sle11sp4_minion"

  Scenario: Bootstrap a SLES 11 SP4 minion
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle11sp4_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-sle11sp4_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "sle11sp4_minion"

  Scenario: Check the new bootstrapped SLES 11 SP4 minion in System Overview page
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    And the Salt master can reach "sle11sp4_minion"

@proxy
  Scenario: Check connection from SLES 11 SP4 minion to proxy
    Given I am on the Systems overview page of this "sle11sp4_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of SLES 11 SP4 minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle11sp4_minion" hostname

  Scenario: Check spacecmd system ID of bootstrapped minion
    Given I am on the Systems overview page of this "sle11sp4_minion"
    Then I run spacecmd listevents for "sle11sp4_minion"

  Scenario: Check events history for failures on SLES 11 SP4 minion
    Given I am on the Systems overview page of this "sle11sp4_minion"
    Then I check for failed events on history event page
