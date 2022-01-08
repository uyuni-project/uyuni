# Copyright (c) 2019-2022 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Sanity checks
  In order to use the product
  I want to be sure to use a sane environment

  Scenario: The server is healthy
    Then "server" should have a FQDN
    And reverse resolution should work for "server"
    And the clock from "server" should be exact
    And service "apache2" is enabled on "server"
    And service "apache2" is active on "server"
    And service "cobblerd" is enabled on "server"
    And service "cobblerd" is active on "server"
    And service "jabberd" is enabled on "server"
    And service "jabberd" is active on "server"
    And service "osa-dispatcher" is enabled on "server"
    And service "osa-dispatcher" is active on "server"
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

@sle_client
  Scenario: The traditional client is healthy
    Then "sle_client" should have a FQDN
    And reverse resolution should work for "sle_client"
    And "sle_client" should communicate with the server using public interface
    And the clock from "sle_client" should be exact

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

@centos_minion
  Scenario: The CentOS minion is healthy
    Then "ceos_minion" should have a FQDN
    And reverse resolution should work for "ceos_minion"
    And "ceos_minion" should communicate with the server using public interface
    And the clock from "ceos_minion" should be exact

@ubuntu_minion
  Scenario: The Ubuntu minion is healthy
    Then "ubuntu_minion" should have a FQDN
    And reverse resolution should work for "ubuntu_minion"
    And "ubuntu_minion" should communicate with the server using public interface
    And the clock from "ubuntu_minion" should be exact

@virthost_kvm
  Scenario: The KVM host is healthy
    Then "kvm_server" should have a FQDN
    And reverse resolution should work for "kvm_server"
    And "kvm_server" should communicate with the server using public interface
    And the clock from "kvm_server" should be exact

@virthost_xen
  Scenario: The Xen host is healthy
    Then "xen_server" should have a FQDN
    And reverse resolution should work for "xen_server"
    And "xen_server" should communicate with the server using public interface
    And the clock from "xen_server" should be exact

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
