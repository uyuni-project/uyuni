# Copyright (c) 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_traditional_client
@scope_onboarding
Feature: Install a package to the traditional client

  Scenario: Pre-requisite: remove packages before traditional client package test
    When I remove package "andromeda-dummy" from this "sle_client" without error control
    And I remove package "virgo-dummy" from this "sle_client" without error control

  Scenario: Install a package to the traditional client
    Given I am on the Systems overview page of this "sle_client"
    And metadata generation finished for "test-channel-x86_64"
    When I follow "Software" in the content area
    And I follow "Install"
    And I check "virgo-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    And I run "rhn_check -vvv" on "sle_client"
    Then I should see a "1 package install has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed
    Then "virgo-dummy-2.0-1.1" should be installed on "sle_client"

  Scenario: Enable old packages for testing a patch install
    When I enable repository "test_repo_rpm_pool" on this "sle_client"
    And I install old package "andromeda-dummy-1.0" on this "sle_client"
    And I run "rhn_check -vvv" on "sle_client"

  Scenario: Schedule errata refresh after reverting to old package
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Install a patch to the traditional client
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    And I check "andromeda-dummy-6789" in the list
    And I check "allowVendorChange"
    And I click on "Apply Patches"
    And I click on "Confirm"
    And I run "rhn_check -vvv" on "sle_client"
    Then I should see a "1 patch update has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed
    Then "andromeda-dummy-2.0-1.1" should be installed on "sle_client"
    And vendor change should be enabled for package actions on "sle_client"
    And The metadata buildtime from package "andromeda-dummy" match the one in the rpm on "sle_client"

  Scenario: Cleanup: remove packages and restore non-update repo
    When I remove package "andromeda-dummy" from this "sle_client"
    And I remove package "virgo-dummy" from this "sle_client"
    And I disable repository "test_repo_rpm_pool" on this "sle_client"
    And I run "rhn_check -vvv" on "sle_client"
