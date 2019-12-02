# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

@ceos7_client
Feature: Be able to register a CentOS 7 traditional client and do some basic operations on it

  Scenario: Clean up sumaform leftovers on a CentOS 7 traditional client
    When I perform a full salt minion cleanup on "ceos7_client"

  Scenario: Prepare a CentOS 7 traditional client
    When I bootstrap traditional client "ceos7_client" using bootstrap script with activation key "1-ceos7_client_key" from the proxy
    And I install the traditional stack utils on "ceos7_client"
    And I install OpenSCAP centos dependencies on "ceos7_client"
    And I run "mgr-actions-control --enable-all" on "ceos7_client"
    And I wait until onboarding is completed for "ceos7_client"

  @proxy
  Scenario: Check connection from CentOS 7 traditional to proxy
    Given I am on the Systems overview page of this "ceos7_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

  @proxy
  Scenario: Check registration on proxy of traditional CentOS 7
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos7_client" hostname

  Scenario: Schedule an OpenSCAP audit job for the CentOS 7 traditional client
    Given I am on the Systems overview page of this "ceos7_client"
    When I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    And I enter "--profile standard" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-centos7-xccdf.xml" as "path"
    And I click on "Schedule"
    And I run "rhn_check -vvv" on "ceos7_client"
    Then I should see a "XCCDF scan has been scheduled" text
    And I wait until event "OpenSCAP xccdf scanning" is completed

  Scenario: Check the results of the OpenSCAP scan on the CentOS 7 traditional client
    Given I am on the Systems overview page of this "ceos7_client"
    When I follow "Audit" in the content area
    And I follow "xccdf_org.open-scap_testresult_standard"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "RHEL-7" text
    And I should see a "XCCDF Rule Results" text
    And I should see a "pass" text
    And I should see a "service_" link

  Scenario: Schedule some actions on the CentOS 7 traditional client
    Given I am authorized with the feature's user
    When I authenticate to XML-RPC
    And I refresh the packages on "ceos7_client" through XML-RPC
    And I run a script on "ceos7_client" through XML-RPC
    And I reboot "ceos7_client" through XML-RPC
    And I unauthenticate from XML-RPC
