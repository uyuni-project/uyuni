# Copyright (c) 2017-2019 SUSE LLC
# Licensed under the terms of the MIT license.
#
# 1) delete CentOS SSH minion and register as Centos minion
# 2) run a remote command
# 3) try an openscap scan
# 4) delete CentOS minion client and register as Centos SSH minion

Feature: Be able to bootstrap a CentOS minion and do some basic operations on it

@centos_minion
  Scenario: Delete the CentOS SSH minion
    When I am on the Systems overview page of this "ceos-ssh-minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    Then I should see a "has been deleted" text
    And "ceos-ssh-minion" should not be registered

@centos_minion
  Scenario: Bootstrap a CentOS minion
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "ceos-minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-PKG-x86_64" from "activationKeys"
    And I select the hostname of the proxy from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host! " text
    And I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "ceos-minion", refreshing the page
    And I wait until onboarding is completed for "ceos-minion"

@proxy
@centos_minion
  Scenario: Check connection from CentOS minion to proxy
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" hostname

@proxy
@centos_minion
  Scenario: Check registration on proxy of CentOS minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos-minion" hostname

@centos_minion
  Scenario: Re-subscribe the CentOS minion to a base channel
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test Base Channel"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

@centos_minion
  Scenario: Detect latest Salt changes on the CentOS minion
    When I query latest Salt changes on "ceos-minion"

@centos_minion
  Scenario: Schedule an OpenSCAP audit job for the CentOS minion
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    And I enter "--profile standard" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-centos7-xccdf.xml" as "path"
    And I click on "Schedule"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait until event "OpenSCAP xccdf scanning" is completed

@centos_minion
  Scenario: Run a remote command on the CentOS minion
    Given I am authorized as "testing" with password "testing"
    When I follow "Salt"
    And I follow "Remote Commands"
    Then I should see a "Remote Commands" text
    When I enter command "cat /etc/os-release"
    And I enter target "*centos*"
    And I click on preview
    And I click on run
    Then I should see "ceos-minion" hostname
    When I wait for "15" seconds
    And I expand the results for "ceos-minion"
    Then I should see a "rhel fedora" text
    And I should see a "REDHAT_SUPPORT_PRODUCT" text

@centos_minion
  Scenario: Check the results of the OpenSCAP scan on the CentOS minion
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow "Audit" in the content area
    And I follow "xccdf_org.open-scap_testresult_standard"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "RHEL-7" text
    And I should see a "XCCDF Rule Results" text
    And I should see a "pass" text
    And I should see a "rpm_" link

@centos_minion
  Scenario: Check events history for failures on CentOS minion
    Given I am on the Systems overview page of this "ceos-minion"
    Then I check for failed events on history event page

@centos_minion
  Scenario: Cleanup: delete the CentOS minion
    When I am on the Systems overview page of this "ceos-minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    Then I should see a "has been deleted" text
    And "ceos-minion" should not be registered

@centos_minion
  Scenario: Cleanup: bootstrap a SSH-managed CentOS minion
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "ceos-ssh-minion" as "hostname"
    And I enter "linux" as "password"
    And I select the hostname of the proxy from "proxies"
    And I click on "Bootstrap"
    Then I wait until I see "Successfully bootstrapped host! " text
    And I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "ceos-ssh-minion", refreshing the page
    And I wait until onboarding is completed for "ceos-ssh-minion"

@centos_minion
  Scenario: Cleanup: re-subscribe the SSH-managed CentOS minion to a base channel
    Given I am on the Systems overview page of this "ceos-ssh-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test Base Channel"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed
