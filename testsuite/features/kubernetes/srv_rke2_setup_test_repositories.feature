# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Download the test repositories into a MLM/Uyuni server installed on RKE2
# and apply the server configuration needed by the test suite.

@rke2
@no_user_creation
Feature: Set up test repositories and server configuration on RKE2

  Scenario: Check the RKE2 configuration
    When the environment variable "SERVER_NAMESPACE" is set on "server"
    And the environment variable "MINIMA_CONFIG_RPM" is set on "server"
    And the environment variable "MINIMA_CONFIG_RH" is set on "server"

  Scenario: Install minima in the server pod
    When I run "mgrctl exec 'curl --output-dir /root -OL https://github.com/uyuni-project/minima/releases/download/v0.4/minima-linux-amd64.tar.gz'" on "server" outside the container
    And I run "mgrctl exec 'tar xf /root/minima-linux-amd64.tar.gz -C /usr/bin'" on "server" outside the container

  Scenario: Synchronize the RPM updates test repository
    When I run "MINIMA_CONFIG=$MINIMA_CONFIG_RPM mgrctl exec -e MINIMA_CONFIG minima sync" on "server" outside the container

  Scenario: Synchronize the AppStream test repository
    When I run "MINIMA_CONFIG=$MINIMA_CONFIG_RH mgrctl exec -e MINIMA_CONFIG minima sync" on "server" outside the container
  
  Scenario: Create pillar top sls to assign salt bundle config
    When I configure the salt bundle pillar on the RKE2 server
