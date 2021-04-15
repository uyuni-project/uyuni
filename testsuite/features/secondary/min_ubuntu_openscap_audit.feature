# Copyright (c) 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_openscap
@scope_ubuntu
@ubuntu_minion
Feature: OpenSCAP audit of Ubuntu Salt minion
  In order to audit an Ubuntu Salt minion
  As an authorized user
  I want to run an OpenSCAP scan on it

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

@ubuntu_minion
@uyuni
  Scenario: Install the client tools packages for Uyuni on the Ubuntu minion
    When I enable Uyuni tools repositories on "ubuntu_minion"

@ubuntu_minion
@susemanager
  Scenario: Install the client tools packages for SUSE Manager on the Ubuntu minion
    When I enable SUSE Manager tools repositories on "ubuntu_minion"

@ubuntu_minion
  Scenario: Install the OpenSCAP packages on the Ubuntu minion
    Given I am on the Systems overview page of this "ubuntu_minion"
    When I enable universe repositories on "ubuntu_minion"
    And I refresh the metadata for "ubuntu_minion"
    And I install OpenSCAP dependencies on "ubuntu_minion"
    And I follow "Software" in the content area
    And I click on "Update Package List"
    And I follow "Events" in the content area
    And I wait until I do not see "Package List Refresh scheduled by admin" text, refreshing the page

  Scenario: Schedule an OpenSCAP audit job on the Ubuntu minion
    Given I am on the Systems overview page of this "ubuntu_minion"
    When I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    And I enter "--profile standard" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-ubuntu2004-xccdf.xml" as "path"
    And I click on "Schedule"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait at most 500 seconds until event "OpenSCAP xccdf scanning" is completed

  Scenario: Check the results of the OpenSCAP scan on the Ubuntu minion
    Given I am on the Systems overview page of this "ubuntu_minion"
    When I follow "Audit" in the content area
    And I follow "xccdf_org.open-scap_testresult_standard"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "Ubuntu" text
    And I should see a "XCCDF Rule Results" text
    When I enter "pass" as the filtered XCCDF result type
    And I click on the filter button
    # TODO: make at least one rule pass on Ubuntu
    Then I should see a "report.html" link

  Scenario: Cleanup: remove audit scans retention period from Ubuntu minion
    Given I am on the Organizations page
    When I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    And I enter "0" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

  Scenario: Cleanup: delete audit results from Ubuntu minion
    Given I am on the Systems overview page of this "ubuntu_minion"
    When I follow "Audit" in the content area
    And I follow "List Scans" in the content area
    And I click on "Select All"
    And I click on "Remove Selected Scans"
    And I click on "Confirm"
    Then I should see a " SCAP Scan(s) deleted. 0 SCAP Scan(s) retained" text

  Scenario: Cleanup: restore audit scans retention period on Ubuntu minion
    Given I am on the Organizations page
    When I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    And I enter "90" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

  Scenario: Cleanup: remove the OpenSCAP packages from the Ubuntu minion
    When I remove OpenSCAP dependencies from "ubuntu_minion"
    When I disable universe repositories on "ubuntu_minion"

@ubuntu_minion
@uyuni
  Scenario: Cleanup: remove the client tools packages for Uyuni on the Ubuntu minion
    When I disable Uyuni tools repositories on "ubuntu_minion"

@ubuntu_minion
@susemanager
  Scenario: Cleanup: remove the client tools packages for SUSE Manager on the Ubuntu minion
    When I disable SUSE Manager tools repositories on "ubuntu_minion"
