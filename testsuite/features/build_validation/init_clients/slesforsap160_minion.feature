# Copyright (c) 2025-2026 SUSE LLC
# Licensed under the terms of the MIT license.

@slesforsap160_minion
Feature: Bootstrap a SLES for SAP 16.0 Salt minion

  Scenario: Clean up sumaform leftovers on a SLES for SAP 16.0 Salt minion
    When I perform a full salt minion cleanup on "slesforsap160_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a SLES for SAP 16.0 minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "slesforsap160_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-slesforsap160_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "slesforsap160_minion"

  Scenario: Check the new bootstrapped SLES for SAP 16.0 minion in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    And the Salt master can reach "slesforsap160_minion"

@proxy
  Scenario: Check connection from SLES for SAP 16.0 minion to proxy
    Given I am on the Systems overview page of this "slesforsap160_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of SLES for SAP 16.0 minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "slesforsap160_minion" hostname

  Scenario: Install the HA packages on the SLES for SAP 16.0 minion
    When I install "pacemaker" on "slesforsap160_minion" using the API
    And I install "crmsh" on "slesforsap160_minion" using the API
    Then I wait for "pacemaker" to be installed on "slesforsap160_minion"
    And I wait for "crmsh" to be installed on "slesforsap160_minion"

  Scenario: Check events history for failures on SLES for SAP 16.0 minion
    Given I am on the Systems overview page of this "slesforsap160_minion"
    Then I check for failed events on history event page
