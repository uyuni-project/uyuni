# Copyright (c) 2015-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_traditional_client
@scope_onboarding
Feature: Display patches

  Scenario: Pre-require: enable old packages to fake a possible installation
    Given I am authorized as "admin" with password "admin"
    When I enable repository "test_repo_rpm_pool" on this "sle_client"
    And I run "zypper -n ref" on "sle_client"
    And I install old package "andromeda-dummy-1.0" on this "sle_client"
    And I install old package "virgo-dummy-1.0" on this "sle_client"
    And I run "rhn_check -vvv" on "sle_client"
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Check all patches exist
    Given I am on the patches page
    When I follow the left menu "Patches > Patch List > Relevant"
    Then I should see an update in the list
    And I should see a "virgo-dummy-3456" link

  Scenario: Check SLES release 6789 patches
    Given I am on the patches page
    And I follow "andromeda-dummy-6789"
    Then I should see a "andromeda-dummy-6789 - Bug Fix Advisory" text
    And I should see a "Test update for andromeda-dummy" text
    And I should see a "Test-Channel-x86_64" link
    And I should see a "Test-Channel-i586" link
    And I should see a "reboot_suggested" text

  Scenario: Check packages of SLES release 6789 patches
    Given I am on the patches page
    And I follow "andromeda-dummy-6789"
    And I follow "Packages"
    Then I should see a "Test-Channel-x86_64" link
    And I should see a "Test-Channel-i586" link
    And I should see a "sha256:ba3f6d939fce43b60f4d20a09887e211f11024b61defb246dd62705bf4f4ced0" text
    And I should see a "andromeda-dummy-2.0-1.1-noarch" link

  Scenario: Check relevant patches for this client
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    Then I should see a "Relevant Patches" text
    And I should see a "Test update for virgo-dummy" text

  Scenario: Cleanup: regenerate search index for later tests
    When I clean the search index on the server

  Scenario: Cleanup: remove old packages
    When I disable repository "test_repo_rpm_pool" on this "sle_client" without error control
    And I run "zypper -n ref" on "sle_client" without error control
    And I remove package "andromeda-dummy" from this "sle_client" without error control
    And I remove package "virgo-dummy" from this "sle_client" without error control
    And I run "rhn_check -vvv" on "sle_client" without error control
