# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test the XML-RPC CVE Audit feature.

  Scenario: before applying patches 
    Given I am on the Admin page
    When I follow "Task Schedules"
    And I follow "cve-server-channels-default"
    And I follow "cve-server-channels-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait for "10" seconds
    And I am logged in via XML-RPC/cve audit as user "admin" and password "admin"
    When I call audit.listSystemsByPatchStatus with CVE identifier "CVE-1999-9979"
    Then I should get status "NOT_AFFECTED" for this client
    When I call audit.listSystemsByPatchStatus with CVE identifier "CVE-1999-9999"
    Then I should get status "AFFECTED_PATCH_APPLICABLE" for this client
    And I should get the test-channel
    And I should get the "milkyway-dummy-2345" patch
    Then I logout from XML-RPC/cve audit namespace.

  Scenario: after applying patches 
    Given I am on the Systems overview page of this "sle-client"
    And I follow "Software"
    And I follow "Patches" in the content area
    And I check "milkyway-dummy-2345" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    And I wait for "5" seconds
    And I run rhn_check on this client
    Then I should see a "patch update has been scheduled" text
    Given I am logged in via XML-RPC/cve audit as user "admin" and password "admin"
    When I call audit.listSystemsByPatchStatus with CVE identifier "CVE-1999-9999"
    Then I should get status "PATCHED" for this client
    And I should get the test-channel
    And I should get the "milkyway-dummy-2345" patch
    Then I logout from XML-RPC/cve audit namespace.
