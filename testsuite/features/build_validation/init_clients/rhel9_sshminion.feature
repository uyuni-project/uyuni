# Copyright (c) 2022-2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new RHEL 9 minion via salt-ssh
#  2) subscribe it to a base channel for testing
#
#  The client is a UBI container running on an EL 9 host. Its sshd listens on
#  port 2222, because the host sshd already uses port 22.

@rhel9_sshminion
Feature: Bootstrap a RHEL 9 Salt SSH minion

  Scenario: Clean up sumaform leftovers on a RHEL 9 Salt SSH minion
    When I perform a full salt minion cleanup on "rhel9_sshminion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a RHEL 9 Salt SSH minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "rhel9_sshminion" as "hostname"
    And I enter "2222" as "port"
    And I enter "linux" as "password"
    And I select "1-rhel9_sshminion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "rhel9_sshminion"

@proxy
  Scenario: Check connection from RHEL 9 Salt SSH minion to proxy
    Given I am on the Systems overview page of this "rhel9_sshminion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of RHEL 9 Salt SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "rhel9_sshminion" hostname

  Scenario: Check events history for failures on RHEL 9 Salt SSH minion
    Given I am on the Systems overview page of this "rhel9_sshminion"
    Then I check for failed events on history event page
