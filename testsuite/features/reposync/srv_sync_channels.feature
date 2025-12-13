# Copyright (c) 2015-2025 SUSE LLC
# Licensed under the terms of the MIT license.

@scc_credentials
Feature: List available channels and enable them
  In order to use software channels
  As root user
  I want to list available channels and enable them

  Scenario: List available channels
    # Order matters here, refresh first
    When I refresh SCC
    And I execute mgr-sync "list channels -e" with user "admin" and password "admin"
    Then I should get "[ ] SLE-Product-SLES15-SP7-Pool for x86_64 SUSE Linux Enterprise Server 15 SP7 x86_64 [sle-product-sles15-sp7-pool-x86_64]"
    And I should get "    [ ] SLE-Product-SLES15-SP7-Updates for x86_64 SUSE Linux Enterprise Server 15 SP7 x86_64 [sle-product-sles15-sp7-updates-x86_64]"
    And I should get "    [ ] SLE15-SP7-Installer-Updates for x86_64 SUSE Linux Enterprise Server 15 SP7 x86_64 [sle15-sp7-installer-updates-x86_64]"

  Scenario: List available mandatory channels
    When I execute mgr-sync "list channels -e --no-optional"
    Then I should get "[ ] SLE-Product-SLES15-SP7-Pool for x86_64 SUSE Linux Enterprise Server 15 SP7 x86_64 [sle-product-sles15-sp7-pool-x86_64]"
    And I should get "    [ ] SLE-Product-SLES15-SP7-Updates for x86_64 SUSE Linux Enterprise Server 15 SP7 x86_64 [sle-product-sles15-sp7-updates-x86_64]"
    And I shouldn't get "    [ ] SLE15-SP7-Installer-Updates for x86_64 SUSE Linux Enterprise Server 15 SP7 x86_64 [sle15-sp7-installer-updates-x86_64]"

  Scenario: List Server product
    When I execute mgr-sync "list products"
    Then I should get "[ ] SUSE Linux Enterprise Server 15 SP7 x86_64"

@proxy
@susemanager
  Scenario: List all products for SUSE Multi-Linux Manager
    When I execute mgr-sync "list products --expand"
    Then I should get "[ ] SUSE Linux Enterprise Server 15 SP7 x86_64"
    And I should get "  [ ] SUSE Multi-Linux Manager Proxy Extension 5.1 x86_64"
    And I should get "  [ ] SUSE Multi-Linux Manager Proxy Extension for SLE 5.1 x86_64"
    And I should get "  [ ] (R) SUSE Multi-Linux Manager Client Tools for SUSE Liberty Linux 7, RHEL and clones 7 x86_64"
    And I should get "  [ ] (R) SUSE Multi-Linux Manager Client Tools for SLE 15 x86_64"

  Scenario: List products with filter
    When I execute mgr-sync "list products --expand --filter x86_64"
    Then I should get "[ ] SUSE Linux Enterprise Server 15 SP7 x86_64"
    And I shouldn't get "ppc64"
    And I shouldn't get "s390x"

  Scenario: Let mgr-sync time out
    When I remove the mgr-sync cache file
    And I execute mgr-sync refresh
    Then I should get "Timeout. No user input for 60 seconds. Exiting..."
