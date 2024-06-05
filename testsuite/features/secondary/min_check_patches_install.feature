# Copyright (c) 2015-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_onboarding
Feature: Display patches

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-require: enable old packages to fake a possible installation
    When I enable repository "test_repo_rpm_pool" on this "sle_minion"
    And I refresh the metadata for "sle_minion"
    And I install old package "andromeda-dummy-1.0" on this "sle_minion"
    And I install old package "virgo-dummy-1.0" on this "sle_minion"
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Check all patches exist
    When I follow the left menu "Patches > Patch List > Relevant"
    Then I should see an update in the list
    When I wait until I see "andromeda-dummy" text, refreshing the page
    Then I should see a "andromeda-dummy-6789" link
    When I enter "virgo-dummy" as the filtered synopsis
    And I click on the filter button
    And I wait until I see "virgo-dummy" text
    Then I should see a "virgo-dummy-3456" link

  Scenario: Check SLES release 6789 patches
    When I follow the left menu "Patches > Patch List > Relevant"
    And I follow "andromeda-dummy-6789"
    Then I should see a "andromeda-dummy-6789 - Bug Fix Advisory" text
    And I should see a "Test update for andromeda-dummy" text
    And I should see a "Fake-RPM-SUSE-Channel" link
    And I should see a "reboot_suggested" text

  Scenario: Check packages of SLES release 6789 patches
    When I follow the left menu "Patches > Patch List > Relevant"
    And I follow "andromeda-dummy-6789"
    And I follow "Packages"
    Then I should see a "Fake-RPM-SUSE-Channel" link
    And I should see a "sha512:6bc584eb0af1bc04705c78e59ca0e4154ea86c46cd75abee57c82bfc4ebb57f3660ed21e9aceceae03855264e163853acbcde88005609d773c20f185587f70cc" text
    And I should see a "andromeda-dummy-2.0-1.1-noarch" link

  Scenario: Check relevant patches for this client
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    Then I should see a "Relevant Patches" text
    And I should see a "Test update for andromeda-dummy" text
    And I should see a "Test update for virgo-dummy" text

  Scenario: Cleanup: regenerate search index for later tests
    When I clean the search index on the server

  Scenario: Cleanup: remove old packages
    When I disable repository "test_repo_rpm_pool" on this "sle_minion" without error control
    And I refresh the metadata for "sle_minion"
    And I remove package "andromeda-dummy" from this "sle_minion" without error control
    And I remove package "virgo-dummy" from this "sle_minion" without error control
