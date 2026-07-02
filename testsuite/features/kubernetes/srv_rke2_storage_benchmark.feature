# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#

@rke2
@scope_storage_benchmark
@benchmark
Feature: RKE2 storage benchmark
  In order to compare storage backends for Uyuni on Kubernetes
  As the system administrator
  I want to run a real Uyuni repository synchronization workload

  Scenario: Run a local file repository synchronization benchmark
    Given The Kubernetes cluster is ready on "server"
    And the reposync benchmark source repository is mounted in the server pod
    When I create a unique reposync benchmark channel
    And I run the reposync benchmark for the mounted source repository
    Then the reposync benchmark should finish successfully
