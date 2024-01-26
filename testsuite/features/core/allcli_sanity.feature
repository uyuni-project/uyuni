# Copyright (c) 2019-2022 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Sanity checks
  In order to use the product
  I want to be sure to use a sane environment

  Scenario: The server is healthy
    Then "server" should have a FQDN
    Then reverse resolution should work for "server"
    And the clock from "server" should be exact
    And service "apache2" is enabled on "server"
    And service "apache2" is active on "server"
    And service "cobblerd" is enabled on "server"
    And service "cobblerd" is active on "server"
    And service "rhn-search" is enabled on "server"
    And service "rhn-search" is active on "server"
    And service "salt-api" is enabled on "server"
    And service "salt-api" is active on "server"
    And service "salt-master" is enabled on "server"
    And service "salt-master" is active on "server"
    And service "taskomatic" is enabled on "server"
    And service "taskomatic" is active on "server"
    And socket "tftp" is enabled on "server"
    And socket "tftp" is active on "server"
    And service "tomcat" is enabled on "server"
    And service "tomcat" is active on "server"

@proxy
  Scenario: The proxy is healthy
    Then "proxy" should have a FQDN
    And reverse resolution should work for "proxy"
    And "proxy" should communicate with the server using public interface
    And the clock from "proxy" should be exact

@sle_minion
  Scenario: The minion is healthy
    Then "sle_minion" should have a FQDN
    And reverse resolution should work for "sle_minion"
    And "sle_minion" should communicate with the server using public interface
    And the clock from "sle_minion" should be exact

@buildhost
  Scenario: The build host is healthy
    Then "build_host" should have a FQDN
    And reverse resolution should work for "build_host"
    And "build_host" should communicate with the server using public interface
    And the clock from "build_host" should be exact

@ssh_minion
  Scenario: The SSH minion is healthy
    Then "ssh_minion" should have a FQDN
    And reverse resolution should work for "ssh_minion"
    And "ssh_minion" should communicate with the server using public interface
    And the clock from "ssh_minion" should be exact

@rhlike_minion
  Scenario: The Red Hat-like minion is healthy
    Then "rhlike_minion" should have a FQDN
    And reverse resolution should work for "rhlike_minion"
    And "rhlike_minion" should communicate with the server using public interface
    And the clock from "rhlike_minion" should be exact

@deblike_minion
  Scenario: The Debian-like minion is healthy
    Then "deblike_minion" should have a FQDN
    And reverse resolution should work for "deblike_minion"
    And "deblike_minion" should communicate with the server using public interface
    And the clock from "deblike_minion" should be exact

@virthost_kvm
  Scenario: The KVM host is healthy
    Then "kvm_server" should have a FQDN
    And reverse resolution should work for "kvm_server"
    And "kvm_server" should communicate with the server using public interface
    And the clock from "kvm_server" should be exact

@skip_if_cloud
  Scenario: The external resources can be reached
    Then it should be possible to reach the test packages
    And it should be possible to reach the build sources
    And it should be possible to reach the Docker profiles

@server_http_proxy
  Scenario: The HTTP proxy is working
    Then it should be possible to use the HTTP proxy

@auth_registry
  Scenario: The registry with authentication is healthy
    Then it should be possible to reach the authenticated registry

@no_auth_registry
  Scenario: The registry without authentication is healthy
    Then it should be possible to reach the not authenticated registry

@custom_download_endpoint
  Scenario: The custom download endpoint is working
    Then it should be possible to use the custom download endpoint
