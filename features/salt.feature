# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check if SaltStack Master is configured and running
  In order to operate SUSE Manager
  As the admin user
  I want to check if SaltStack Master is installed and running

  Scenario: Check SaltStack Master is installed
    When I get a content of a file "/etc/salt/master"
    Then it should contain "rest_cherrypy:" text
    And it should contain "port: 9080" text
    And it should contain "external_auth:" text

  Scenario: Check SaltStack Master is properly configured
    When I issue command "ss -nta | grep 9080"
    Then it should contain "127.0.0.1:9080" text

  Scenario: Check SaltStack Minion is running
    When I issue local command "rcsalt-minion status | grep Active"
    Then it should contain "active (running) since" text

  Scenario: Check SaltStack Minion can be registered
    When I issue command "salt-key --list unaccepted"
    Then it should contain testsuite hostname
    When I issue command "yes | salt-key -A"
    And when I issue command "salt-key --list accepted"
    Then it should contain testsuite hostname

  Scenario: Check if SaltStack Minion communicates with the Master
    When I ping client machine from the Master
    Then it should contain testsuite hostname
    When I get OS information of the client machine from the Master
    Then it should contain "SLES" text

  Scenario: Cleaning up for the general testsuite
    When I delete key of the testsuite hostname
    And when I issue command "salt-key --list unaccepted"
    Then it should contain testsuite hostname
