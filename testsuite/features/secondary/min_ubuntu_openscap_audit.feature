# Copyright (c) 2017-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: OpenSCAP audit of Ubuntu Salt minion
  In order to audit an Ubuntu Salt minion
  As an authorized user
  I want to run an OpenSCAP scan on it

@ubuntu_minion
  Scenario: Install the OpenSCAP packages on the Ubuntu minion
    Given I am on the Systems overview page of this "ubuntu_minion"
    When I enable universe repositories on "ubuntu_minion"
    And I install OpenSCAP dependencies on "ubuntu_minion"
    And I follow "Software" in the content area
    And I click on "Update Package List"
    And I follow "Events" in the content area
    And I wait until I do not see "Package List Refresh scheduled by admin" text, refreshing the page

@ubuntu_minion
  Scenario: Schedule an OpenSCAP audit job on the Ubuntu minion
    Given I am on the Systems overview page of this "ubuntu_minion"
    When I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    And I enter "--profile standard" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-ubuntu1604-xccdf.xml" as "path"
    And I click on "Schedule"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait at most 500 seconds until event "OpenSCAP xccdf scanning" is completed

@ubuntu_minion
  Scenario: Run a remote command on the Ubuntu minion
    Given I am authorized
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "cat /etc/os-release"
    And I enter target "*ubuntu*"
    And I click on preview
    And I click on run
    Then I should see "ubuntu_minion" hostname
    When I wait until I see "show response" text
    And I expand the results for "ubuntu_minion"
    Then I should see a "ID=ubuntu" text

@ubuntu_minion
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

@ubuntu_minion
  Scenario: Cleanup: remove audit scans retention period from Ubuntu minion
    Given I am on the Organizations page
    When I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    And I enter "0" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

@ubuntu_minion
  Scenario: Cleanup: delete audit results from Ubuntu minion
    Given I am on the Systems overview page of this "ubuntu_minion"
    When I follow "Audit" in the content area
    And I follow "List Scans" in the content area
    And I click on "Select All"
    And I click on "Remove Selected Scans"
    And I click on "Confirm"
    Then I should see a "1 SCAP Scan(s) deleted. 0 SCAP Scan(s) retained" text

@ubuntu_minion
  Scenario: Cleanup: restore audit scans retention period on Ubuntu minion
    Given I am on the Organizations page
    When I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    And I enter "90" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

@ubuntu_minion
  Scenario: Cleanup: remove the openSCAP packages from the Ubuntu minion
    When I remove OpenSCAP dependencies from "ubuntu_minion"
    When I disable universe repositories on "ubuntu_minion"
