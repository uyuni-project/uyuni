# Copyright (c) 2025 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_salt
@skip_if_github_validation
Feature: Health Check tool based on a supportconfig
  A supportconfig generated from the Uyuni server
  should be able to be parsed by Health Check tool.

  Scenario: A supportconfig is taken from the server
    When I generate a supportconfig for the server
    Then I obtain and extract the supportconfig from the server

  Scenario: Execute health check tool with server supportconfig
    When I run "mgr-health-check -v -s /root/server-supportconfig/uyuni-server-supportconfig/ start" on "localhost"
    Then I run "test $(podman ps | grep health-check | wc -l) == 4" on "localhost"

  Scenario: I wait until health-check is ready
    When I run "curl localhost:3000/api/health -o /dev/null" on "localhost" with timeout at most 10 seconds

  Scenario: Health Check containers are healthy and running
    When I run "curl -s localhost:9000 -o /dev/null" on "localhost"
    Then I run "curl -s localhost:3100 -o /dev/null" on "localhost"
    And I run "curl -s localhost:9081 -o /dev/null" on "localhost"
    And I run "curl -s localhost:3000 -o /dev/null" on "localhost"

  Scenario: Health Check containers are exposing metrics
    When I run "curl -s localhost:9000/metrics.json | python3 -c 'import sys, json; print(json.load(sys.stdin).keys())'" on "localhost"

  Scenario: Cleanup: Remove health check tool
    When I run "mgr-health-check stop" on "localhost"
    Then I run "test $(podman ps | grep health-check | wc -l) == 0" on "localhost"
    And I run "rm /root/server-supportconfig* -rf" on "localhost"
