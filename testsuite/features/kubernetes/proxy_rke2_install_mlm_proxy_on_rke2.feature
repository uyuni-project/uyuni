# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rke2
@no_user_creation
Feature: Install MLM proxy on RKE2

  Scenario: Create helm chart directory on proxy
    When I create the Helm chart directory on "proxy"

  Scenario: Update OCI app version for proxy
    When I update the OCI Helm chart app version on "proxy"

  Scenario: Build helm dependencies on proxy
    When I build the Helm chart dependencies on "proxy"

  Scenario: Copy and uncompress proxy config tarball
    When I unpack the MLM proxy configuration for Helm

  Scenario: Install uyuni proxy on Kubernetes
    When I install the MLM proxy on RKE2
