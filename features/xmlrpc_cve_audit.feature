# Copyright (c) 2010-2011 SUSE Linux Products GmbH.
# Licensed under the terms of the MIT license.

Feature: Test the XML-RPC CVE Audit feature.

  Background:
    Given I am on the Admin page
    When I follow "Task Schedules"
      And I follow "cve-server-channels-default"
      And I follow "cve-server-channels-bunch"
      And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
      And I wait for "5" seconds

  @xmlrpc
  Scenario: before applying patches 
    Given I am logged in via XML-RPC/cve audit as user "admin" and password "admin"

    When I call audit.listSystemsByPatchStatus with CVE identifier "CVE-2012-3495"
    Then I should get status "NOT_AFFECTED" for system "1000010000"
     
    When I call audit.listSystemsByPatchStatus with CVE identifier "CVE-2012-3400"
    Then I should get status "AFFECTED_PATCH_APPLICABLE" for system "1000010000"
      And I should get channel "103"
      And I should get patch "2"

    Then I logout from XML-RPC/cve audit namespace.

  @xmlrpc
  Scenario: after applying patches 
    When I follow "Systems"
      And I follow this client link
      And I follow "Software"
      And I follow "Patches" in class "content-nav"
      And I click on "Select All"
      And I click on "Apply Patches"
      And I click on "Confirm"
      And I wait for "5" seconds
      And I run rhn_check on this client
    Then I should see a "patch updates have been scheduled" text

    Given I am logged in via XML-RPC/cve audit as user "admin" and password "admin"
    
    When I call audit.listSystemsByPatchStatus with CVE identifier "CVE-2012-3400"
    
    Then I should get status "PATCHED" for system "1000010000"
      And I should get channel "103"
      And I should get patch "2"

    Then I logout from XML-RPC/cve audit namespace.
