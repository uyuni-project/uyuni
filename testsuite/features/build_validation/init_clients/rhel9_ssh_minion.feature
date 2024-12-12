# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Rhel 9 minion
#  2) subscribe it to a base channel for testing

@rhel9_ssh_minion
Feature: Bootstrap a Rhel 9 SSH minion

  Scenario: Clean up sumaform leftovers on a Rhel 9 Salt minion
    When I perform a full salt minion cleanup on "rhel9_ssh_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a Rhel 9 SSH minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "rhel9_ssh_minion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-rhel9_ssh_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    # workaround for bsc#1222108
    And I wait at most 480 seconds until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "rhel9_ssh_minion"

@proxy
  Scenario: Check connection from Rhel 9 SSH minion to proxy
    Given I am on the Systems overview page of this "rhel9_ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of Rhel 9 SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "rhel9_ssh_minion" hostname

  Scenario: Check events history for failures on Rhel 9 SSH minion
    Given I am on the Systems overview page of this "rhel9_ssh_minion"
    Then I check for failed events on history event page
