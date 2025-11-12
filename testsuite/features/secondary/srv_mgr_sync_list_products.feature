# Copyright (c) 2018-2025 SUSE LLC
# SPDX-License-Identifier: MIT

Feature: List available products
  In order to use software channels
  As root user
  I want to list available products from command line

@susemanager
  Scenario: List available products
    When I execute mgr-sync "list products" with user "admin" and password "admin"
    Then I should get "[ ] SUSE Linux Enterprise Desktop 15 SP3 x86_64"

@uyuni
  Scenario: List available products
    When I execute mgr-sync "list products" with user "admin" and password "admin"
    Then I should get "[ ] openSUSE Leap 15.6 x86_64"

@susemanager
  Scenario: List all available products
    When I execute mgr-sync "list products -e"
    Then I should get "[ ] SUSE Linux Enterprise Desktop 15 SP3 x86_64"
    And I should get "  [ ] (R) Basesystem Module 15 SP3 x86_64"
    And I should get "  [ ] Desktop Applications Module 15 SP3 x86_64"
