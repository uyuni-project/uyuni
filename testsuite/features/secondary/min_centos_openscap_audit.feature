# Copyright (c) 2017-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: openSCAP audit of CentOS Salt minion
  In order to audit a CentOS Salt minion
  As an authorized user
  I want to run an openSCAP scan on it

@centos_minion
  Scenario: Prepare the CentOS minion
    Given I am authorized
    When I enable SUSE Manager tools repositories on "ceos_minion"
    And I enable repository "CentOS-Base" on this "ceos_minion"
    And I install OpenSCAP centos dependencies on "ceos_minion"

@centos_minion
  Scenario: Schedule an OpenSCAP audit job for the CentOS minion
    Given I am on the Systems overview page of this "ceos_minion"
    When I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    And I enter "--profile standard" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-rhel7-xccdf.xml" as "path"
    And I click on "Schedule"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait at most 500 seconds until event "OpenSCAP xccdf scanning" is completed

@centos_minion
  Scenario: Run a remote command on the CentOS minion
    Given I am authorized as "testing" with password "testing"
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "cat /etc/os-release"
    And I enter target "*centos*"
    And I click on preview
    And I click on run
    Then I should see "ceos_minion" hostname
    When I wait for "15" seconds
    And I expand the results for "ceos_minion"
    Then I should see a "rhel fedora" text
    And I should see a "REDHAT_SUPPORT_PRODUCT" text

@centos_minion
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

@centos_minion
  Scenario: Cleanup: remove audit scans retention period from CentOS minion
    Given I am on the Organizations page
    When I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    And I enter "0" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

@centos_minion
  Scenario: Cleanup: delete audit results from CentOS minion
    Given I am on the Systems overview page of this "ceos_minion"
    When I follow "Audit" in the content area
    And I follow "List Scans" in the content area
    And I click on "Select All"
    And I click on "Remove Selected Scans"
    And I click on "Confirm"
    Then I should see a "1 SCAP Scan(s) deleted. 0 SCAP Scan(s) retained" text

@centos_minion
  Scenario: Cleanup: restore audit scans retention period on CentOS minion
    Given I am on the Organizations page
    When I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    And I enter "90" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text
