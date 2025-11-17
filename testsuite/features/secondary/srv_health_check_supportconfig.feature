# Copyright (c) 2025 SUSE LLC
# SPDX-License-Identifier: MIT

@scope_salt
@skip_if_github_validation
Feature: Health Check tool based on a supportconfig
  A supportconfig generated from the Uyuni server
  should be able to be parsed by Health Check tool.

  Scenario: A supportconfig is taken from the server
    When I generate a supportconfig for the server
    Then I obtain and extract the supportconfig from the server

  Scenario: Execute Health Check tool with server supportconfig
    When I start the health check tool with supportconfig "/root/server-supportconfig/uyuni-server-supportconfig/" on "localhost"
    Then I check that the health check tool is running on "localhost"

  Scenario: Health Check containers are healthy and running
    When I wait until port "9000" is listening on "localhost" host
    Then I wait until port "3100" is listening on "localhost" host
    And I wait until port "9081" is listening on "localhost" host
    And I wait until port "3000" is listening on "localhost" host

  Scenario: Health Check containers are exposing metrics
    Then I check that the health check tool exposes metrics on "localhost"

  Scenario: I can stop the Health Check tool
    When I stop health check tool on "localhost"
    Then I check that the health check tool is not running on "localhost"
    And I remove test supportconfig on "localhost"
