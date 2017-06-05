# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: register a traditional client centos7

  Scenario: Install the trad-client on centos7
     Given I am authorized
     And  I run "sed s/enabled=0/enabled=1/g /etc/yum.repos.d/Devel_Galaxy_Manager_Head_RES-Manager-Tools-7-x86_64.repo  -i" on "ceos-minion" without error control
     And  I run "sed s/enabled=0/enabled=1/g /etc/yum.repos.d/SLE-Manager-Tools-RES-7-x86_64.repo -i" on "ceos-minion" without error control
     And  I run "sed s/enabled=0/enabled=1/g /etc/yum.repos.d/CentOS-Base.repo -i" on "ceos-minion" without error control
     And  I run "yum repolist" on "ceos-minion"
     And  I run "yum install -y --nogpgcheck rhn-client-tools rhn-check rhn-setup rhnsd hwdata m2crypto wget osad rhncfg-actions" on "ceos-minion"
     And  I run "yum install -y --nogpgcheck spacewalk-oscap scap-security-guide" on "ceos-minion"
     And  I register the centos7 as tradclient
     And  I run "rhn-actions-control --enable-all" on "ceos-minion"
      
   Scenario: Schedule an openscap-audit job for centos tradclient
    Given I am on the Systems overview page of this "ceos-minion"
    And I follow "Audit" in the content area
    And I follow "Schedule" in the content area
    When I enter "--profile common" as "params"
    And I enter "/usr/share/xml/scap/ssg/content/ssg-centos7-xccdf.xml" as "path"
    And I click on "Schedule"
    And I run "rhn_check -vvv" on "ceos-minion"
    Then I should see a "XCCDF scan has been scheduled" text

  Scenario: Check results of the openscap centos
    Given I am on the Systems overview page of this "ceos-minion"
    And I follow "Audit" in the content area
    When I follow "xccdf_org.open-scap_testresult_RHEL6-Default"
    Then I should see a "Details of XCCDF Scan" text
    And I should see a "RHEL6-Default" text
    And I should see a "XCCDF Rule Results" text
    And I should see a "CCE-" text
    And I should see a "rule-" link

  Scenario: Schedule some actions for centos7
     Given I am authorized as "admin" with password "admin"
     And execute some tests for centos_trad_client
       
  Scenario: Delete Trad-client or migrated minion
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I wait until i see "has been deleted." text
