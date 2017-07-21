# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Use the openSCAP audit feature in SUSE Manager

  Scenario: Schedule an audit job
    Given I am on the Systems overview page of this "sle-client"
    And I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    When I enter "--profile RHEL6-Default" as "params"
    And I enter "/usr/share/openscap/scap-rhel6-xccdf.xml" as "path"
    And I click on "Schedule"
    And I run rhn_check on this client
    Then I should see a "XCCDF scan has been scheduled" text

  Scenario: Check results of the audit job
    Given I am on the Systems overview page of this "sle-client"
    And I follow "Audit" in the content area
    When I follow "xccdf_org.open-scap_testresult_RHEL6-Default"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "RHEL6-Default" text
    And I should see a "XCCDF Rule Results" text
    And I should see a "CCE-" text
    And I should see a "rule-" link

  Scenario: Schedule an audit job using the suse profile
    Given I am on the Systems overview page of this "sle-client"
    And I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    When I enter "--profile Default" as "params"
    And I enter "/usr/share/openscap/scap-yast2sec-xccdf.xml" as "path"
    And I click on "Schedule"
    And I run rhn_check on this client
    Then I should see a "XCCDF scan has been scheduled" text

  Scenario: Check results of the audit job suse profile
    Given I am on the Systems overview page of this "sle-client"
    And I follow "Audit" in the content area
    When I follow "xccdf_org.open-scap_testresult_Default"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "Default" text
    And I should see a "XCCDF Rule Results" text
    And I should see a "pass" text
    And I should see a "rule-" link

  Scenario: Check NAGIOS status of last action
    Given I perform a nagios check last event for "sle-client"
    Then I should see Completed: OpenSCAP xccdf scanning scheduled by admin

  Scenario: Cleanup: remove audit scans retention period (trad-client)
    Given I am on the Organizations page
    And I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    When I enter "0" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

  Scenario: Delete audit results (trad-client)
    Given I am on the Systems overview page of this "sle-client"
    And I follow "Audit" in the content area
    And I follow "List Scans" in the content area
    When I click on "Select All"
    And I click on "Remove Selected Scans"
    And I click on "Confirm"
    Then I should see a "deleted. 0 SCAP Scan(s) retained" text

  Scenario: Restore audit scans retention period (trad-client)
    Given I am on the Organizations page
    And I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    When I enter "90" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text
