# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rke2
Feature: RKE2 TFTP container sanity checks on the server
  In order to provision systems via PXE boot
  As the system administrator
  I want to ensure the TFTP container is correctly deployed and serving files

  Scenario: TFTP deployment is ready and service has active endpoints
    Given The Kubernetes cluster is ready on "server"
    And the "tftp" deployment on "server" should become ready within 5 minutes
    Then the "tftp" service on "server" should have at least one active endpoint

  Scenario: TFTP serves a file placed in the boot root
    Given I create a sanity-check file in the TFTP boot root on "server"
    When I download the sanity-check file via TFTP from "server"
    Then the downloaded TFTP content should match the expected sanity-check content on "server"
    And I remove the sanity-check file from the TFTP boot root on "server"
