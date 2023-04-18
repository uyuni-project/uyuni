# Copyright (c) 2017-2022 SUSE LLC.
# Licensed under the terms of the MIT license.

@scope_visualization
Feature: The system details of each minion and client provides an overview of the system

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section
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

@rhlike_minion
  Scenario: Red Hat-like minion hardware refresh
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Hardware"
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh scheduled by admin" is completed

@rhlike_minion
  Scenario: Red Hat-like minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "rhlike_minion"
    Then the hostname for "rhlike_minion" should be correct
    And the kernel for "rhlike_minion" should be correct
    And the OS version for "rhlike_minion" should be correct
    And the IPv4 address for "rhlike_minion" should be correct
    And the IPv6 address for "rhlike_minion" should be correct
    And the system ID for "rhlike_minion" should be correct
    And the system name for "rhlike_minion" should be correct
    And the uptime for "rhlike_minion" should be correct
    And I should see several text fields for "rhlike_minion"

@deblike_minion
  Scenario: Debian-like minion hardware refresh
    Given I am on the Systems overview page of this "deblike_minion"
    When I follow "Hardware"
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh scheduled by admin" is completed

@deblike_minion
  Scenario: Debian-like minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "deblike_minion"
    Then the hostname for "deblike_minion" should be correct
    And the kernel for "deblike_minion" should be correct
    And the OS version for "deblike_minion" should be correct
    And the IPv4 address for "deblike_minion" should be correct
    And the IPv6 address for "deblike_minion" should be correct
    And the system ID for "deblike_minion" should be correct
    And the system name for "deblike_minion" should be correct
    And the uptime for "deblike_minion" should be correct
    And I should see several text fields for "deblike_minion"

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
