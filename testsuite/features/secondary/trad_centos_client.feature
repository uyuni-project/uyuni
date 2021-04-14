# Copyright (c) 2017-2020 SUSE LLC
# Licensed under the terms of the MIT license.
#
# 1) delete CentOS minion and register a CentOS traditional client
# 2) run an OpenSCAP audit
# 3) refresh packages
# 4) run a script
# 5) reboot
# 6) delete the traditional client and register as Centos minion

@scope_traditional_client
@scope_res
@centos_minion
Feature: Be able to register a CentOS 7 traditional client and do some basic operations on it

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Delete the CentOS minion before traditional client tests
    When I am on the Systems overview page of this "ceos_minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "ceos_minion" should not be registered

  Scenario: Prepare the CentOS 7 traditional client
    When I enable SUSE Manager tools repositories on "ceos_client"
    And I enable repository "CentOS-Base" on this "ceos_client"
    And I install the traditional stack utils on "ceos_client"
    And I install OpenSCAP dependencies on "ceos_client"
    And I fix CentOS 7 OpenSCAP files on "ceos_client"
    And I register "ceos_client" as traditional client
    And I run "rhn-actions-control --enable-all" on "ceos_client"

@proxy
  Scenario: Check connection from CentOS 7 traditional to proxy
    Given I am on the Systems overview page of this "ceos_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of traditional CentOS 7
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos_client" hostname

  Scenario: Re-subscribe the CentOS traditional client to a base channel
    Given I am on the Systems overview page of this "ceos_client"
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

  Scenario: Schedule an OpenSCAP audit job for the CentOS traditional client
    Given I am on the Systems overview page of this "ceos_client"
    When I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    And I enter "--profile standard" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-rhel7-xccdf.xml" as "path"
    And I click on "Schedule"
    And I run "rhn_check -vvv" on "ceos_client"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait at most 500 seconds until event "OpenSCAP xccdf scanning" is completed

  Scenario: Check the results of the OpenSCAP scan on the CentOS traditional client
    Given I am on the Systems overview page of this "ceos_client"
    When I follow "Audit" in the content area
    And I follow "xccdf_org.open-scap_testresult_standard"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "RHEL-7" text
    And I should see a "XCCDF Rule Results" text
    When I enter "pass" as the filtered XCCDF result type
    And I click on the filter button
    Then I should see a "ensure_redhat_gpgkey_installed" link

  Scenario: Schedule some actions on the CentOS 7 traditional client
    When I authenticate to XML-RPC
    And I refresh the packages on "ceos_client" through XML-RPC
    And I run a script on "ceos_client" through XML-RPC
    And I reboot "ceos_client" through XML-RPC
    And I unauthenticate from XML-RPC

  Scenario: Cleanup: delete the CentOS 7 traditional client
    Given I am on the Systems overview page of this "ceos_client"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted." text
    Then "ceos_client" should not be registered

  Scenario: Cleanup: delete the installed rpms on CentOS 7 traditional client
    When I remove the traditional stack utils from "ceos_client"
    And I remove OpenSCAP dependencies from "ceos_client"
    And I disable SUSE Manager tools repositories on "ceos_client"
    And I disable repository "CentOS-Base" on this "ceos_client"

  Scenario: Cleanup: bootstrap a CentOS minion after traditional client tests
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "ceos_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I am on the System Overview page
    And I wait until I see the name of "ceos_minion", refreshing the page
    And I wait until onboarding is completed for "ceos_minion"

  Scenario: Cleanup: re-subscribe the new CentOS minion to a base channel
    Given I am on the Systems overview page of this "ceos_minion"
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
