# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#

@rke2
@scope_storage_benchmark
@benchmark
Feature: RKE2 storage benchmark
  In order to compare storage backends for Uyuni on Kubernetes
  As the system administrator
  I want to run real-world repository metadata generation workloads

  Scenario: Run the repodata generation workload
    Given The Kubernetes cluster is ready on "server"
    And the Uyuni benchmark tool is available in the server pod
    When I prepare the repodata generation benchmark dataset
    And I run the repodata generation benchmark
    Then the repodata generation benchmark should finish successfully
