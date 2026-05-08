# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#

@rke2

Feature: RKE2 Server Deployment
  In order to manage systems using a Kubernetes installation
  As the system administrator
  I want to ensure the server is correctly deployed on RKE2

  Scenario: Ensure server cluster health and persistence
    Given The first-time setup job is successful
    Then the setup marker file should exist on "server"
    And the "db" deployment on "server" should become ready within 5 minutes
    And the "uyuni" deployment on "server" should become ready within 15 minutes
