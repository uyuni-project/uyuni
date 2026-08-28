# Copyright (c) 2026 Akash Kumar <meakash7902@gmail.com>
# Licensed under the terms of the MIT license.
#

@rke2
@long_running
Feature: RKE2 storage benchmark
  In order to compare storage backends for Uyuni on Kubernetes
  As the system administrator
  I want to run a real Uyuni repository synchronization workload

  Scenario: Run a local file repository synchronization benchmark
    Given the reposync benchmark source repository is mounted in the server pod
    When I create a unique reposync benchmark channel
    And I run the reposync benchmark for the mounted source repository
    Then the reposync benchmark should finish successfully
