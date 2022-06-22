# Copyright (c) 2015-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scc_credentials
Feature: Be able to list available channels and enable them
  In order to use software channels
  As root user
  I want to be able to list available channels and enable them

  Scenario: List available channels
    # Order matters here, refresh first
    When I refresh SCC
    And I execute mgr-sync "list channels -e" with user "admin" and password "admin"
    Then I should get "[ ] SLES12-SP3-Pool for x86_64 SUSE Linux Enterprise Server 12 SP3 x86_64 [sles12-sp3-pool-x86_64]"
    And I should get "    [ ] SLE-Live-Patching12-SP3-Pool for x86_64 SUSE Linux Enterprise Live Patching 12 SP3 x86_64 [sle-live-patching12-sp3-pool-x86_64]"
    And I should get "sles12-sp3-debuginfo"
    And I should get "sles12-sp3-installer"

  Scenario: List available mandatory channels
    When I execute mgr-sync "list channels -e --no-optional"
    Then I should get "[ ] SLES12-SP3-Pool for x86_64 SUSE Linux Enterprise Server 12 SP3 x86_64 [sles12-sp3-pool-x86_64]"
    And I should get "    [ ] SLE-Live-Patching12-SP3-Pool for x86_64 SUSE Linux Enterprise Live Patching 12 SP3 x86_64 [sle-live-patching12-sp3-pool-x86_64]"
    And I shouldn't get "sles12-sp3-debuginfo"
    And I shouldn't get "sles12-sp3-installer"

@susemanager
  Scenario: List products
    When I execute mgr-sync "list products"
    Then I should get "[ ] SUSE Linux Enterprise Server 12 SP5 x86_64"
    And I should get "[ ] SUSE Manager Proxy 4.1 x86_64"

@susemanager
  Scenario: List all products for SUSE Manager
    When I execute mgr-sync "list products --expand"
    Then I should get "[ ] SUSE Linux Enterprise Server 12 SP5 x86_64"
    And I should get "[ ] SUSE Manager Proxy 4.1 x86_64"
    And I should get "  [ ] (R) SUSE Manager Client Tools for RHEL and ES 7 x86_64"
    And I should get "  [ ] (R) SUSE Manager Client Tools for SLE 15 x86_64"

  Scenario: List products with filter
    When I execute mgr-sync "list products --expand --filter x86_64"
    Then I should get "[ ] SUSE Linux Enterprise Server 12 SP5 x86_64"
    And I shouldn't get "ppc64"
    And I shouldn't get "s390x"

  Scenario: Run spacewalk-repo-sync with custom URLs
    When I call spacewalk-repo-sync for channel "test_base_channel" with a custom url "http://localhost/pub/TestRepoRpmUpdates/"
    Then I should see "Channel: test_base_channel" in the output
    And I should see "Sync completed." in the output
    And I should see "Total time:" in the output
    And I should see "Repo URL:" in the output

  Scenario: Enable a real base channel
    When I execute mgr-sync "add channel sles12-sp5-pool-x86_64"
    And I execute mgr-sync "list channels"
    Then I should get "[I] SLES12-SP5-Pool for x86_64 SUSE Linux Enterprise Server 12 SP5 x86_64 [sles12-sp5-pool-x86_64]"
    And I should get "    [I] SLES12-SP5-Updates for x86_64 SUSE Linux Enterprise Server 12 SP5 x86_64 [sles12-sp5-updates-x86_64]"
    And I should get "    [ ] SLE-Module-Containers12-Pool for x86_64 Containers Module 12 x86_64 [sle-module-containers12-pool-x86_64-sp5]"

  Scenario: Enable a real child channel
    When I execute mgr-sync "add channel sle-module-containers12-pool-x86_64-sp5"
    And I execute mgr-sync "list channels"
    Then I should get "[I] SLES12-SP5-Pool for x86_64 SUSE Linux Enterprise Server 12 SP5 x86_64 [sles12-sp5-pool-x86_64]"
    And I should get "    [I] SLE-Module-Containers12-Pool for x86_64 Containers Module 12 x86_64 [sle-module-containers12-pool-x86_64-sp5]"
    And I should get "    [I] SLE-Module-Containers12-Updates for x86_64 Containers Module 12 x86_64 [sle-module-containers12-updates-x86_64-sp5]"

  Scenario: Let mgr-sync time out
    When I remove the mgr-sync cache file
    And I execute mgr-sync refresh
    Then I should get "Timeout. No user input for 60 seconds. Exiting..."
