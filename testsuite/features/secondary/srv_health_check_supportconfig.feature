# Copyright (c) 2025-2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_salt
@skip_if_github_validation
Feature: Health Check tool based on a supportconfig
  A supportconfig generated from the Uyuni server
  should be able to be parsed by Health Check tool.

  Scenario: A supportconfig is taken from the server
    When I generate a supportconfig for the server
    Then I obtain and extract the supportconfig from the server

  Scenario: Execute Health Check tool with server supportconfig
    When I start the health check tool with the extracted supportconfig on "localhost"
    Then the health check tool should be running on "localhost"

  Scenario: Health Check containers are healthy and running
    When I wait until port "9000" is listening on "localhost" host
    Then I wait until port "3100" is listening on "localhost" host
    And I wait until port "9081" is listening on "localhost" host
    And I wait until port "3000" is listening on "localhost" host

  Scenario: Health Check containers are exposing expected metrics
    Then the health check tool should expose the expected metrics on "localhost"

  Scenario: Health Check Grafana dashboard is accessible
    Then the health check Grafana dashboard should be accessible on "localhost"

  Scenario: I can stop the Health Check tool
    When I stop the health check tool on "localhost"
    Then the health check tool should not be running on "localhost"
    When I remove test supportconfig on "localhost"
