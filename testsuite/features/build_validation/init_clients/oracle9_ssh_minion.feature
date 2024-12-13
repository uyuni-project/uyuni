# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Oracle 9 minion via salt-ssh
#  2) subscribe it to a base channel for testing

@oracle9_ssh_minion
Feature: Bootstrap a Oracle 9 Salt SSH minion

  Scenario: Clean up sumaform leftovers on a Oracle 9 Salt SSH minion
    When I perform a full salt minion cleanup on "oracle9_ssh_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a Oracle 9 Salt SSH minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "oracle9_ssh_minion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-oracle9_ssh_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    # workaround for bsc#1222108
    And I wait at most 480 seconds until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "oracle9_ssh_minion"

@proxy
  Scenario: Check connection from Oracle 9 Salt SSH minion to proxy
    Given I am on the Systems overview page of this "oracle9_ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of Oracle 9 Salt SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "oracle9_ssh_minion" hostname

@monitoring_server
  Scenario: Prepare Oracle 9 Salt SSH minion firewall for monitoring
    When I enable firewall ports for monitoring on this "oracle9_ssh_minion"

  Scenario: Check events history for failures on Oracle 9 Salt SSH minion
    Given I am on the Systems overview page of this "oracle9_ssh_minion"
    Then I check for failed events on history event page
