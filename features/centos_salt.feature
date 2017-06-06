# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: register a salt-minion via bootstrap
         run cmd and openscap scan, reboot test

  Scenario: bootstrap a centos minion
     Given I am authorized
     And I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     And  I enter the hostname of "ceos-minion" as hostname
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     And I wait until i see "Successfully bootstrapped host! " text
     And I navigate to "rhn/systems/Overview.do" page
     And I wait until i see "min-centos" text

 Scenario: Schedule an openscap-audit job for centos minion
    Given I am on the Systems overview page of this "ceos-minion"
    And I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    When I enter "--profile standard" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-centos7-xccdf.xml" as "path"
    And I click on "Schedule"
    Then I should see a "XCCDF scan has been scheduled" text

 Scenario: Run a remote command on centos
    Given I am authorized as "testing" with password "testing"
    And I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    Then I enter command "cat /etc/os-release"
    And I click on preview
    And I click on run
    Then I should see "ceos-minion" hostname
    And I wait for "15" seconds
    And I expand the results for "ceos-minion"
    And I should see a "rhel fedora" text
    Then I should see a "REDHAT_SUPPORT_PRODUCT" text

  Scenario: Reboot a salt minion centos 
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    And I click on "Reboot system"
    Then I wait and check that "ceos-minion" has rebooted
    And I wait until "salt-minion" service is up and running on "ceos-minion"

  Scenario: Check results of the openscap centos minion
    Given I am on the Systems overview page of this "ceos-minion"
    And I follow "Audit" in the content area
    When I follow "xccdf_org.open-scap_testresult_standard"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "RHEL-7" text
    And I should see a "XCCDF Rule Results" text
    And I should see a "pass" text
    And I should see a "rpm_" link
