# Copyright (c) 2026 Akash Kumar <meakash7902@gmail.com>
# Licensed under the terms of the MIT license.

@rke2
Feature: Salt package download benchmark
  In order to compare storage backends for Uyuni on Kubernetes
  As the system administrator
  I want configured Salt minions to download every package of the benchmark channel

  Scenario: Download every benchmark channel package on every configured minion
    Given the Salt package download benchmark inputs are valid
    And the benchmark minions are subscribed to the benchmark channel
    And the initial benchmark channel package snapshot is valid
    And the benchmark minions are ready for the benchmark channel
    When I clear RPM payload caches on the benchmark minions outside the measurement
    And I execute and record the channel package downloads
    Then the package download result report should exist
    And every configured minion should have downloaded every channel package
