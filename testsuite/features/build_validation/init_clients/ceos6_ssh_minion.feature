# Copyright (c) 2020-2021 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new CentOS minion via salt-ssh
#  2) subscribe it to a base channel for testing

@ceos6_ssh_minion
Feature: Bootstrap a CentOS 6 Salt SSH minion

  Scenario: Clean up sumaform leftovers on a CentOS 6 Salt SSH minion
    When I perform a full salt minion cleanup on "ceos6_ssh_minion"

  Scenario: Bootstrap a CentOS 6 Salt SSH minion
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "ceos6_ssh_minion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-ceos6_ssh_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "ceos6_ssh_minion"

  # WORKAROUND bsc#1181847
  Scenario: Import the GPG keys for CentOS 6 Salt SSH minion
    When I import the GPG keys for "ceos6_ssh_minion"

@proxy
  Scenario: Check connection from CentOS 6 Salt SSH minion to proxy
    Given I am on the Systems overview page of this "ceos6_ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of CentOS 6 Salt SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos6_ssh_minion" hostname

  Scenario: Check events history for failures on CentOS 6 Salt SSH minion
    Given I am on the Systems overview page of this "ceos6_ssh_minion"
    Then I check for failed events on history event page
