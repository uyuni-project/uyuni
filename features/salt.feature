# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Salt is configured and running
  In order to operate SUSE Manager based on Salt
  I want to verify general salt functionality and system registration

  Scenario: salt-api is properly configured
    When I get the contents of the remote file "/etc/salt/master.d/susemanager.conf"
    Then it should contain a "rest_cherrypy:" text
    And it should contain a "port: 9080" text
    And it should contain a "external_auth:" text

  Scenario: salt-master and salt-api are listening
    Then salt-api should be listening on local port 9080
    And salt-master should be listening on public port 4505
    And salt-master should be listening on public port 4506

  Scenario: The minion is running
   Given the Salt Minion is configured
    And I restart salt-minion
    Then the Salt Minion should be running

  Scenario: The minion key can be accepted
    When I list unaccepted keys at Salt Master
    Then the list of the keys should contain this client's hostname
    And I accept all Salt unaccepted keys
    And I list accepted keys at Salt Master
    Then the list of the keys should contain this client's hostname

  Scenario: The minion communicates with the master and can register
    # It takes a while before we can get the grains and registration is done
    Given that the master can reach this client
    When I get OS information of the Minion from the Master
    Then it should contain a "SLES" text
    And this client should appear in spacewalk

