# Copyright (c) 2026 SUSE LLC.
# SPDX-License-Identifier: MIT
#
# The live OVAL feeds are too slow to sync (over 15 minutes for SLES 15) and their data drifts, so this feature
# points the package-owned OVAL sources of the server to a small fixture: milkyway-dummy older than 2.0-1.1 is affected
# by the fake CVE-1999-9990, which no channel patch fixes. @oval_fixture restores the sources when a scenario fails.
# OVAL data is global server state: keep this feature in sequential run sets, after min_cve_audit.feature
# (see the comment in run_sets/secondary.yml).
# Uyuni CI's sle_minion runs an OS without OVAL support, and the fixture only covers SLES, hence @susemanager.

@skip_if_github_validation
@scope_cve_audit
@sle_minion
@susemanager
@oval_fixture
Feature: CVE audit with OVAL data
  In order to get accurate CVE audit results
  As an authorized user
  I want the OVAL data of my clients' OS to be synchronized and used by the CVE audit

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: vulnerable package and channel CVE data
    When I enable repository "test_repo_rpm_pool" on this "sle_minion"
    And I install old package "milkyway-dummy-1.0" on this "sle_minion"
    And I refresh the metadata for "sle_minion"
    And I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows
    When I follow the left menu "Admin > Task Schedules"
    And I follow "cve-server-channels-default"
    And I follow "cve-server-channels-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  # Every other OS keeps its OVAL entry without URL, so the sync downloads only the fixture and takes seconds
  Scenario: Synchronize OVAL data from a fixture
    When I point the OVAL sources of the server to a fixture declaring "CVE-1999-9990" for the OS of "sle_minion"
    And I follow the left menu "Admin > Task Schedules"
    And I follow "oval-data-sync-default"
    And I follow "oval-data-sync-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  # The bunch finishes even when a product fails to sync, so check the stored data
  Scenario: OVAL data is stored for the OS of the SLE minion
    Then OVAL vulnerability data should exist for the OS of "sle_minion"

  Scenario: CVE audit reports an OVAL vulnerability without patch in the channels
    When I call audit.list_systems_by_patch_status() with CVE identifier "CVE-1999-9990"
    Then I should get status "AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI" for "sle_minion"

  Scenario: CVE audit uses the OVAL data of the SLE minion
    When I follow the left menu "Audit > CVE Audit"
    And I select "1999" from "cveIdentifierYear"
    And I enter "9990" as "cveIdentifierId"
    And I click on "Audit Servers"
    Then I should see "sle_minion" as link
    And the CVE audit row for "sle_minion" should not show the "OVAL data out of sync. Potential missed vulnerabilities" warning

# Rocky is not supported by OVAL; Alma, Oracle, Liberty and RHEL are, so no other Red Hat-like OS shows this warning
@rhlike_minion
@rocky8_minion
  Scenario: CVE audit shows that OVAL does not support the OS of the Rocky Linux minion
    When I follow the left menu "Audit > CVE Audit"
    And I select "1999" from "cveIdentifierYear"
    And I enter "9990" as "cveIdentifierId"
    And I click on "Audit Servers"
    Then the CVE audit row for "rhlike_minion" should show the "OVAL audit not supported for this system's OS" warning

  Scenario: Upgrade the vulnerable package with a patch
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    And I enter "milkyway" as the filtered synopsis
    And I click on the filter button
    And I check "milkyway-dummy-2345" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    Then I should see a "patch update has been scheduled" text
    And I wait until event "Patch Update: milkyway-dummy-2345" is completed

  Scenario: CVE audit reports the OVAL vulnerability as patched after the upgrade
    When I call audit.list_systems_by_patch_status() with CVE identifier "CVE-1999-9990"
    Then I should get status "PATCHED" for "sle_minion"

  # A sync that refreshes nothing deletes the OVAL vulnerability data, which a sync of the live feeds would replace
  Scenario: Cleanup: remove the OVAL data of the fixture
    When I clear the OVAL sources of the server
    And I follow the left menu "Admin > Task Schedules"
    And I follow "oval-data-sync-default"
    And I follow "oval-data-sync-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows
    And OVAL vulnerability data should not exist for the OS of "sle_minion"
    When I delete the OVAL platforms of the server that have no vulnerability data

  Scenario: Cleanup: restore the OVAL sources of the server
    When I restore the OVAL sources of the server

  Scenario: Cleanup: remove the vulnerable package
    When I disable repository "test_repo_rpm_pool" on this "sle_minion" without error control
    And I remove package "milkyway-dummy" from this "sle_minion" without error control
