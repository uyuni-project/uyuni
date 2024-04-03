# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

@opensuse154arm_ssh_minion
Feature: Bootstrap a openSUSE 15.4 ARM Salt SSH minion

  Scenario: Clean up sumaform leftovers on a openSUSE 15.4 ARM Salt SSH minion
    When I perform a full salt minion cleanup on "opensuse154arm_ssh_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a openSUSE 15.4 ARM system managed via salt-ssh
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "opensuse154arm_ssh_minion" as "hostname"
    And I enter "linux" as "password"
    And I select "1-opensuse154arm_ssh_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    # workaround for bsc#1222108
    And I wait at most 480 seconds until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "opensuse154arm_ssh_minion"

@proxy
  Scenario: Check connection from openSUSE 15.4 ARM SSH minion to proxy
    Given I am on the Systems overview page of this "opensuse154arm_ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of openSUSE 15.4 ARM SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "opensuse154arm_ssh_minion" hostname

  Scenario: Check events history for failures on openSUSE 15.4 ARM SSH minion
    Given I am on the Systems overview page of this "opensuse154arm_ssh_minion"
    Then I check for failed events on history event page
