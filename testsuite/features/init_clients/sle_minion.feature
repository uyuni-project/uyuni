# Copyright (c) 2016-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@sle_minion
Feature: Bootstrap a Salt minion via the GUI

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a SLES minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text

  Scenario: Check the new bootstrapped minion in System Overview page
    When I follow the left menu "Salt > Keys"
    And I wait until I do not see "Loading..." text
    Then I should see a "accepted" text
    When I follow the left menu "Systems > Overview"
    And I wait until I see the name of "sle_minion", refreshing the page
    And I wait until onboarding is completed for "sle_minion"
    Then the Salt master can reach "sle_minion"

  Scenario: Use correct kernel image on the SLES minion
    When I remove package "kernel-default-base" from this "sle_minion"
    And I install package "kernel-default" on this "sle_minion"

  Scenario: Reboot the SLES minion to use the new kernel
    When I reboot the "sle_minion" host through SSH, waiting until it comes back

@proxy
  Scenario: Check connection from minion to proxy
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle_minion" hostname
