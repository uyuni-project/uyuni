# Copyright (c) 2020-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@sle11sp4_ssh_minion
Feature: Bootstrap a SLES 11 SP4 Salt SSH minion

  Scenario: Clean up sumaform leftovers on a SLES 11 SP4 Salt SSH minion
    When I perform a full salt minion cleanup on "sle11sp4_ssh_minion"

  Scenario: Bootstrap a SLES 11 SP4 system managed via salt-ssh
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "sle11sp4_ssh_minion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-sle11sp4_ssh_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "sle11sp4_ssh_minion"

  # WORKAROUND bsc#1181847
  Scenario: Import the GPG keys for SLES 11 SP4 Salt SSH minion
    When I import the GPG keys for "sle11sp4_ssh_minion"

@proxy
  Scenario: Check connection from SLES 11 SP4 SSH minion to proxy
    Given I am on the Systems overview page of this "sle11sp4_ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of SLES 11 SP4 SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle11sp4_ssh_minion" hostname

  Scenario: Check events history for failures on SLES 11 SP4 SSH minion
    Given I am on the Systems overview page of this "sle11sp4_ssh_minion"
    Then I check for failed events on history event page
