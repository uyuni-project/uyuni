# Copyright (c) 2022-2025 SUSE LLC
# Licensed under the terms of the MIT license.

@sles15sp4_sshminion
Feature: Bootstrap a SLES 15 SP4 Salt SSH minion

  Scenario: Clean up sumaform leftovers on a SLES 15 SP4 Salt SSH minion
    When I perform a full salt minion cleanup on "sles15sp4_sshminion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a SLES 15 SP4 system managed via salt-ssh
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "sles15sp4_sshminion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-sles15sp4_sshminion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "sles15sp4_sshminion"

@proxy
  Scenario: Check connection from SLES 15 SP4 SSH minion to proxy
    Given I am on the Systems overview page of this "sles15sp4_sshminion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of SLES 15 SP4 SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sles15sp4_sshminion" hostname

  Scenario: Use correct kernel image on the SLES 15 SP4 SSH minion
    When I remove package "kernel-default-base" from this "sles15sp4_sshminion"
    And I install package "kernel-default" on this "sles15sp4_sshminion"

  Scenario: Reboot the SLES 15 SP4 SSH minion to use the new kernel
    When I reboot the "sles15sp4_sshminion" host through SSH, waiting until it comes back

  Scenario: Check events history for failures on SLES 15 SP4 SSH minion
    Given I am on the Systems overview page of this "sles15sp4_sshminion"
    Then I check for failed events on history event page
