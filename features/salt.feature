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
