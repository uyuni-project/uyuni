# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rke2
@no_user_creation
Feature: Install MLM dependencies on RKE2

  @skip_if_external_cluster
  Scenario: Install Helm
    When I install Helm on "proxy"

  Scenario: Install cert-manager and trust-manager
    When I install and wait for cert-manager on "proxy"
    And I install and wait for trust-manager on "proxy"

  ## Install Traefik
  Scenario: Install Traefik
    When I install Traefik on "proxy"

  @default_local_path_class
  Scenario: Apply local path provisioner
    When I configure the local path provisioner on "proxy"
