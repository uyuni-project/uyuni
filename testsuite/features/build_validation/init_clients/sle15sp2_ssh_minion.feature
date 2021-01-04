# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

@sle15sp2_ssh_minion
Feature: Bootstrap a SLES 15 SP2 Salt SSH Minion

  Scenario: Clean up sumaform leftovers on a SLES 15 SP2 Salt SSH Minion
    When I perform a full salt minion cleanup on "sle15sp2_ssh_minion"

  Scenario: Bootstrap a SLES 15 SP2 system managed via salt-ssh
    Given I am authorized
    And I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    And I check "manageWithSSH"
    And I enter the hostname of "sle15sp2_ssh_minion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-sle15sp2_ssh_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    Then I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "sle15sp2_ssh_minion"

  # WORKAROUD for bsc#1124634
  # Package 'sle-manager-tools-release' is automatically installed during bootstrap and
  # stays installed after removal of channel containing it. So it is not possible to update it.
  # Package needs to be removed from highstate to avoid failure when updating it.
  Scenario: Remove sle-manager-tools-release from state after SLES 15 SP2 bootstrap
    Given I am on the Systems overview page of this "sle15sp2_ssh_minion"
    When I remove package "sle-manager-tools-release" from highstate

  Scenario: Import the GPG keys for SLES 15 SP2 Salt SSH Minion
    When I import the GPG keys for "sle15sp2_ssh_minion"

@proxy
  Scenario: Check connection from SLES 15 SP2 SSH minion to proxy
    Given I am on the Systems overview page of this "sle15sp2_ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of SLES 15 SP2 SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle15sp2_ssh_minion" hostname

  Scenario: Check events history for failures on SLES 15 SP2 SSH minion
    Given I am on the Systems overview page of this "sle15sp2_ssh_minion"
    Then I check for failed events on history event page
