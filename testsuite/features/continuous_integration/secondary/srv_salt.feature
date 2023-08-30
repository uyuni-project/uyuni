# Copyright (c) 2015-2018 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_salt
Feature: Salt is configured and running
  In order to operate Uyuni based on Salt
  I want to use general Salt functionality and system registration

  Scenario: salt-api is properly configured
    When I get the contents of the remote file "/etc/salt/master.d/susemanager.conf"
    Then it should contain a "rest_cherrypy:" text
    And it should contain a "port: 9080" text
    And it should contain a "external_auth:" text

  Scenario: salt-master and salt-api are listening
    Then salt-api should be listening on local port 9080
    And salt-master should be listening on public port 4505
    And salt-master should be listening on public port 4506

  Scenario: There are no top.sls files in certain folders
    When I run "ls /srv/susemanager/salt/top.sls" on "server" without error control
    Then the command should fail
    When I run "ls /srv/susemanager/salt/top.sls" on "server" without error control
    Then the command should fail
    When I run "ls /srv/susemanager/pillar/top.sls" on "server" without error control
    Then the command should fail
    When I run "ls /usr/share/susemanager/salt/top.sls" on "server" without error control
    Then the command should fail
    When I run "ls /usr/share/susemanager/pillar/top.sls" on "server" without error control
    Then the command should fail
