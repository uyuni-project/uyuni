# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rke2
@no_user_creation
Feature: Configure RKE2 installation
  In order to install RKE2 on the server and proxy
  As the system administrator
  I want the RKE2 configuration to be available

  Scenario: Verify the server configuration
    Then the environment variable "RKE2_VERSION" is set on "server"
    And the environment variable "RKE2_INSTALL_METHOD" is set on "server"
    And file "/etc/rancher/rke2/config.yaml" should exist on "server"

  Scenario: Verify the proxy configuration
    Then the environment variable "RKE2_VERSION" is set on "proxy"
    And the environment variable "RKE2_INSTALL_METHOD" is set on "proxy"
    And file "/etc/rancher/rke2/config.yaml" should exist on "proxy"
