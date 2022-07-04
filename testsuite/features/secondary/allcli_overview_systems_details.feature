# Copyright (c) 2017-2022 SUSE LLC.
# Licensed under the terms of the MIT license.

@scope_visualization
Feature: The system details of each minion and client provides an overview of the system

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

@sle_client
  Scenario: Traditional client hardware refresh
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Hardware"
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I force picking pending events on "sle_client" if necessary
    And I wait until event "Hardware List Refresh scheduled by admin" is completed

@sle_client
  Scenario: Traditional client grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "sle_client"
    Then the hostname for "sle_client" should be correct
    And the kernel for "sle_client" should be correct
    And the OS version for "sle_client" should be correct
    And the IPv4 address for "sle_client" should be correct
    And the IPv6 address for "sle_client" should be correct
    And the system ID for "sle_client" should be correct
    And the system name for "sle_client" should be correct
    And the uptime for "sle_client" should be correct
    And I should see several text fields for "sle_client"

@sle_minion
  Scenario: SLE minion hardware refresh
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Hardware"
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh scheduled by admin" is completed

@sle_minion
  Scenario: Minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "sle_minion"
    Then the hostname for "sle_minion" should be correct
    And the kernel for "sle_minion" should be correct
    And the OS version for "sle_minion" should be correct
    And the IPv4 address for "sle_minion" should be correct
    And the IPv6 address for "sle_minion" should be correct
    And the system ID for "sle_minion" should be correct
    And the system name for "sle_minion" should be correct
    And the uptime for "sle_minion" should be correct
    And I should see several text fields for "sle_minion"

@rh_minion
  Scenario: RedHat-like minion hardware refresh
    Given I am on the Systems overview page of this "rh_minion"
    When I follow "Hardware"
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh scheduled by admin" is completed

@rh_minion
  Scenario: RedHat-like minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "rh_minion"
    Then the hostname for "rh_minion" should be correct
    And the kernel for "rh_minion" should be correct
    And the OS version for "rh_minion" should be correct
    And the IPv4 address for "rh_minion" should be correct
    And the IPv6 address for "rh_minion" should be correct
    And the system ID for "rh_minion" should be correct
    And the system name for "rh_minion" should be correct
    And the uptime for "rh_minion" should be correct
    And I should see several text fields for "rh_minion"

@deb_minion
  Scenario: Debian-like minion hardware refresh
    Given I am on the Systems overview page of this "deb_minion"
    When I follow "Hardware"
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh scheduled by admin" is completed

@deb_minion
  Scenario: Debian-like minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "deb_minion"
    Then the hostname for "deb_minion" should be correct
    And the kernel for "deb_minion" should be correct
    And the OS version for "deb_minion" should be correct
    And the IPv4 address for "deb_minion" should be correct
    And the IPv6 address for "deb_minion" should be correct
    And the system ID for "deb_minion" should be correct
    And the system name for "deb_minion" should be correct
    And the uptime for "deb_minion" should be correct
    And I should see several text fields for "deb_minion"

@ssh_minion
  Scenario: SSH-managed minion hardware refresh
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Hardware"
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh scheduled by admin" is completed

@ssh_minion
  Scenario: SSH-managed minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "ssh_minion"
    Then the hostname for "ssh_minion" should be correct
    And the kernel for "ssh_minion" should be correct
    And the OS version for "ssh_minion" should be correct
    And the IPv4 address for "ssh_minion" should be correct
    And the IPv6 address for "ssh_minion" should be correct
    And the system ID for "ssh_minion" should be correct
    And the system name for "ssh_minion" should be correct
    And the uptime for "ssh_minion" should be correct
    And I should see several text fields for "ssh_minion"
