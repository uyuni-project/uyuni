# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if:
# * there is no proxy ($proxy is nil)
#
# Check pod and containers of the proxy

@proxy
Feature: Check containerized proxy
  In order to use a containerized proxy with the server
  As the system administrator
  I want to check the status of the containerized proxy

  Scenario: Check pod and container statuses
    When I get the contents of the remote file "/tmp/podman-proxy.log"
    Then it should contain a "uyuni-proxy-test-status: Running" text
    And it should contain a "proxy-http-status: running" text
    And it should contain a "proxy-ssh-status: running" text
    And it should contain a "proxy-squid-status: running" text
    And it should contain a "proxy-salt-broker-status: running" text
    And it should contain a "proxy-tftpd-status: running" text
    And it should contain a "uyuni-proxy-test-containers: 6" text
