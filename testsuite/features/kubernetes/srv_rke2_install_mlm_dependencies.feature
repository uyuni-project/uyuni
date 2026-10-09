# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rke2
@no_user_creation
Feature: Install MLM dependencies on RKE2

  Scenario: Create server namespace
    When I create namespace "$SERVER_NAMESPACE" on "server"

@create_spacewalk_pv
  Scenario: Create the spacewalk PV
    When I create a persistent volume defined in"$SPACEWALK_PV_FILE" on "server"

@create_pgsql_pv
  Scenario: Create the PostgreSQL PV
    When I create a persistent volume defined in"$PGSQL_PV_FILE" on "server"

@skip_if_external_cluster
  Scenario: Install Helm
    When I install Helm on "server"

  Scenario: Install cert-manager and trust-manager
    When I install and wait for cert-manager on "server"
    And I install and wait for trust-manager on "server"

  ## Install Traefik
  Scenario: Install Traefik
    When I install Traefik on "server"

  @default_local_path_class
  Scenario: Apply local path provisioner
    When I configure the local path provisioner on "server"

  ## Set up secret with scc credentials
  @scc_credentials
  Scenario: Set up SCC credentials
    When I set up the Kubernetes SCC credentials on the server
