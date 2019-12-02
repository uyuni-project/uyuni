# Copyright (c) 2018-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to list available products and enable them
  In order to use software channels
  As root user
  I want to be able to list available products and enable them

  Scenario: List available products
    When I execute mgr-sync "list products" with user "admin" and password "admin"
    Then I should get "[I] SUSE Linux Enterprise Server 12 SP2 x86_64"
    And I should get "[ ] SUSE Linux Enterprise Desktop 15 x86_64"

  Scenario: List all available products
    When I execute mgr-sync "list products -e" with user "admin" and password "admin"
    Then I should get "[ ] SUSE Linux Enterprise Desktop 15 x86_64"
    And I should get "  [ ] (R) Basesystem Module 15 x86_64"
    And I should get "  [ ] Desktop Applications Module 15 x86_64"

  Scenario: Enable "SUSE Linux Enterprise Desktop 15 x86_64" with recommended modules
    When I enable product "SUSE Linux Enterprise Desktop 15 x86_64"
    Then I should get "Adding channels required by 'SUSE Linux Enterprise Desktop 15 x86_64' product"
    And I should get "- sle-product-sled15-updates-x86_64"
    And I should get "- sle-product-sled15-pool-x86_64"
    And I should get "- sle-module-basesystem15-updates-x86_64-sled"
    And I should get "- sle-module-basesystem15-pool-x86_64-sled"
    And I should get "- sle-module-desktop-applications15-updates-x86_64-sled"
    And I should get "- sle-module-desktop-applications15-pool-x86_64-sled"
    And I should get "- sle-product-we15-updates-x86_64-sled"
    And I should get "- sle-product-we15-pool-x86_64-sled"
    And I should get "Product successfully added"

  Scenario: Enable "SUSE Linux Enterprise Server for SAP Applications 15 x86_64" without recommended modules
    When I enable product "SUSE Linux Enterprise Server for SAP Applications 15 x86_64" without recommended
    Then I should get "Adding channels required by 'SUSE Linux Enterprise Server for SAP Applications 15 x86_64' product"
    And I should get "- sle-product-sles_sap15-updates-x86_64"
    And I should get "- sle-product-sles_sap15-pool-x86_64"
    And I shouldn't get "sle-module-basesystem15-pool-x86_64-sap"
    And I shouldn't get "sle-module-basesystem15-updates-x86_64-sap"
    And I should get "Product successfully added"
