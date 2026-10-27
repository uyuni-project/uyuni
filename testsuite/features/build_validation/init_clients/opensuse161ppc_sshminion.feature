# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@opensuse161ppc_sshminion
Feature: Bootstrap an openSUSE 16.1 PowerPC Salt SSH minion

  Scenario: Clean up sumaform leftovers on an openSUSE 16.1 PowerPC Salt SSH minion
    When I perform a full salt minion cleanup on "opensuse161ppc_sshminion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap an openSUSE 16.1 PowerPC Salt SSH minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "opensuse161ppc_sshminion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-opensuse161ppc_sshminion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "opensuse161ppc_sshminion"

@proxy
  Scenario: Check connection from openSUSE 16.1 PowerPC Salt SSH minion to proxy
    Given I am on the Systems overview page of this "opensuse161ppc_sshminion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of openSUSE 16.1 PowerPC Salt SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "opensuse161ppc_sshminion" hostname

  Scenario: Check events history for failures on openSUSE 16.1 PowerPC Salt SSH minion
    Given I am on the Systems overview page of this "opensuse161ppc_sshminion"
    Then I check for failed events on history event page
