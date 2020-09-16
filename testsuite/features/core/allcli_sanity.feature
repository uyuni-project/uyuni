# Copyright (c) 2019-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Sanity checks
  In order to use the product
  I want to be sure to use a sane environment

  Scenario: The server is healthy
    Then "server" should have a FQDN
    And the clock from "server" should be exact

@sle_client
  Scenario: The traditional client is healthy
    Then "sle_client" should have a FQDN
    And "sle_client" should communicate with the server
    And the clock from "sle_client" should be exact

@sle_minion
  Scenario: The minion is healthy
    Then "sle_minion" should have a FQDN
    And "sle_minion" should communicate with the server
    And the clock from "sle_minion" should be exact

@buildhost
  Scenario: The build host is healthy
    Then "build_host" should have a FQDN
    And "build_host" should communicate with the server
    And the clock from "build_host" should be exact

@ssh_minion
  Scenario: The SSH minion is healthy
    Then "ssh_minion" should have a FQDN
    And "ssh_minion" should communicate with the server
    And the clock from "ssh_minion" should be exact

@proxy
  Scenario: The proxy is healthy
    Then "proxy" should have a FQDN
    And "proxy" should communicate with the server
    And the clock from "proxy" should be exact

@centos_minion
  Scenario: The CentOS minion is healthy
    Then "ceos_minion" should have a FQDN
    And "ceos_minion" should communicate with the server
    And the clock from "ceos_minion" should be exact

@ubuntu_minion
  Scenario: The Ubuntu minion is healthy
    Then "ubuntu_minion" should have a FQDN
    And "ubuntu_minion" should communicate with the server
    And the clock from "ubuntu_minion" should be exact

@virthost_kvm
  Scenario: The KVM host is healthy
    Then "kvm_server" should have a FQDN
    And "kvm_server" should communicate with the server
    And the clock from "kvm_server" should be exact

@virthost_xen
  Scenario: The Xen host is healthy
    Then "xen_server" should have a FQDN
    And "xen_server" should communicate with the server
    And the clock from "xen_server" should be exact

  Scenario: The external resources can be reached
    Then it should be possible to reach the test packages
    And it should be possible to reach the build sources
    And it should be possible to reach the container profiles
    And it should be possible to reach the test suite profiles
    And it should be possible to reach the portus registry
    And it should be possible to reach the other registry
