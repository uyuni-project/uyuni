# Copyright (c) 2012 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Use the openSCAP audit feature in SUSE Manager

  Scenario: Schedule an audit job
    Given I am on the Systems overview page of this client
     And I follow "Audit" in class "content-nav"
     And I follow "Schedule" in class "contentnav-row2"
    When I enter "--profile RHEL6-Default" as "params"
     And I enter "/usr/share/openscap/scap-rhel6-xccdf.xml" as "path"
     And I click on "Schedule"
     And I run rhn_check on this client
    Then I should see a "XCCDF scan has been scheduled" text

  Scenario: Check results of the audit job
    Given I am on the Systems overview page of this client
      And I follow "Audit" in class "content-nav"
     When I follow "xccdf_org.open-scap_testresult_RHEL6-Default"
     Then I should see a "Details of XCCDF Scan" text
      And I should see a "RHEL6-Default" text
      And I should see a "XCCDF Rule Results" text
      And I should see a "CCE-" text
      And I should see a "rule-" link

  Scenario: Schedule an audit job using the suse profile
    Given I am on the Systems overview page of this client
     And I follow "Audit" in class "content-nav"
     And I follow "Schedule" in class "contentnav-row2"
    When I enter "--profile Default" as "params"
     And I enter "/usr/share/openscap/scap-yast2sec-xccdf.xml" as "path"
     And I click on "Schedule"
     And I run rhn_check on this client
    Then I should see a "XCCDF scan has been scheduled" text

  Scenario: Check results of the audit job
    Given I am on the Systems overview page of this client
      And I follow "Audit" in class "content-nav"
     When I follow "xccdf_org.open-scap_testresult_Default"
     Then I should see a "Details of XCCDF Scan" text
      And I should see a "Default" text
      And I should see a "XCCDF Rule Results" text
      And I should see a "pass" text
      And I should see a "rule-" link


