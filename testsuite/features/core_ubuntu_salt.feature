# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.
#
# 1) register an Ubuntu minion via GUI
# 2) run a remote command
# 3) try an openscap scan

Feature: Be able to bootstrap an Ubuntu minion and do some basic operations on it

@proxy
@ubuntu_minion
  Scenario: Bootstrap a Ubuntu minion
    Given I am authorized
    And I go to the bootstrapping page
    And I should see a "Bootstrap Minions" text
    And I enter the hostname of "ubuntu-minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-PKG-x86_64" from "activationKeys"
    And I select the hostname of the proxy from "proxies"
    When I click on "Bootstrap"
    Then I wait until I see "Successfully bootstrapped host!" text
    And I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "ubuntu-minion", refreshing the page
    And I wait until onboarding is completed for "ubuntu-minion"
    And I query latest Salt changes on "ubuntu-minion"

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

@proxy
@ubuntu_minion
  Scenario: Schedule an OpenSCAP audit job for the Ubuntu minion
    Given I am on the Systems overview page of this "ubuntu-minion"
    When I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    And I enter "--profile standard" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-centos7-xccdf.xml" as "path"
    And I click on "Schedule"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait until event "OpenSCAP xccdf scanning" is completed

@proxy
@ubuntu_minion
  Scenario: Run a remote command on the Ubuntu minion
    Given I am authorized
    And I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    And I enter command "cat /etc/os-release"
    And I enter target "*ubuntu*"
    And I click on preview
    When I click on run
    Then I should see "ubuntu-minion" hostname
    And I wait until I see "show response" text
    And I expand the results for "ubuntu-minion"
    And I should see a "rhel fedora" text
    And I should see a "REDHAT_SUPPORT_PRODUCT" text

@proxy
@ubuntu_minion
  Scenario: Check the results of the OpenSCAP scan on the Ubuntu minion
    Given I am on the Systems overview page of this "ubuntu-minion"
    When I follow "Audit" in the content area
    And I follow "xccdf_org.open-scap_testresult_standard"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "RHEL-7" text
    And I should see a "XCCDF Rule Results" text
    And I should see a "pass" text or "notapplicable" text
    And I should see a "rpm_" link
