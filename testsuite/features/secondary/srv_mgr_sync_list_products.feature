# Copyright (c) 2018-2026 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: List available products
  In order to use software channels
  As root user
  I want to list available products from command line

@susemanager
  Scenario: List available products
    When I execute mgr-sync "list products" with user "admin" and password "admin"
    Then I should get "[ ] SUSE Linux Enterprise Server 15 SP6 x86_64"

@uyuni
  Scenario: List available products
    When I execute mgr-sync "list products" with user "admin" and password "admin"
    Then I should get "[ ] RHEL and Liberty 8 Base"

@susemanager
  Scenario: List all available products
    When I execute mgr-sync "list products -e"
    Then I should get "[ ] SUSE Linux Enterprise Server 15 SP6 x86_64"
    And I should get "  [ ] (R) Basesystem Module 15 SP6 x86_64"
    And I should get "  [ ] Desktop Applications Module 15 SP6 x86_64"
