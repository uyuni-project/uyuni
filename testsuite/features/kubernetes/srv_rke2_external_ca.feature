# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Verifies that MLM supports replacing the self-signed CA with an externally
# provided one on both the server and proxy RKE2 clusters.

@rke2
Feature: RKE2 External CA Replacement

  Scenario: Pre-requisite: Back up CA certificates before external CA replacement
    Given The Kubernetes cluster is ready on "server"
    And I back up the CA certificates on the server and proxy

  Scenario: Replace self-signed CA with an external CA
    When I generate an external CA on "server"
    And I replace the uyuni-ca secret with the external CA on "server"
    And I delete the leaf certificate secrets on "server"
    Then the "uyuni-cert" secret on "server" should be re-issued within 10 minutes
    And the "db-cert" secret on "server" should be re-issued within 5 minutes
    And the "uyuni-cert" certificate on "server" should be signed by the external CA
    And the "db-cert" certificate on "server" should be signed by the external CA

  Scenario: Verify server deployments recover after CA replacement
    Then the "db" deployment on "server" should become ready within 5 minutes
    And the "uyuni" deployment on "server" should become ready within 15 minutes

  Scenario: Inject external CA into proxy cluster
    Given The Kubernetes cluster is ready on "proxy"
    When I re-generate the proxy certificate on the server using the external CA
    And I transfer the proxy certificate from the server to "proxy"
    And I update the uyuni-ca configmap on "proxy" with the external CA
    Then the "proxy-cert" certificate on "proxy" should be signed by the external CA

  Scenario: Verify proxy deployments recover after CA replacement
    Then the "uyuni-proxy" deployment on "proxy" should become ready within 10 minutes
    And the "uyuni-proxy-tftp" deployment on "proxy" should become ready within 10 minutes

  Scenario: Cleanup: Restore original CA certificates after external CA tests
    When I restore the original CA certificates on the server and proxy
