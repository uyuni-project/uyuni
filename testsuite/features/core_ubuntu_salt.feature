# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.
#
# 1) register an Ubuntu minion via GUI
# 2) run a remote command
# 3) try an openscap scan

Feature: Be able to bootstrap an Ubuntu minion and do some basic operations on it

@ubuntu_minion
  Scenario: Bootstrap an Ubuntu minion
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "ubuntu-minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-UBUNTU-TEST" from "activationKeys"
    And I select the hostname of the proxy from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host! " text
    And I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "ubuntu-minion", refreshing the page
    And I wait until onboarding is completed for "ubuntu-minion"

@proxy
@ubuntu_minion
  Scenario: Check connection from Ubuntu minion to proxy
    Given I am on the Systems overview page of this "ubuntu-minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" hostname

@proxy
@ubuntu_minion
  Scenario: Check registration on proxy of Ubuntu minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ubuntu-minion" hostname

@ubuntu_minion
  Scenario: Detect latest Salt changes on the Ubuntu minion
    When I query latest Salt changes on ubuntu system "ubuntu-minion"

@ubuntu_minion
  Scenario: Schedule an OpenSCAP audit job for the Ubuntu minion
    Given I am on the Systems overview page of this "ubuntu-minion"
    When I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    And I enter "--profile common" as "params"
    And I enter "/usr/share/scap-security-guide/ssg-ubuntu1604-xccdf.xml" as "path"
    And I click on "Schedule"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait until event "OpenSCAP xccdf scanning" is completed

@ubuntu_minion
  Scenario: Run a remote command on the Ubuntu minion
    Given I am authorized as "testing" with password "testing"
    When I follow "Salt"
    And I follow "Remote Commands"
    Then I should see a "Remote Commands" text
    When I enter command "cat /etc/os-release"
    And I enter target "*ubuntu*"
    And I click on preview
    And I click on run
    Then I should see "ubuntu-minion" hostname
    When I wait for "15" seconds
    And I expand the results for "ubuntu-minion"
    Then I should see a "ID=ubuntu" text

@ubuntu_minion
  Scenario: Check the results of the OpenSCAP scan on the Ubuntu minion
    Given I am on the Systems overview page of this "ubuntu-minion"
    When I follow "Audit" in the content area
    And I follow "xccdf_org.open-scap_testresult_common"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "Ubuntu" text
    And I should see a "XCCDF Rule Results" text
    And I should see a "pass" text or "notapplicable" text
    And I should see a "report.html" link
