# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: CVE Audit
  In Order to check if systems are patched against certain vulnerabilities
  As an authorized user
  I want to see systems that need to be patched

  Background:
    Given I am authorized as "admin" with password "admin"

  Scenario: schedule channel data refresh
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "cve-server-channels-default"
    And I follow "cve-server-channels-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: feature should be accessible
    When I follow "Audit"
    Then I should see a "CVE Audit" link in the left menu
    And I should see a "CVE Audit" text

  Scenario: searching for a known CVE number
    When I follow "Audit"
    And I select "1999" from "cveIdentifierYear"
    And I enter "9999" as "cveIdentifierId"
    And I click on "Audit systems"
    Then I should see this client as link
    And I should see a "Affected, at least one patch available in an assigned channel" text
    And I should see a "Install a new patch in this system" link
    And I should see a "Only candidate is: milkyway-dummy-2345" text
    And I should see a "Download CSV" link
    And I should see an alphabar link to this system
    And I should see a "Patch status" link
    And I should see a "System" link
    And I should see a "extra CVE data update" link
    Then I follow "Install a new patch in this system"
    And I should see a "Relevant Patches" text

  Scenario: searching for an unknown CVE number
    When I follow "Audit"
    And I select "2012" from "cveIdentifierYear"
    And I enter "2806" as "cveIdentifierId"
    And I click on "Audit systems"
    Then I should see a "The specified CVE number was not found" text

  Scenario: selecting a system for the System Set Manager
    When I follow "Audit"
    And I select "1999" from "cveIdentifierYear"
    And I enter "9999" as "cveIdentifierId"
    And I click on "Audit systems"
    And I should see a "Affected, at least one patch available in an assigned channel" text
    When I check "Affected, at least one patch available in an assigned channel" in the list
    Then I should see a "system selected" text
    When I follow "Manage"
    And I follow "Systems" in the content area
    Then I should see this client as link
    And I follow "Clear"
