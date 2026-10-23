# Copyright (c) 2017-2023 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
@scope_openscap
Feature: OpenSCAP audit of Salt minion
  In order to audit a Salt minion
  As an authorized user
  I want to run an OpenSCAP scan on it

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section
    And I am on the Systems overview page of this "sle_minion"

@Uyuni
  Scenario: Enable required repositories
    When I enable repository "repo-oss" on this "sle_minion" without error control

@susemanager
  Scenario: Enable required repositories
    When I enable repository "os_pool_repo" on this "sle_minion" without error control

  Scenario: Install the OpenSCAP packages on the SLE minion
    When I refresh the metadata for "sle_minion"
    And I install OpenSCAP dependencies on "sle_minion"
    And I follow "Software" in the content area
    And I click on "Update Package List"
    And I wait until event "Package List Refresh" is completed

@susemanager
  Scenario: Upload the SCAP content of the SLE minion to the server
    When I follow the left menu "Audit > OpenSCAP > SCAP Content"
    And I follow "Upload" in the content area
    And I enter "SLE 15 SSG" as "name"
    And I attach the remote file "/usr/share/xml/scap/ssg/content/ssg-sle15-ds.xml" on "sle_minion" to "scapFile"
    And I attach the remote file "/usr/share/xml/scap/ssg/content/ssg-sle15-xccdf.xml" on "sle_minion" to "xccdfFile"
    And I click on "Upload"
    Then I should see a "SLE 15 SSG" text

@uyuni
  Scenario: Upload the SCAP content of the SLE minion to the server
    When I follow the left menu "Audit > OpenSCAP > SCAP Content"
    And I follow "Upload" in the content area
    And I enter "openSUSE SSG" as "name"
    And I attach the remote file "/usr/share/xml/scap/ssg/content/ssg-opensuse-ds.xml" on "sle_minion" to "scapFile"
    And I attach the remote file "/usr/share/xml/scap/ssg/content/ssg-opensuse-xccdf.xml" on "sle_minion" to "xccdfFile"
    And I click on "Upload"
    Then I should see a "openSUSE SSG" text

@susemanager
  Scenario: Schedule an OpenSCAP audit job on the SLE minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Audit" in the content area
    And I follow "OpenSCAP" in the content area
    And I follow "Schedule" in the content area
    And I wait at most 30 seconds until I do not see "This system does not yet have OpenSCAP scan capability." text, refreshing the page
    And I select "SLE 15 SSG" from "dataStreamName"
    And I select "Standard System Security Profile" from "xccdfProfileId"
    And I click on "Schedule"
    Then I should see a "SCAP scan has been scheduled." text
    And I wait at most 500 seconds until event "OpenSCAP xccdf scanning" is completed

@uyuni
  Scenario: Schedule an OpenSCAP audit job on the SLE minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Audit" in the content area
    And I follow "OpenSCAP" in the content area
    And I follow "Schedule" in the content area
    And I wait at most 30 seconds until I do not see "This system does not yet have OpenSCAP scan capability." text, refreshing the page
    And I select "openSUSE SSG" from "dataStreamName"
    And I select "Standard System Security Profile" from "xccdfProfileId"
    And I click on "Schedule"
    Then I should see a "SCAP scan has been scheduled." text
    And I wait at most 500 seconds until event "OpenSCAP xccdf scanning" is completed

@susemanager
  Scenario: Check results of the audit job on the minion
    When I follow "Audit" in the content area
    And I follow "OpenSCAP" in the content area
    And I follow "xccdf_org.open-scap_testresult"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "Standard System Security Profile" text
    And I should see a "XCCDF Rule Results" text
    When I enter "pass" as the filtered XCCDF result type
    And I click on the filter button
    Then I should see a "xccdf_org.ssgproject.content_rule_service_httpd_disabled" link

@uyuni
  Scenario: Check results of the audit job on the minion
    When I follow "Audit" in the content area
    And I follow "OpenSCAP" in the content area
    And I follow "xccdf_org.open-scap_testresult"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "Standard System Security Profile" text
    And I should see a "XCCDF Rule Results" text
    When I enter "pass" as the filtered XCCDF result type
    And I click on the filter button
    Then I should see a "xccdf_org.ssgproject.content_rule_file_permissions_etc_passwd" link

@susemanager
  Scenario: Create a second, almost identical, audit job
    When I follow "Audit" in the content area
    And I follow "OpenSCAP" in the content area
    And I follow "Schedule" in the content area
    And I wait at most 30 seconds until I do not see "This system does not yet have OpenSCAP scan capability." text, refreshing the page
    And I select "SLE 15 SSG" from "dataStreamName"
    And I select "Standard System Security Profile" from "xccdfProfileId"
    And I click on "Schedule"
    Then I should see a "SCAP scan has been scheduled." text
    When I wait for the OpenSCAP audit to finish

@uyuni
  Scenario: Create a second, almost identical, audit job
    When I follow "Audit" in the content area
    And I follow "OpenSCAP" in the content area
    And I follow "Schedule" in the content area
    And I wait at most 30 seconds until I do not see "This system does not yet have OpenSCAP scan capability." text, refreshing the page
    And I select "openSUSE SSG" from "dataStreamName"
    And I select "Standard System Security Profile" from "xccdfProfileId"
    And I click on "Schedule"
    Then I should see a "SCAP scan has been scheduled." text
    When I wait for the OpenSCAP audit to finish

  Scenario: Compare audit results
    When I follow "Audit" in the content area
    And I follow "OpenSCAP" in the content area
    And I follow "List Scans" in the content area
    And I click on "Select All"
    And I click on "Compare"
    Then I should see a "XCCDF Rule Results" text
    And I should see a "None" text

  Scenario: Cleanup: remove audit scans retention period
    When I follow the left menu "Admin > Organizations"
    When I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    And I enter "0" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

  Scenario: Cleanup: delete audit results
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Audit" in the content area
    And I follow "OpenSCAP" in the content area
    And I follow "List Scans" in the content area
    And I click on "Select All"
    And I click on "Remove"
    And I click on "Confirm"
    Then I should see a "2 SCAP Scan(s) deleted. 0 SCAP Scan(s) retained" text

  Scenario: Cleanup: restore audit scans retention period
    When I follow the left menu "Admin > Organizations"
    When I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    And I enter "90" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

@susemanager
  Scenario: Cleanup: delete the SCAP content from the server
    When I follow the left menu "Audit > OpenSCAP > SCAP Content"
    And I click on "Delete" in row "SLE 15 SSG"
    And I click on "Delete" in element "delete-modal"
    Then I should see a "SCAP content has been deleted." text

@uyuni
  Scenario: Cleanup: delete the SCAP content from the server
    When I follow the left menu "Audit > OpenSCAP > SCAP Content"
    And I click on "Delete" in row "openSUSE SSG"
    And I click on "Delete" in element "delete-modal"
    Then I should see a "SCAP content has been deleted." text

  Scenario: Cleanup: remove the OpenSCAP packages from the SLE minion
    When I remove OpenSCAP dependencies from "sle_minion"

@Uyuni
  Scenario: Cleanup: Disable required repositories
    When I disable repository "repo-oss" on this "sle_minion" without error control

@susemanager
  Scenario: Cleanup: Disable required repositories
    When I disable repository "os_pool_repo" on this "sle_minion" without error control
