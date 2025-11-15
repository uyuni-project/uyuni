# Copyright (c) 2015-2025 SUSE LLC
# SPDX-License-Identifier: MIT

@scc_credentials
Feature: List available channels and enable them
  In order to use software channels
  As root user
  I want to list available channels and enable them

  Scenario: List available channels
    # Order matters here, refresh first
    When I refresh SCC
    And I execute mgr-sync "list channels -e" with user "admin" and password "admin"
    Then I should get "[ ] SLE-Product-SLES15-SP4-Pool for x86_64 SUSE Linux Enterprise Server 15 SP4 x86_64 [sle-product-sles15-sp4-pool-x86_64]"
    And I should get "    [ ] SLE-Product-SLES15-SP4-Updates for x86_64 SUSE Linux Enterprise Server 15 SP4 x86_64 [sle-product-sles15-sp4-updates-x86_64]"
    And I should get "    [ ] SLE15-SP4-Installer-Updates for x86_64 SUSE Linux Enterprise Server 15 SP4 x86_64 [sle15-sp4-installer-updates-x86_64]"

  Scenario: List available mandatory channels
    When I execute mgr-sync "list channels -e --no-optional"
    Then I should get "[ ] SLE-Product-SLES15-SP4-Pool for x86_64 SUSE Linux Enterprise Server 15 SP4 x86_64 [sle-product-sles15-sp4-pool-x86_64]"
    And I should get "    [ ] SLE-Product-SLES15-SP4-Updates for x86_64 SUSE Linux Enterprise Server 15 SP4 x86_64 [sle-product-sles15-sp4-updates-x86_64]"
    And I shouldn't get "    [ ] SLE15-SP4-Installer-Updates for x86_64 SUSE Linux Enterprise Server 15 SP4 x86_64 [sle15-sp4-installer-updates-x86_64]"

  Scenario: List Server product
    When I execute mgr-sync "list products"
    Then I should get "[ ] SUSE Linux Enterprise Server 15 SP4 x86_64"

@proxy
@susemanager
  Scenario: List Proxy product
    When I execute mgr-sync "list products"
    Then I should get "[ ] SUSE Manager Proxy 4.3 x86_64"

@proxy
@susemanager
  Scenario: List all products for SUSE Multi-Linux Manager
    When I execute mgr-sync "list products --expand"
    Then I should get "[ ] SUSE Linux Enterprise Server 15 SP4 x86_64"
    And I should get "[ ] SUSE Manager Proxy 4.3 x86_64"
    And I should get "  [ ] (R) SUSE Multi-Linux Manager Beta Client Tools for SUSE Liberty Linux 7, RHEL and clones 7 x86_64 (BETA)"
    And I should get "  [ ] (R) SUSE Multi-Linux Manager Beta Client Tools for SLE 15 x86_64 (BETA)"

  Scenario: List products with filter
    When I execute mgr-sync "list products --expand --filter x86_64"
    Then I should get "[ ] SUSE Linux Enterprise Server 15 SP4 x86_64"
    And I shouldn't get "ppc64"
    And I shouldn't get "s390x"

  Scenario: Let mgr-sync time out
    When I remove the mgr-sync cache file
    And I execute mgr-sync refresh
    Then I should get "Timeout. No user input for 60 seconds. Exiting..."
