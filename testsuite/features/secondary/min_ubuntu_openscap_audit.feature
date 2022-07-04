# Copyright (c) 2017-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_openscap
@scope_deb
@deb_minion
Feature: OpenSCAP audit of Debian-like Salt minion
  In order to audit a Debian-like Salt minion
  As an authorized user
  I want to run an OpenSCAP scan on it

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Enable all the necessary repositories for OpenSCAP on Debian-like minion
    When I enable Debian-like "universe" repository on "deb_minion"
    And I enable client tools repositories on "deb_minion"

  Scenario: Install the OpenSCAP packages on the Debian-like minion
    Given I am on the Systems overview page of this "deb_minion"
    When I refresh the metadata for "deb_minion"
    And I install OpenSCAP dependencies on "deb_minion"
    And I follow "Software" in the content area
    And I click on "Update Package List"
    And I wait until event "Package List Refresh" is completed

  Scenario: Schedule an OpenSCAP audit job on the Debian-like minion
    Given I am on the Systems overview page of this "deb_minion"
    When I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    And I wait at most 30 seconds until I do not see "This system does not yet have OpenSCAP scan capability." text, refreshing the page
    And I enter "--profile standard" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-ubuntu2004-xccdf.xml" as "path"
    And I click on "Schedule"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait at most 500 seconds until event "OpenSCAP xccdf scanning" is completed

  Scenario: Check the results of the OpenSCAP scan on the Debian-like minion
    Given I am on the Systems overview page of this "deb_minion"
    When I follow "Audit" in the content area
    And I follow "xccdf_org.open-scap_testresult_standard"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "Ubuntu" text
    And I should see a "XCCDF Rule Results" text
    When I enter "pass" as the filtered XCCDF result type
    And I click on the filter button
    # TODO: make at least one rule pass on Ubuntu
    Then I should see a "report.html" link

  Scenario: Cleanup: remove audit scans retention period from Debian-like minion
    When I follow the left menu "Admin > Organizations"
    When I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    And I enter "0" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

  Scenario: Cleanup: delete audit results from Debian-like minion
    Given I am on the Systems overview page of this "deb_minion"
    When I follow "Audit" in the content area
    And I follow "List Scans" in the content area
    And I click on "Select All"
    And I click on "Remove Selected Scans"
    And I click on "Confirm"
    Then I should see a " SCAP Scan(s) deleted. 0 SCAP Scan(s) retained" text

  Scenario: Cleanup: restore audit scans retention period on Debian-like minion
    When I follow the left menu "Admin > Organizations"
    When I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    And I enter "90" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

  Scenario: Cleanup: remove the OpenSCAP packages from the Debian-like minion
    When I remove OpenSCAP dependencies from "deb_minion"

  Scenario: Cleanup: remove all the necessary repositories for OpenSCAP on Debian-like minion
    When I disable client tools repositories on "deb_minion"
    And I disable Debian-like "universe" repository on "deb_minion"
