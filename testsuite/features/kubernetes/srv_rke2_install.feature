# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
@rke2
Feature: RKE2 Server Health
  In order to manage systems using a Kubernetes installation
  As the system administrator
  I want to ensure the server is correctly deployed on RKE2

  Scenario: Ensure the deployments are up
    Then the "uyuni" deployment on "server" in the namespace "uyuni" should become ready within 15 minutes
    And the "db" deployment on "server" in the namespace "uyuni" should become ready within 15 minutes
    And the "tftp" deployment on "server" in the namespace "uyuni" should become ready within 15 minutes
    And the "saline" deployment on "server" in the namespace "uyuni" should become ready within 15 minutes
    And the "hub-xmlrpc" deployment on "server" in the namespace "uyuni" should become ready within 15 minutes
    And the "coco-attestation" deployment on "server" in the namespace "uyuni" should become ready within 15 minutes

  Scenario: Ensure the pods are up
    Then the "uyuni" pod on "server" in the namespace "uyuni" should become ready within 15 minutes
    And the "db" pod on "server" in the namespace "uyuni" should become ready within 15 minutes
    And the "tftp" pod on "server" in the namespace "uyuni" should become ready within 15 minutes
    And the "saline" pod on "server" in the namespace "uyuni" should become ready within 15 minutes
    And the "hub-xmlrpc" pod on "server" in the namespace "uyuni" should become ready within 15 minutes
    And the "coco-attestation" pod on "server" in the namespace "uyuni" should become ready within 15 minutes

  Scenario: The setup marker exists
    Then the setup marker file should exist on "server"

    
