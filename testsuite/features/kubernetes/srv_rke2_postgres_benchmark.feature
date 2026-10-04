# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rke2
@long_running
Feature: RKE2 PostgreSQL database storage benchmark
  In order to compare storage backends for Uyuni on Kubernetes
  As the system administrator
  I want to run standard PostgreSQL pgbench database workloads

  Scenario: Run PostgreSQL database storage benchmark
    Given the database benchmark inputs are valid
    And the PostgreSQL database pod is ready in the uyuni namespace
    When I initialize the pgbench benchmark database schema
    And I run the read-write transaction benchmark
    And I run the read-only transaction benchmark
    Then the database benchmark result report should exist and be valid
    And I clean up the pgbench benchmark database
