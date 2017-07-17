# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check if new CVE-ID syntax is working with SUSE Manager
  In Order to check if the new CVE-ID syntax is working with SUSE Manager
  As the testing user
  I want to see the patches in the web page with a long CVE ID

  Scenario: check perseus-dummy-7891 patches
    Given I am on the patches page
    When I follow "All" in the left menu
    And I follow "perseus-dummy-7891"
    Then I should see a "perseus-dummy-7891 - Security Advisory" text
    And I should see a "CVE-1999-12345" link
    And I should see a "CVE-1999-99781" link

  Scenario: Check local metdata for long CVE IDs
    When I refresh the metadata for "sle-client"
    Then I should have 'reference.*id="CVE-1999-12345' in the patch metadata
    And I should have 'reference.*id="CVE-1999-99781' in the patch metadata
    And I should have 'reference.*http://www.cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-1999-12345' in the patch metadata
    And I should have 'reference.*http://www.cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-1999-99781' in the patch metadata

  Scenario: Search for CVE ID with the new format
    Given I am on the patches page
    And I follow "Advanced Search"
    When I enter "CVE-1999-12345" as "search_string" in the content area
    And I click on "Search"
    Then I should see a "Advanced Search" text
    And I should see a "perseus-dummy-7891" link in the content area
