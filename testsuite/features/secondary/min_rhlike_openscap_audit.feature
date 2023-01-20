# Copyright (c) 2017-2023 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_openscap
@scope_res
@rhlike_minion
Feature: OpenSCAP audit of Red Hat-like Salt minion
  In order to audit a Red Hat-like Salt minion
  As an authorized user
  I want to run an OpenSCAP scan on it

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Enable repositories for openSCAP on the Red Hat-like minion
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "no-appstream-8-result-RHEL8-Pool for x86_64"
    And I check "no-appstream-8-result-Custom Channel for Rocky 8 DVD"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed
    When I enable repository "Rocky-BaseOS" on this "rhlike_minion"
    And I enable the repositories "tools_update_repo tools_pool_repo" on this "rhlike_minion"
    And I refresh the metadata for "rhlike_minion"

  Scenario: Install the OpenSCAP packages on the Red Hat-like minion
    Given I am on the Systems overview page of this "rhlike_minion"
    And I install OpenSCAP dependencies on "rhlike_minion"
    And I follow "Software" in the content area
    And I click on "Update Package List"
    And I wait until event "Package List Refresh" is completed

  Scenario: Schedule an OpenSCAP audit job on the Red Hat-like minion
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    And I wait at most 30 seconds until I do not see "This system does not yet have OpenSCAP scan capability." text, refreshing the page
    And I enter "--profile standard" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-centos8-xccdf.xml" as "path"
    And I click on "Schedule"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait at most 500 seconds until event "OpenSCAP xccdf scanning" is completed

  Scenario: Check the results of the OpenSCAP scan on the Red Hat-like minion
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Audit" in the content area
    And I follow "xccdf_org.open-scap_testresult_xccdf_org.ssgproject.content_profile_standard"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "RHEL-8" text
    And I should see a "XCCDF Rule Results" text
    # WORKAROUND for the missing Rocky 8 openSCAP profile
    # See https://github.com/SUSE/spacewalk/issues/19259
    # When I enter "pass" as the filtered XCCDF result type
    # And I click on the filter button
    # Then I should see a "rpm_verify_permissions" link

  Scenario: Cleanup: remove audit scans retention period from Red Hat-like minion
    When I follow the left menu "Admin > Organizations"
    When I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    And I enter "0" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

  Scenario: Cleanup: delete audit results from Red Hat-like minion
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Audit" in the content area
    And I follow "List Scans" in the content area
    And I click on "Select All"
    And I click on "Remove Selected Scans"
    And I click on "Confirm"
    Then I should see a " SCAP Scan(s) deleted. 0 SCAP Scan(s) retained" text

  Scenario: Cleanup: restore audit scans retention period on Red Hat-like minion
    When I follow the left menu "Admin > Organizations"
    When I follow "SUSE Test" in the content area
    And I follow "Configuration" in the content area
    And I enter "90" as "scap_retention_period"
    And I click on "Update Organization"
    Then I should see a "Organization SUSE Test was successfully updated." text

  Scenario: Cleanup: remove the OpenSCAP packages from the Red Hat-like minion
    When I remove OpenSCAP dependencies from "rhlike_minion"
    And I disable repository "Rocky-BaseOS" on this "rhlike_minion"
    And I disable the repositories "tools_update_repo tools_pool_repo" on this "rhlike_minion"

  Scenario: Cleanup: restore the base channel for the Red Hat-like minion
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Fake Base Channel"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed
