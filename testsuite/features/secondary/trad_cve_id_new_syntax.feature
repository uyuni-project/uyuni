# Copyright (c) 2015-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_cve_audit
@scope_traditional_client
Feature: Support for new CVE-ID syntax

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Check perseus-dummy-7891 patches
    When I follow the left menu "Patches > Patch List > All"
    And I enter "dummy" as the filtered synopsis
    And I click on the filter button
    And I follow "perseus-dummy-7891"
    Then I should see a "perseus-dummy-7891 - Security Advisory" text
    And I should see a "CVE-1999-12345" link
    And I should see a "CVE-1999-99781" link

  Scenario: Check local metadata for long CVE IDs
    When I refresh the metadata for "sle_client"
    Then I should have 'reference.*id="CVE-1999-12345' in the patch metadata
    And I should have 'reference.*id="CVE-1999-99781' in the patch metadata
    And I should have 'reference.*http://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-1999-12345' in the patch metadata
    And I should have 'reference.*http://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-1999-99781' in the patch metadata

  Scenario: Search for CVE ID with the new format
    When I follow the left menu "Patches > Advanced Search"
    When I enter "CVE-1999-12345" as "search_string" in the content area
    And I click on "Search"
    Then I should see a "Advanced Search" text
    And I should see a "perseus-dummy-7891" link in the content area
