# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@sles160_sshminion
Feature: Bootstrap a SLES 16.0 Salt SSH minion

  Scenario: Clean up sumaform leftovers on a SLES 16.0 Salt SSH minion
    When I perform a full salt minion cleanup on "sles160_sshminion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a SLES 16.0 system managed via salt-ssh
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "sles160_sshminion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-sles160_sshminion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "sles160_sshminion"

@proxy
  Scenario: Check connection from SLES 16.0 SSH minion to proxy
    Given I am on the Systems overview page of this "sles160_sshminion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of SLES 16.0 SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sles160_sshminion" hostname

  Scenario: Check events history for failures on SLES 16.0 SSH minion
    Given I am on the Systems overview page of this "sles160_sshminion"
    Then I check for failed events on history event page
