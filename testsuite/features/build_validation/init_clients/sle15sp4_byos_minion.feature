# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

@sle15sp4_byos_minion
Feature: Bootstrap a SLES 15 SP4 byos Salt minion

  Scenario: Clean up sumaform leftovers on a SLES 15 SP4 Salt minion
    When I perform a full salt minion cleanup on "sle15sp4_byos_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a SLES 15 SP4 byos minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle15sp4_byos_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-sle15sp4_byos_minion_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    Then I wait until "sle15sp4_byos_minion" is rejected on server
    When I follow the left menu "Systems > Overview"
    And I should not see "sle15sp4_byos_minion" hostname

  Scenario: Check the new bootstrapped SLES 15 SP4 byos minion in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    And the Salt master can reach "sle15sp4_byos_minion"
