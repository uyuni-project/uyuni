# Copyright (c) 2015-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Patches display

  Scenario: Pre-require: enable old packages to fake a possible installation
    Given I am authorized as "admin" with password "admin"
    When I enable repository "test_repo_rpm_pool" on this "sle-client"
    And I run "zypper -n ref" on "sle-client"
    And I run "zypper -n in --oldpackage andromeda-dummy-1.0" on "sle-client"
    And I run "zypper -n in --oldpackage virgo-dummy-1.0" on "sle-client"
    And I run "rhn_check -vvv" on "sle-client"
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

  Scenario: check SLES release 6789 patches
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
    And I should see a "sha256:11d493dcec20274eb2f312d0b552eaaf3155ee81dd13fe7945cbd7aa44f70761" text
    And I should see a "andromeda-dummy-2.0-1.1-noarch" link

  Scenario: Check relevant patches for this client
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    Then I should see a "Relevant Patches" text
    And I should see a "Test update for virgo-dummy" text

  Scenario: Cleanup: regenerate search index for later tests
    When I clean the search index on the server

  Scenario: Cleanup: remove old packages
    When I disable repository "test_repo_rpm_pool" on this "sle-client" without error control
    And I run "zypper -n ref" on "sle-client" without error control
    And I run "zypper -n rm --oldpackage andromeda-dummy-1.0" on "sle-client" without error control
    And I run "zypper -n rm --oldpackage virgo-dummy-1.0" on "sle-client" without error control
    And I run "rhn_check -vvv" on "sle-client" without error control
