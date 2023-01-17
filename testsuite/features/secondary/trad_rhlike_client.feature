# Copyright (c) 2017-2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
# 1) delete Red Hat-like minion and register a Red Hat-like traditional client
# 2) run an OpenSCAP audit
# 3) refresh packages
# 4) run a script
# 5) reboot
# 6) delete the traditional client and register as Red Hat-like minion

@scope_traditional_client
@scope_res
@rhlike_minion
Feature: Be able to register a Red Hat-like traditional client and do some basic operations on it

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Delete the Red Hat-like minion before traditional client tests
    When I am on the Systems overview page of this "rhlike_minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "rhlike_minion" should not be registered

  Scenario: Prepare the Red Hat-like traditional client
    When I enable repository "CentOS-Base" on this "rhlike_client"
    And I enable the repositories "tools_update_repo tools_pool_repo" on this "rhlike_client"
    And I refresh the packages list via package manager on "rhlike_client"
    And I install the traditional stack utils on "rhlike_client"
    And I install OpenSCAP dependencies on "rhlike_client"
    And I fix CentOS 7 OpenSCAP files on "rhlike_client"
    And I register "rhlike_client" as traditional client with activation key "1-RH-LIKE-KEY"
    And I run "rhn-actions-control --enable-all" on "rhlike_client"

@proxy
  Scenario: Check connection from Red Hat-like traditional to proxy
    Given I am on the Systems overview page of this "rhlike_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of traditional Red Hat-like
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "rhlike_client" hostname

  Scenario: Re-subscribe the Red Hat-like traditional client to a base channel
    Given I am on the Systems overview page of this "rhlike_client"
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

  Scenario: Schedule an OpenSCAP audit job for the Red Hat-like traditional client
    Given I am on the Systems overview page of this "rhlike_client"
    When I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    And I wait at most 30 seconds until I do not see "This system does not yet have OpenSCAP scan capability." text, refreshing the page
    And I enter "--profile standard" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-centos7-xccdf.xml" as "path"
    And I click on "Schedule"
    And I run "rhn_check -vvv" on "rhlike_client"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait at most 500 seconds until event "OpenSCAP xccdf scanning" is completed

  Scenario: Check the results of the OpenSCAP scan on the Red Hat-like traditional client
    Given I am on the Systems overview page of this "rhlike_client"
    When I follow "Audit" in the content area
    And I follow "xccdf_org.open-scap_testresult_xccdf_org.ssgproject.content_profile_standard"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "RHEL-7" text
    And I should see a "XCCDF Rule Results" text
    When I enter "pass" as the filtered XCCDF result type
    And I click on the filter button
    Then I should see a "ensure_redhat_gpgkey_installed" link

  Scenario: Schedule some actions on the Red Hat-like traditional client
    When I am logged in API as user "admin" and password "admin"
    And I refresh the packages on traditional "rhlike_client" through API
    And I run a script on traditional "rhlike_client" through API
    And I reboot traditional "rhlike_client" through API
    And I logout from API

  Scenario: Cleanup: delete the Red Hat-like traditional client
    Given I am on the Systems overview page of this "rhlike_client"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted." text
    Then "rhlike_client" should not be registered

  Scenario: Cleanup: delete the installed rpms on Red Hat-like traditional client
    When I remove the traditional stack utils from "rhlike_client"
    And I remove OpenSCAP dependencies from "rhlike_client"
    And I disable the repositories "tools_update_repo tools_pool_repo" on this "rhlike_client"
    And I disable repository "CentOS-Base" on this "rhlike_client"

  Scenario: Cleanup: bootstrap a Red Hat-like minion after traditional client tests
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "rhlike_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I follow the left menu "Systems > Overview"
    And I wait until I see the name of "rhlike_minion", refreshing the page
    And I wait until onboarding is completed for "rhlike_minion"

  Scenario: Cleanup: re-subscribe the new Red Hat-like minion to a base channel
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
