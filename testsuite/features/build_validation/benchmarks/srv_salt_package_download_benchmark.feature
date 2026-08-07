# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@benchmark @long_running
Feature: Salt channel package download on configured minions
  In order to measure package storage performance
  As an operator of an existing Uyuni environment
  I want configured Salt minions to download every package in a configured channel

  Scenario: Download every configured channel package on every configured minion
    Given the Salt package download benchmark inputs are valid
    And the initial configured channel package snapshot is valid
    And a ready server pod is reachable from the benchmark controller
    And the benchmark minions are ready for the configured channel
    When I clear RPM payload caches on the benchmark minions outside the measurement
    And I execute and record the channel package downloads
    Then the package download result report should exist
    And every configured minion should have downloaded every channel package
