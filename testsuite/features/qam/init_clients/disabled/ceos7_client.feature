# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

@ceos7_client
Feature: Be able to register a CentOS 7 traditional client and do some basic operations on it

  Scenario: Prepare a CentOS 7 traditional client
    Given I am authorized
    And I install package "hwdata m2crypto wget" on this "ceos7_client"
    And I install package "rhn-client-tools rhn-check rhn-setup rhnsd osad rhncfg-actions" on this "ceos7_client"
    And I install package "spacewalk-oscap scap-security-guide" on this "ceos7_client"
    And I register "ceos7_client" as traditional client with activation key "1-ceos7_client_key"
    And I run "mgr-actions-control --enable-all" on "ceos7_client"
    And I wait until onboarding is completed for "ceos7_client"

  @proxy
  Scenario: Check connection from CentOS 7 traditional to proxy
    Given I am on the Systems overview page of this "ceos7_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" hostname

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
    Given I am authorized as "admin" with password "admin"
    When I authenticate to XML-RPC
    And I refresh the packages on "ceos7_client" through XML-RPC
    And I run a script on "ceos7_client" through XML-RPC
    And I reboot "ceos7_client" through XML-RPC
    And I unauthenticate from XML-RPC
