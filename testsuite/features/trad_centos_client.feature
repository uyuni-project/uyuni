# Copyright (c) 2017-2019 SUSE LLC
# Licensed under the terms of the MIT license.
#
# 1) delete CentOS SSH minion and register a CentOS traditional client
# 2) run an OpenSCAP audit
# 3) refresh packages
# 4) run a script
# 5) reboot
# 6) delete the traditional client and register as Centos SSH minion

Feature: Be able to register a CentOS 7 traditional client and do some basic operations on it

@centos_minion
  Scenario: Delete the CentOS SSH minion
    When I am on the Systems overview page of this "ceos-ssh-minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    Then I should see a "has been deleted" text
    And I run "systemctl disable salt-minion" on "ceos-ssh-minion" without error control
    And "ceos-ssh-minion" should not be registered

@centos_minion
  Scenario: Prepare a CentOS 7 traditional client
    Given I am authorized
    When I enable repository "Devel_Galaxy_Manager_3.2_RES-Manager-Tools-7-x86_64" on this "ceos-client"
    And I enable repository "SLE-Manager-Tools-RES-7-x86_64" on this "ceos-client"
    And I enable repository "CentOS-Base" on this "ceos-client"
    And I install package "hwdata m2crypto wget" on this "ceos-client"
    And I install package "rhn-client-tools rhn-check rhn-setup rhnsd osad rhncfg-actions" on this "ceos-client"
    And I install package "spacewalk-oscap scap-security-guide" on this "ceos-client"
    And I register "ceos-client" as traditional client
    And I run "rhn-actions-control --enable-all" on "ceos-client"

@proxy
@centos_minion
  Scenario: Check connection from CentOS 7 traditional to proxy
    Given I am on the Systems overview page of this "ceos-client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" hostname

@proxy
@centos_minion
  Scenario: Check registration on proxy of traditional CentOS 7
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos-client" hostname

@centos_minion
  Scenario: Re-subscribe the CentOS traditional client to a base channel
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
  Scenario: Schedule an OpenSCAP audit job for the CentOS traditional client
    Given I am on the Systems overview page of this "ceos-client"
    When I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    And I enter "--profile standard" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-centos7-xccdf.xml" as "path"
    And I click on "Schedule"
    And I run "rhn_check -vvv" on "ceos-client"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait until event "OpenSCAP xccdf scanning" is completed

@centos_minion
  Scenario: Check the results of the OpenSCAP scan on the CentOS traditional client
    Given I am on the Systems overview page of this "ceos-client"
    When I follow "Audit" in the content area
    And I follow "xccdf_org.open-scap_testresult_standard"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "RHEL-7" text
    And I should see a "XCCDF Rule Results" text
    And I should see a "pass" text
    And I should see a "service_" link

@centos_minion
  Scenario: Schedule some actions on the CentOS 7 traditional client
    Given I am authorized as "admin" with password "admin"
    When I authenticate to XML-RPC
    And I refresh the packages on "ceos-client" through XML-RPC
    And I run a script on "ceos-client" through XML-RPC
    And I reboot "ceos-client" through XML-RPC
    And I unauthenticate from XML-RPC

@centos_minion
  Scenario: Cleanup: delete the CentOS 7 traditional client
    Given I am on the Systems overview page of this "ceos-client"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    And I wait until I see "has been deleted." text

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
  Scenario: Cleanup: re-subscribe the new SSH-managed CentOS minion to a base channel
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
