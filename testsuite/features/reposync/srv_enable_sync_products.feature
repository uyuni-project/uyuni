# Copyright (c) 2018-2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to list available products and enable them
  In order to use software channels
  As root user
  I want to be able to list available products and enable them

  Scenario: List available products
    When I execute mgr-sync "list products" with user "admin" and password "admin"
    Then I should get "[ ] SUSE Linux Enterprise Desktop 15 SP3 x86_64"

  Scenario: List all available products
    When I execute mgr-sync "list products -e"
    Then I should get "[ ] SUSE Linux Enterprise Desktop 15 SP3 x86_64"
    And I should get "  [ ] (R) Basesystem Module 15 SP3 x86_64"
    And I should get "  [ ] Desktop Applications Module 15 SP3 x86_64"
