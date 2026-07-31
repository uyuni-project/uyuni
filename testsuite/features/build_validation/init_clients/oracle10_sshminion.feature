# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Oracle 10 minion via salt-ssh
#  2) subscribe it to a base channel for testing

@oracle10_sshminion
Feature: Bootstrap an Oracle 10 Salt SSH minion

  Scenario: Clean up sumaform leftovers on an Oracle 10 Salt SSH minion
    When I perform a full salt minion cleanup on "oracle10_sshminion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap an Oracle 10 Salt SSH minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "oracle10_sshminion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-oracle10_sshminion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "oracle10_sshminion"

@proxy
  Scenario: Check connection from Oracle 10 Salt SSH minion to proxy
    Given I am on the Systems overview page of this "oracle10_sshminion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of Oracle 10 Salt SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "oracle10_sshminion" hostname

@monitoring_server
  Scenario: Prepare Oracle 10 Salt SSH minion firewall for monitoring
    When I enable firewall ports for monitoring on this "oracle10_sshminion"

  Scenario: Check events history for failures on Oracle 10 Salt SSH minion
    Given I am on the Systems overview page of this "oracle10_sshminion"
    Then I check for failed events on history event page
