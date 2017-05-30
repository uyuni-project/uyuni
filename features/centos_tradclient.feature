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
     And  I register the centos7 as tradclient
     And  I run "rhn-actions-control --enable-all" on "ceos-minion"

  Scenario: Schedule some actions for centos7
     Given I am authorized as "admin" with password "admin"
     And execute some tests for centos_trad_client

  Scenario: run openscap audit for centos7 trad-client.
     Given I am authorized as "admin" with password "admin"
     And execute test for openscap tradclient

  Scenario: Delete Trad-client or migrated minion
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I wait until i see "has been deleted." text
  # FIXME: add a reregistration so we can migrate from trad_client to salt 
  # this depend on actual bug atm
