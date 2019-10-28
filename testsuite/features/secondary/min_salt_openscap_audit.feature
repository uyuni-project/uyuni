# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: openSCAP audit of Salt minion
  In order to audit a Salt minion
  As an authorized user
  I want to run an openSCAP scan on it

  Scenario: Schedule an audit job on the minion
    Given I disable IPv6 forwarding on all interfaces of the SLE minion
    And I am on the Systems overview page of this "sle-minion"
    And I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    When I enter "--profile Default" as "params"
    And I enter "/usr/share/openscap/scap-yast2sec-xccdf.xml" as "path"
    And I click on "Schedule"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait until event "OpenSCAP xccdf scanning" is completed

  Scenario: Check results of the audit job on the minion
    Given I am on the Systems overview page of this "sle-minion"
    And I follow "Audit" in the content area
    When I follow "xccdf_org.open-scap_testresult_Default"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "Default" text
    And I should see a "XCCDF Rule Results" text
    And I should see a "pass" text or "notapplicable" text
    And I should see a "rule-" link

  Scenario: Create a second, almost identical, audit job
    Given I enable IPv6 forwarding on all interfaces of the SLE minion
    And I am on the Systems overview page of this "sle-minion"
    And I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    When I enter "--profile Default" as "params"
    And I enter "/usr/share/openscap/scap-yast2sec-xccdf.xml" as "path"
    And I click on "Schedule"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait for the openSCAP audit to finish
    And I disable IPv6 forwarding on all interfaces of the SLE minion

  Scenario: Compare audit results
    Given I am on the Systems overview page of this "sle-minion"
    And I follow "Audit" in the content area
    And I follow "List Scans" in the content area
    When I click on "Select All"
    And I click on "Compare Selected Scans"
    Then I should see a "XCCDF Rule Results" text
    And I should see a "rule-sysctl-ipv6-all-forward" text

  Scenario: Remove audit scans retention period
    Given I am on the Organizations page
    And I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    When I enter "0" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

  Scenario: Delete audit results
    Given I am on the Systems overview page of this "sle-minion"
    And I follow "Audit" in the content area
    And I follow "List Scans" in the content area
    When I click on "Select All"
    And I click on "Remove Selected Scans"
    And I click on "Confirm"
    Then I should see a "2 SCAP Scan(s) deleted. 0 SCAP Scan(s) retained" text

  Scenario: Cleanup: restore audit scans retention period
    Given I am on the Organizations page
    And I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    When I enter "90" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text
