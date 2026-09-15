# Copyright (c) 2026 SUSE LLC
# SPDX-License-Identifier: MIT

@rke2
@no_user_creation
Feature: Install MLM on RKE2

  Scenario: Update OCI app version
    When I update the OCI Helm chart app version on "server"

  @install_mlm_on_rke2
  Scenario: Install Uyuni
    When I build the Helm chart dependencies on "server"
    And I install the MLM server on RKE2
