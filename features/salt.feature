# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check if SaltStack Master is configured and running
  In order to operate SUSE Manager
  As the admin user
  I want to check if SaltStack Master is installed and running

  Scenario: Check SaltStack Master is installed
    Given this client hostname
    When I get a content of a file "/etc/salt/master.d/susemanager.conf"
    Then it should contain "rest_cherrypy:" text
    And it should contain "port: 9080" text
    And it should contain "external_auth:" text

  Scenario: Check SaltStack Master is properly configured
    Then the Salt rest-api should be listening on local port 9080
    And the salt-master should be listening on public port 4505
    And the salt-master should be listening on public port 4506

  Scenario: Check SaltStack Minion is running
   Given the salt-minion is configured
    When I remove possible Salt Master key "/etc/salt/pki/minion/minion_master.pub"
    And I restart Salt Minion
    Then the Salt Minion should be running

  Scenario: Check SaltStack Minion can be registered
    Given this client hostname
    When I list unaccepted keys at Salt Master
    Then the list of the keys should contain this client hostname
    When I accept all Salt unaccepted keys
    When I list accepted keys at Salt Master
    Then the list of the keys should contain this client hostname

  Scenario: Check if SaltStack Minion communicates with the Master
    Given this client hostname
    Then the Salt Minion should be running
    When I get OS information of the client machine from the Master
    Then it should contain "SLES" text

  Scenario: Cleaning up for the general testsuite
    Given this client hostname
    When I delete key of this client
    When I list unaccepted keys at Salt Master
    Then it should contain testsuite hostname
