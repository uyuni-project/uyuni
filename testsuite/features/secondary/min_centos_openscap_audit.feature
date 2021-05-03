# Copyright (c) 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_openscap
@scope_res
@centos_minion
Feature: OpenSCAP audit of CentOS Salt minion
  In order to audit a CentOS Salt minion
  As an authorized user
  I want to run an OpenSCAP scan on it

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Install the OpenSCAP packages on the CentOS minion
    Given I am on the Systems overview page of this "ceos_minion"
    When I enable repository "CentOS-Base" on this "ceos_minion"
    And I enable SUSE Manager tools repositories on "ceos_minion"
    And I refresh the metadata for "ceos_minion"
    And I install OpenSCAP dependencies on "ceos_minion"
    And I fix CentOS 7 OpenSCAP files on "ceos_minion"
    And I follow "Software" in the content area
    And I click on "Update Package List"
    And I follow "Events" in the content area
    And I wait until I do not see "Package List Refresh scheduled by admin" text, refreshing the page

  Scenario: Schedule an OpenSCAP audit job on the CentOS minion
    Given I am on the Systems overview page of this "ceos_minion"
    When I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    And I enter "--profile standard" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-rhel7-xccdf.xml" as "path"
    And I click on "Schedule"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait at most 500 seconds until event "OpenSCAP xccdf scanning" is completed

  Scenario: Check the results of the OpenSCAP scan on the CentOS minion
    Given I am on the Systems overview page of this "ceos_minion"
    When I follow "Audit" in the content area
    And I follow "xccdf_org.open-scap_testresult_standard"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "RHEL-7" text
    And I should see a "XCCDF Rule Results" text
    When I enter "pass" as the filtered XCCDF result type
    And I click on the filter button
    Then I should see a "rpm_verify_permissions" link

  Scenario: Cleanup: remove audit scans retention period from CentOS minion
    When I follow the left menu "Admin > Organizations"
    When I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    And I enter "0" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

  Scenario: Cleanup: delete audit results from CentOS minion
    Given I am on the Systems overview page of this "ceos_minion"
    When I follow "Audit" in the content area
    And I follow "List Scans" in the content area
    And I click on "Select All"
    And I click on "Remove Selected Scans"
    And I click on "Confirm"
    Then I should see a " SCAP Scan(s) deleted. 0 SCAP Scan(s) retained" text

  Scenario: Cleanup: restore audit scans retention period on CentOS minion
    When I follow the left menu "Admin > Organizations"
    When I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    And I enter "90" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

  Scenario: Cleanup: remove the OpenSCAP packages from the CentOS minion
    When I remove OpenSCAP dependencies from "ceos_minion"
    And I disable repository "CentOS-Base" on this "ceos_minion"
    And I disable SUSE Manager tools repositories on "ceos_client"
