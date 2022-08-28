# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Debian 11 minion via salt-ssh
#  2) subscribe it to a base channel for testing

@debian11_ssh_minion
Feature: Bootstrap a Debian 11 Salt SSH minion

  Scenario: Clean up sumaform leftovers on a Debian 11 Salt SSH minion
    When I perform a full salt minion cleanup on "debian11_ssh_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a SSH-managed Debian 11 minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "debian11_ssh_minion" as "hostname"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I enter "22" as "port"
    And I select "1-debian11_ssh_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I check "manageWithSSH"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "debian11_ssh_minion"

  Scenario: Check events history for failures on SSH-managed Debian 11 minion
    Given I am on the Systems overview page of this "debian11_ssh_minion"
    Then I check for failed events on history event page
