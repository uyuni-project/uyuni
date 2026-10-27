# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@slesforsap15sp6_minion
Feature: Bootstrap a SLES for SAP 15 SP6 Salt minion

  Scenario: Clean up sumaform leftovers on a SLES for SAP 15 SP6 Salt minion
    When I perform a full salt minion cleanup on "slesforsap15sp6_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a SLES 15 SP6 minion with the SLES for SAP 15 SP6 activation key
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "slesforsap15sp6_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-slesforsap15sp6_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "slesforsap15sp6_minion"

  Scenario: Check the new bootstrapped SLES 15 SP6 minion in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    And the Salt master can reach "slesforsap15sp6_minion"

@proxy
  Scenario: Check connection from SLES for SAP 15 SP6 minion to proxy
    Given I am on the Systems overview page of this "slesforsap15sp6_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of SLES for SAP 15 SP6 minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "slesforsap15sp6_minion" hostname

  Scenario: Convert the SLES 15 SP6 minion to SLES for SAP 15 SP6
    Given I am on the Systems overview page of this "slesforsap15sp6_minion"
    When I follow "Software" in the content area
    And I follow "Product Migration" in the content area
    And I wait until I see "Target Products:" text, refreshing the page
    And I wait until I see "SUSE Linux Enterprise Server for SAP Applications 15 SP6 x86_64" text
    And I choose "SUSE Linux Enterprise Server for SAP Applications 15 SP6 x86_64" radio button
    And I click on "Select Channels"
    When I select the channel "Custom Channel for slesforsap15sp6_minion"
    And I click on "Schedule Migration"
    Then I should see a "Product Migration - Confirm" text
    When I click on "Confirm"
    Then I should see a "This system is scheduled to be migrated to" text

  Scenario: Check the conversion to SLES for SAP 15 SP6 is successful
    Given I am on the Systems overview page of this "slesforsap15sp6_minion"
    When I follow "Events"
    And I follow "History"
    And I wait at most 600 seconds until event "Product Migration" is completed
    And I wait until event "Package List Refresh" is completed
    And I follow "Details" in the content area
    Then I wait until I see "SUSE Linux Enterprise Server for SAP Applications 15 SP6" text, refreshing the page

  Scenario: Install the HA packages on the SLES for SAP 15 SP6 minion
    When I install "sle-ha-release" on "slesforsap15sp6_minion" using the API
    And I install "pacemaker" on "slesforsap15sp6_minion" using the API
    And I install "crmsh" on "slesforsap15sp6_minion" using the API
    Then I wait for "pacemaker" to be installed on "slesforsap15sp6_minion"
    And I wait for "crmsh" to be installed on "slesforsap15sp6_minion"

  Scenario: Check events history for failures on SLES for SAP 15 SP6 minion
    Given I am on the Systems overview page of this "slesforsap15sp6_minion"
    Then I check for failed events on history event page
