# Copyright (c) 2020-2021 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Ubuntu minion via salt-ssh
#  2) subscribe it to a base channel for testing

@ubuntu2204_ssh_minion
Feature: Bootstrap a Ubuntu 22.04 Salt SSH minion

  Scenario: Clean up sumaform leftovers on a Ubuntu 22.04 Salt SSH minion
    When I perform a full salt minion cleanup on "ubuntu2204_ssh_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a SSH-managed Ubuntu 22.04 minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "ubuntu2204_ssh_minion" as "hostname"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I enter "22" as "port"
    And I select "1-ubuntu2204_ssh_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I check "manageWithSSH"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "ubuntu2204_ssh_minion"

  Scenario: Check events history for failures on SSH-managed Ubuntu 22.04 minion
    Given I am on the Systems overview page of this "ubuntu2204_ssh_minion"
    Then I check for failed events on history event page
