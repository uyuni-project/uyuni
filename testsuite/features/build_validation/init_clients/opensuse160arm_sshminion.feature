# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@opensuse160arm_sshminion
Feature: Bootstrap an openSUSE 16.0 ARM Salt SSH minion

  Scenario: Clean up sumaform leftovers on an openSUSE 16.0 ARM Salt SSH minion
    When I perform a full salt minion cleanup on "opensuse160arm_sshminion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap an openSUSE 16.0 ARM system managed via salt-ssh
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "opensuse160arm_sshminion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-opensuse160arm_sshminion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "opensuse160arm_sshminion"

@proxy
  Scenario: Check connection from openSUSE 16.0 ARM SSH minion to proxy
    Given I am on the Systems overview page of this "opensuse160arm_sshminion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of openSUSE 16.0 ARM SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "opensuse160arm_sshminion" hostname

  Scenario: Check events history for failures on openSUSE 16.0 ARM SSH minion
    Given I am on the Systems overview page of this "opensuse160arm_sshminion"
    Then I check for failed events on history event page
