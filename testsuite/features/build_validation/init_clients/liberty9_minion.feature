# Copyright (c) 2023 SUSE LLC
# SPDX-License-Identifier: MIT
#
#  1) bootstrap a new Liberty Linux 9 minion
#  2) subscribe it to a base channel for testing

@susemanager
@liberty9_minion
Feature: Bootstrap a Liberty Linux 9 Salt minion

  Scenario: Clean up sumaform leftovers on a Liberty Linux 9 Salt minion
    When I perform a full salt minion cleanup on "liberty9_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a Liberty Linux 9 Salt minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "liberty9_minion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-liberty9_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "liberty9_minion"

@proxy
  Scenario: Check connection from Liberty Linux 9 Salt minion to proxy
    Given I am on the Systems overview page of this "liberty9_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of Liberty Linux 9 Salt minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "liberty9_minion" hostname

@monitoring_server
  Scenario: Prepare Liberty Linux 9 Salt minion firewall for monitoring
    When I enable firewall ports for monitoring on this "liberty9_minion"

  Scenario: Check events history for failures on Liberty Linux 9 Salt minion
    Given I am on the Systems overview page of this "liberty9_minion"
    Then I check for failed events on history event page
