# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check if SaltStack is configured and running
  In order to operate SUSE Manager based on SaltStack
  I want to check if SaltStack Master, Minion and API are configured and running

  Scenario: Check if the SaltStack API is properly configured
    When I get the contents of the remote file "/etc/salt/master.d/susemanager.conf"
    Then it should contain a "rest_cherrypy:" text
    And it should contain a "port: 9080" text
    And it should contain a "external_auth:" text

  Scenario: Check if SaltStack Master and API are listening
    Then salt-api should be listening on local port 9080
    And salt-master should be listening on public port 4505
    And salt-master should be listening on public port 4506

  Scenario: Check if the SaltStack Minion is running
   Given the Salt Minion is configured
    When I remove possible Salt Master key "/etc/salt/pki/minion/minion_master.pub"
    And I restart salt-minion
    And I wait for "2" seconds
    Then the Salt Minion should be running

  Scenario: Check if the SaltStack Minion can be registered
    When I list unaccepted keys at Salt Master
    Then the list of the keys should contain this client's hostname
    When I accept all Salt unaccepted keys
    And I list accepted keys at Salt Master
    Then the list of the keys should contain this client's hostname

  Scenario: Check if SaltStack Minion communicates with the Master
    When I wait for "8" seconds
    And I get OS information of the Minion from the Master
    Then it should contain a "SLES" text
    And I should see this client in spacewalk

  Scenario: Check if the Minion key can be deleted
    When I delete the key of this client
    # This is to clean up for the following tests
    And I delete the registered minion
    And I restart salt-minion
    And I wait for "2" seconds
    And I list unaccepted keys at Salt Master
    Then the list of the keys should contain this client's hostname
