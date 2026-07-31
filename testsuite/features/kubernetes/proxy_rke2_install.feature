# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#

@rke2
Feature: RKE2 Proxy Deployment
  In order to use a proxy deployed on RKE2
  As the system administrator
  I want to ensure the proxy is correctly initialized

  Scenario: Ensure proxy cluster health
    When The Kubernetes cluster is ready on "proxy"
    Then the "uyuni-proxy" deployment on "proxy" in the namespace "uyuni" should become ready within 15 minutes
    And the "uyuni-proxy-tftp" deployment on "proxy" in the namespace "uyuni" should become ready within 15 minutes
