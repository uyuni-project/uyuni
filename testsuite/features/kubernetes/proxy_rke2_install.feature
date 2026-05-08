# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#

@rke2

Feature: RKE2 Proxy Deployment
  In order to use a proxy deployed on RKE2
  As the system administrator
  I want to ensure the proxy is correctly initialized

  Scenario: Ensure proxy cluster health
    Given The Kubernetes cluster is ready on "proxy"
    When I wait until the "uyuni-proxy" deployment on "proxy" becomes ready within 10 minutes
    Then the "uyuni-proxy-tftp" deployment on "proxy" should become ready within 10 minutes
