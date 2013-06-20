# Copyright (c) 2010-2011 SUSE Linux Products GmbH.
# Licensed under the terms of the MIT license.

Feature: Test the XML-RPC CVE Audit feature.

  @xmlrpc
  Background:
    Given I am on the Admin page
    When I follow "Task Schedules"
      And I follow "cve-server-channels-default"
      And I follow "cve-server-channels-bunch"
      And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
      And I wait for "5" seconds

   @xmlrpc
   Scenario: 
     Given I am logged in via XML-RPC/cve audit as user "admin" and password "admin"
       And channel data has already been updated

     When I call audit.listSystemsByPatchStatus with CVE identifier "CVE-2012-3495"
     Then I should get status "NOT_AFFECTED" for system "1000010000"
     
     When I call audit.listSystemsByPatchStatus with CVE identifier "CVE-2012-3400"
     Then I should get status "AFFECTED_PATCH_APPLICABLE" for system "1000010000"
       And I should get channel "103"
       And I should get patch "2"

     Then I logout from XML-RPC/cve audit namespace.
