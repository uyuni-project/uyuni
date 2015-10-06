# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check if SaltStack is configured and running
  In order to operate SUSE Manager based on SaltStack
  I want to verify general salt functionality and system registration

  Scenario: Check if salt-api is properly configured
    When I get the contents of the remote file "/etc/salt/master.d/susemanager.conf"
    Then it should contain a "rest_cherrypy:" text
    And it should contain a "port: 9080" text
    And it should contain a "external_auth:" text

  Scenario: Check if salt-master and salt-api are listening
    Then salt-api should be listening on local port 9080
    And salt-master should be listening on public port 4505
    And salt-master should be listening on public port 4506

  Scenario: Check if the minion is running
   Given the Salt Minion is configured
    When I remove possible Salt Master key "/etc/salt/pki/minion/minion_master.pub"
    And I restart salt-minion
    And I wait for "2" seconds
    Then the Salt Minion should be running

  Scenario: Check if the minion key can be accepted
    When I list unaccepted keys at Salt Master
    Then the list of the keys should contain this client's hostname
    When I accept all Salt unaccepted keys
    And I list accepted keys at Salt Master
    Then the list of the keys should contain this client's hostname

  Scenario: Check if the minion communicates with the master and verify registration
    When I wait for "8" seconds
    And I get OS information of the Minion from the Master
    Then it should contain a "SLES" text
    And I should see this client in spacewalk

  Scenario: Check if the minion key can be deleted
    When I delete the key of this client
    # This is to clean up for the following tests
    And I delete the registered minion
    And I restart salt-minion
    And I wait for "2" seconds
    And I list unaccepted keys at Salt Master
    Then the list of the keys should contain this client's hostname
