# Copyright (c) 2015-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@sle_minion
@scope_cve_audit
Feature: CVE Audit on SLE Salt Minions
  In order to check if systems are patched against certain vulnerabilities
  As an authorized user
  I want to see the Salt Minions that need to be patched

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: downgrade milkyway-dummy to lower version
    When I enable repository "test_repo_rpm_pool" on this "sle_minion"
    And I install old package "milkyway-dummy-1.0" on this "sle_minion"
    And I refresh the metadata for "sle_minion"
    And I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Schedule channel data refresh
    When I follow the left menu "Admin > Task Schedules"
    And I follow "cve-server-channels-default"
    And I follow "cve-server-channels-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Display CVE audit page
    When I follow the left menu "Audit > CVE Audit"
    Then I should see a "CVE Audit" link in the left menu
    And I should see a "CVE Audit" text

  Scenario: Search for a known CVE number
    When I follow the left menu "Audit > CVE Audit"
    And I select "1999" from "cveIdentifierYear"
    And I enter "9999" as "cveIdentifierId"
    And I click on "Audit Servers"
    Then I should see "sle_minion" as link
    And I should see a "Affected, at least one patch available in an assigned channel" text
    And I should see a "Install a new patch on this system" link
    And I should see a "milkyway-dummy-2345" text
    And I should see a "Download CSV" link
    And I should see a "Status" button
    And I should see a "Name" button
    And I should see a "extra CVE data update" link
    Then I follow "Install a new patch on this system" on "sle_minion" row
    And I should see a "Relevant Patches" text

  Scenario: Search for an unknown CVE number
    When I follow the left menu "Audit > CVE Audit"
    And I select "2012" from "cveIdentifierYear"
    And I enter "2806" as "cveIdentifierId"
    And I click on "Audit Servers"
    Then I should see a "The specified CVE number was not found" text

  Scenario: Select a system for the System Set Manager
    And I click on the clear SSM button
    And I follow the left menu "Audit > CVE Audit"
    And I select "1999" from "cveIdentifierYear"
    And I enter "9999" as "cveIdentifierId"
    And I click on "Audit Servers"
    Then I should see a "Affected, at least one patch available in an assigned channel" text
    When I check the "sle_minion" client
    Then I should see a "system selected" text
    When I follow the left menu "Systems > System List > All"
    Then I should see "sle_minion" as link
    And I click on the clear SSM button

  Scenario: List systems by patch status via API before patch
    When I follow the left menu "Admin > Task Schedules"
    And I follow "cve-server-channels-default"
    And I follow "cve-server-channels-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows
    When I call audit.list_systems_by_patch_status() with CVE identifier "CVE-1999-9979"
    Then I should get status "NOT_AFFECTED" for "sle_minion"
    When I call audit.list_systems_by_patch_status() with CVE identifier "CVE-1999-9999"
    Then I should get status "AFFECTED_FULL_PATCH_APPLICABLE" for "sle_minion"
    And I should get the "fake-rpm-suse-channel" channel label
    And I should get the "milkyway-dummy-2345" patch

  Scenario: Apply patches
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    And I wait until I see "milkyway-dummy-2345" text, refreshing the page
    And I check "milkyway-dummy-2345" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    Then I should see a "patch update has been scheduled" text
    And I wait until event "Patch Update: milkyway-dummy-2345" is completed

  Scenario: List systems by patch status via API after patch
    And I call audit.list_systems_by_patch_status() with CVE identifier "CVE-1999-9999"
    Then I should get status "PATCHED" for "sle_minion"

  Scenario: Cleanup: remove installed packages
    When I disable repository "test_repo_rpm_pool" on this "sle_minion" without error control
    And I remove package "milkyway-dummy" from this "sle_minion" without error control

  Scenario: Cleanup: remove remaining systems from SSM after CVE audit tests
    When I click on the clear SSM button
