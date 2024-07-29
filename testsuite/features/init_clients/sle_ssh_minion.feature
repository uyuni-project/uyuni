# Copyright (c) 2016-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@ssh_minion
Feature: Bootstrap a Salt host managed via salt-ssh

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Register this SSH minion for service pack migration
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "ssh_minion" as "hostname"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I follow the left menu "Systems > Overview"
    And I wait until I see the name of "ssh_minion", refreshing the page
    And I wait until onboarding is completed for "ssh_minion"

  Scenario: Use correct kernel image on the SSH minion
    When I remove package "kernel-default-base" from this "ssh_minion"
    And I install package "kernel-default" on this "ssh_minion"

  Scenario: Reboot the SSH minion to use the new kernel
    When I reboot the "ssh_minion" host through SSH, waiting until it comes back

@proxy
  Scenario: Check connection from SSH minion to proxy
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ssh_minion" hostname
