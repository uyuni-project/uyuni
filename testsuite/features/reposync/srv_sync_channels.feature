# Copyright (c) 2015-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to list available channels and enable them
  In order to use software channels
  As root user
  I want to be able to list available channels and enable them

@scc_credentials
  Scenario: List available channels
    # Order matters here, refresh first
    When I refresh SCC
    And I execute mgr-sync "list channels -e" with user "admin" and password "admin"
    Then I should get "[ ] SLES11-SP1-Pool for x86_64 SUSE Linux Enterprise Server 11 SP1 x86_64 [sles11-sp1-pool-x86_64]"
    And I should get "    [ ] SLE11-SDK-SP1-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 11 SP1 [sle11-sdk-sp1-updates-x86_64]"

@scc_credentials
  Scenario: List available mandatory channels
    When I execute mgr-sync "list channels -e --no-optional" with user "admin" and password "admin"
    Then I should get "[ ] SLES11-SP1-Pool for x86_64 SUSE Linux Enterprise Server 11 SP1 x86_64 [sles11-sp1-pool-x86_64]"
    And I should get "    [ ] SLE11-SDK-SP1-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 11 SP1 [sle11-sdk-sp1-updates-x86_64]"
    And I shouldn't get "debuginfo"
    And I shouldn't get "sles11-extras"

@scc_credentials
  Scenario: List products
    When I execute mgr-sync "list products" with user "admin" and password "admin"
    Then I should get "[ ] SUSE Linux Enterprise Server 12 x86_64"
    And I should get "[ ] SUSE Manager Proxy 2.1 x86_64"

@scc_credentials
@susemanager
  Scenario: List all products for SUSE Manager
    When I execute mgr-sync "list products --expand" with user "admin" and password "admin"
    Then I should get "[ ] SUSE Linux Enterprise Server 12 x86_64"
    And I should get "[ ] SUSE Manager Proxy 2.1 x86_64"
    And I should get "  [ ] (R) SUSE Linux Enterprise Client Tools RES 7 x86_64"
    And I should get "  [ ] (R) SUSE Manager Tools 15 x86_64"

@scc_credentials
@uyuni
  Scenario: List all products for Uyuni
    When I execute mgr-sync "list products --expand" with user "admin" and password "admin"
    Then I should get "[ ] SUSE Linux Enterprise Server 12 x86_64"
    And I should get "[ ] SUSE Manager Proxy 2.1 x86_64"

@scc_credentials
  Scenario: List products with filter
    When I execute mgr-sync "list products --expand --filter x86_64" with user "admin" and password "admin"
    Then I should get "[ ] SUSE Linux Enterprise Server 12 SP3 x86_64"
    And I shouldn't get "ppc64"
    And I shouldn't get "s390x"

@scc_credentials
  Scenario: Run spacewalk-repo-sync with custom URLs
    When I call spacewalk-repo-sync for channel "test_base_channel" with a custom url "http://localhost/pub/TestRepoRpmUpdates/"
    Then I should see "Channel: test_base_channel" in the output
    And I should see "Sync completed." in the output
    And I should see "Total time:" in the output
    And I should see "Repo URL:" in the output

@scc_credentials
  Scenario: Enable sles12-sp5-pool-x86_64
    When I execute mgr-sync "add channel sles12-sp5-pool-x86_64" with user "admin" and password "admin"
    And I execute mgr-sync "list channels"
    Then I should get "[I] SLES12-SP5-Pool for x86_64 SUSE Linux Enterprise Server 12 SP5 x86_64 [sles12-sp5-pool-x86_64]"
    And I should get "    [I] SLES12-SP5-Updates for x86_64 SUSE Linux Enterprise Server 12 SP5 x86_64 [sles12-sp5-updates-x86_64]"
    And I should get "    [ ] SLE-Module-Containers12-Pool for x86_64 Containers Module 12 x86_64 [sle-module-containers12-pool-x86_64-sp5]"

@scc_credentials
  Scenario: Enable sle-module-containers12-pool-x86_64-sp5
    When I execute mgr-sync "add channel sle-module-containers12-pool-x86_64-sp5" with user "admin" and password "admin"
    And I execute mgr-sync "list channels"
    Then I should get "[I] SLES12-SP5-Pool for x86_64 SUSE Linux Enterprise Server 12 SP5 x86_64 [sles12-sp5-pool-x86_64]"
    And I should get "    [I] SLE-Module-Containers12-Pool for x86_64 Containers Module 12 x86_64 [sle-module-containers12-pool-x86_64-sp5]"
    And I should get "    [I] SLE-Module-Containers12-Updates for x86_64 Containers Module 12 x86_64 [sle-module-containers12-updates-x86_64-sp5]"

@scc_credentials
  Scenario: Let mgr-sync time out
    When I remove the mgr-sync cache file
    And I execute mgr-sync refresh
    Then I should get "Timeout. No user input for 60 seconds. Exiting..."
