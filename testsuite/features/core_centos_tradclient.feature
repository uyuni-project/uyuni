# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.
#
# 1) register a CentOS 7 traditional client
# 2) run an OpenSCAP audit
# 3) refresh packages
# 4) run a script
# 5) reboot
# 6) delete the traditional client

Feature: Be able to register a CentOS 7 traditional client and do some basic operations on it

@centosminion
  Scenario: Prepare a CentOS 7 traditional client
     Given I am authorized
     When I run "sed s/enabled=.*/enabled=1/g /etc/yum.repos.d/Devel_Galaxy_Manager_Head_RES-Manager-Tools-7-x86_64.repo -i" on "ceos-traditional-client" without error control
     And  I run "sed s/enabled=.*/enabled=1/g /etc/yum.repos.d/SLE-Manager-Tools-RES-7-x86_64.repo -i" on "ceos-traditional-client" without error control
     And  I run "sed s/enabled=.*/enabled=1/g /etc/yum.repos.d/CentOS-Base.repo -i" on "ceos-traditional-client" without error control
     And  I run "yum repolist" on "ceos-traditional-client"
     And  I run "yum install -y --nogpgcheck rhn-client-tools rhn-check rhn-setup rhnsd hwdata m2crypto wget osad rhncfg-actions" on "ceos-traditional-client"
     And  I run "yum install -y --nogpgcheck spacewalk-oscap scap-security-guide" on "ceos-traditional-client"
     And  I register "ceos-traditional-client" as traditional client
     And  I run "rhn-actions-control --enable-all" on "ceos-traditional-client"

@proxy
@centosminion
  Scenario: Check connection from CentOS 7 traditional to proxy
    Given I am on the Systems overview page of this "ceos-traditional-client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" hostname

@proxy
@centosminion
  Scenario: Check registration on proxy of traditional CentOS 7
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos-traditional-client" hostname

@centosminion
  Scenario: Schedule an OpenSCAP audit job for the CentOS traditional client
    Given I am on the Systems overview page of this "ceos-traditional-client"
    When I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    And I enter "--profile common" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-centos7-xccdf.xml" as "path"
    And I click on "Schedule"
    And I run "rhn_check -vvv" on "ceos-traditional-client"
    Then I should see a "XCCDF scan has been scheduled" text

@centosminion
  Scenario: Check the results of the OpenSCAP scan on the CentOS traditional client
    Given I am on the Systems overview page of this "ceos-traditional-client"
    When I follow "Audit" in the content area
    And I follow "xccdf_org.open-scap_testresult_common"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "RHEL-7" text
    And I should see a "XCCDF Rule Results" text
    And I should see a "pass" text or "notapplicable" text
    And I should see a "service_" link

@centosminion
  Scenario: Schedule some actions on the CentOS 7 traditional client
     Given I am authorized as "admin" with password "admin"
     When I authenticate to XML-RPC
     And I refresh the packages on "ceos-traditional-client" through XML-RPC
     And I run a script on "ceos-traditional-client" through XML-RPC
     And I reboot "ceos-traditional-client" through XML-RPC
     And I unauthenticate from XML-RPC

@centosminion
  Scenario: Delete the CentOS 7 traditional client
    Given I am on the Systems overview page of this "ceos-traditional-client"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    And I wait until I see "has been deleted." text
