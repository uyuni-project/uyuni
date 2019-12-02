# Copyright (c) 2017-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: openSCAP audit of Ubuntu Salt minion
  In order to audit an Ubuntu Salt minion
  As an authorized user
  I want to run an openSCAP scan on it

@ubuntu_minion
  Scenario: Schedule an OpenSCAP audit job for the Ubuntu minion
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
    Given I am authorized with the feature's user
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
    And I should see a "pass" text or "notapplicable" text
    And I should see a "report.html" link
    And I should see a "results.xml" link
