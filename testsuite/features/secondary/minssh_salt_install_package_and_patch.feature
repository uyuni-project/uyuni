# Copyright (c) 2015-2025 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_salt
Feature: Install a package and a patch on the SUSE SSH client via Salt through the UI

  Scenario: Pre-requisite: enable test_repo_rpm_pool repository
    When I enable repository "test_repo_rpm_pool" on this "ssh_minion"

  Scenario: Pre-requisite: install virgo-dummy-1.0 package on SSH minion
    When I refresh the metadata for "ssh_minion"
    And I install old package "virgo-dummy-1.0" on this "ssh_minion" without error control

  Scenario: Pre-requisite: remove andromeda-dummy package from SSH minion
    When I remove package "andromeda-dummy" from this "ssh_minion" without error control
    And I refresh the metadata for "ssh_minion"
    And I refresh packages list via spacecmd on "ssh_minion"
    And I wait until refresh package list on "ssh_minion" is finished

  Scenario: Pre-requisite: refresh package list and check old packages installed on SSH minion
    When I refresh packages list via spacecmd on "ssh_minion"
    And I wait until refresh package list on "ssh_minion" is finished
    Then spacecmd should show packages "virgo-dummy-1.0" installed on "ssh_minion"
    And I wait until package "andromeda-dummy-2.0-1.1" is removed from "ssh_minion" via spacecmd

  Scenario: Log in as org admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: ensure the errata cache is computed before patching Salt minion
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "virgo-dummy" as the filtered package name
    And I click on the filter button until page does contain "virgo-dummy" text
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    When I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Install a patch on the SSH minion
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    And I enter "virgo" as the filtered synopsis
    And I click on the filter button
    And I check "virgo-dummy-3456" in the list
    And I check "allowVendorChange"
    And I click on "Apply Patches"
    And I click on "Confirm"
    Then I should see a "1 patch update has been scheduled for" text
    When I wait for "virgo-dummy-2.0-1.1" to be installed on "ssh_minion"
    Then vendor change should be enabled for package actions on "ssh_minion"

  Scenario: Install a package on the SSH minion
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Software" in the content area
    And I follow "Packages"
    And I follow "Install"
    And I enter "andromeda" as the filtered package name
    And I click on the filter button
    And I check "andromeda-dummy-2.0-1.1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    When I wait for "andromeda-dummy-2.0-1.1" to be installed on "ssh_minion"
    Then vendor change should be enabled for package actions on "ssh_minion"

  Scenario: Cleanup: disable test_repo_rpm_pool
    When I disable repository "test_repo_rpm_pool" on this "ssh_minion"

  Scenario: Cleanup: remove virgo-dummy package from SSH minion
    When I remove package "virgo-dummy" from this "ssh_minion" without error control
    And I refresh the metadata for "ssh_minion"
    And I refresh packages list via spacecmd on "ssh_minion"
    And I wait until refresh package list on "ssh_minion" is finished

  Scenario: Cleanup: remove andromeda-dummy package from SSH minion
    When I remove package "andromeda-dummy" from this "ssh_minion" without error control
    And I refresh the metadata for "ssh_minion"
    And I refresh packages list via spacecmd on "ssh_minion"
    And I wait until refresh package list on "ssh_minion" is finished
