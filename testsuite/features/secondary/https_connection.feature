# Copyright (c) 2025 SUSE LLC
# SPDX-License-Identifier: MIT

@scope_visualization
Feature: Secure connection

  Scenario: Connecting to the server insecurely
    When I connect to the server insecurely
    Then the connection should redirect to the secured channel

  Scenario: Connecting to the server securely
    When I connect to the server securely
    Then the connection should be secured
    And the page title should contain "Sign In" text

  Scenario: Connecting to the server securely while using CA certificate file
    When I connect to the server securely while using CA certificate file
    Then the connection should be secured
    And the page title should contain "Sign In" text

  Scenario: Connecting to the server securely while using incorrect CA certificate file
    When I connect to the server securely while using incorrect certificate as a CA certificate file
    Then the secure connection should fail due to unverified certificate signature
