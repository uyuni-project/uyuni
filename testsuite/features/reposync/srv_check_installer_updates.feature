# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Installer update repositories
  In order to use installer updates
  As root user
  I want that Installer-Updates Channels got synced when adding the product

@scc_credentials
  Scenario: Installer updates channels got enabled during add product
    When I execute mgr-sync "list channels" with user "admin" and password "admin"
    Then I should get "    [I] SLES12-SP5-Installer-Updates for x86_64 SUSE Linux Enterprise Server 12 SP5 x86_64 [sles12-sp5-installer-updates-x86_64]"
    And I should get "    [I] SLE15-SP2-Installer-Updates for x86_64 SUSE Linux Enterprise Server 15 SP2 x86_64 [sle15-sp2-installer-updates-x86_64]"
