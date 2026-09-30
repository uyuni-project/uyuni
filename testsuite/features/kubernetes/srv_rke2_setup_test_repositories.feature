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

  Scenario: Install minima in the server
    When I download and unzip minima on "server"

  Scenario: Synchronize the test repositories
    When I sync minima with data "$MINIMA_CONFIG_RPM" on "server"
    And I sync minima with data "$MINIMA_CONFIG_RH" on "server"
  
  Scenario: Create pillar top sls to assign salt bundle config
    When I configure the salt bundle pillar on the RKE2 server
