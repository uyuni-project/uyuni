# Copyright (c) 2019-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Sanity checks
  In order to use the product
  I want to be sure to use a sane environment

  Scenario: The server is healthy
    Then "server" should have a FQDN

  Scenario: The traditional client is healthy
    Then "sle_client" should have a FQDN
    And "sle_client" should communicate with the server

  Scenario: The minion is healthy
    Then "sle_minion" should have a FQDN
    And "sle_minion" should communicate with the server

@ssh_minion
  Scenario: The SSH minion is healthy
    Then "ssh_minion" should have a FQDN
    And "ssh_minion" should communicate with the server

@proxy
  Scenario: The proxy is healthy
    Then "proxy" should have a FQDN
    And "proxy" should communicate with the server

@centos_minion
  Scenario: The Centos minion is healthy
    Then "ceos_ssh_minion" should have a FQDN
    And "ceos_ssh_minion" should communicate with the server

@ubuntu_minion
  Scenario: The Ubuntu minion is healthy
    Then "ubuntu_ssh_minion" should have a FQDN
    And "ubuntu_ssh_minion" should communicate with the server

  Scenario: The external resources can be reached
    Then it should be possible to reach the test packages
    And it should be possible to reach the build sources
    And it should be possible to reach the container profiles
    And it should be possible to reach the test suite profiles
    And it should be possible to reach the portus registry
    And it should be possible to reach the other registry
