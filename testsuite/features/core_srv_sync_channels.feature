# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to list available channels and enable them
  In order to use software channels
  As root user
  I want to be able to list available channels and enable them

  Scenario: List available channels
    When I execute mgr-sync "list channels -e" with user "admin" and password "admin"
    Then I should get "[ ] SLES11-SP1-Pool for x86_64 SUSE Linux Enterprise Server 11 SP1 x86_64 [sles11-sp1-pool-x86_64]"
    And I should get "    [ ] SLE11-SDK-SP1-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 11 SP1 [sle11-sdk-sp1-updates-x86_64]"

  Scenario: List available mandatory channels
    When I execute mgr-sync "list channels -e --no-optional"
    Then I should get "[ ] SLES11-SP1-Pool for x86_64 SUSE Linux Enterprise Server 11 SP1 x86_64 [sles11-sp1-pool-x86_64]"
    And I should get "    [ ] SLE11-SDK-SP1-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 11 SP1 [sle11-sdk-sp1-updates-x86_64]"
    And I shouldn't get "debuginfo"
    And I shouldn't get "sles11-extras"

  Scenario: List products
    When I execute mgr-sync "list products"
    Then I should get "[ ] SUSE Linux Enterprise Server 12 (x86_64)"
    And I should get "[ ] SUSE Manager Proxy 2.1 (x86_64)"

  Scenario: List all products
    When I execute mgr-sync "list products --expand"
    Then I should get "[ ] SUSE Linux Enterprise Server 12 (x86_64)"
    And I should get "[ ] SUSE Manager Proxy 2.1 (x86_64)"
    And I should get "  [ ] SUSE Cloud 4 (x86_64)"
    And I should get "  [ ] SUSE Linux Enterprise High Availability Extension 12 (x86_64)"

  Scenario: List products with filter
    When I execute mgr-sync "list products --expand --filter x86_64"
    Then I should get "[ ] SUSE Linux Enterprise Server for SAP All-in-One 11 SP3 (x86_64)"
    And I shouldn't get "ppc64"
    And I shouldn't get "s390x"

  Scenario: Enable sles12-sp2-pool-x86_64
    When I execute mgr-sync "add channel sles12-sp2-pool-x86_64"
    And I execute mgr-sync "list channels"
    Then I should get "[I] SLES12-SP2-Pool for x86_64 SUSE Linux Enterprise Server 12 SP2 x86_64 [sles12-sp2-pool-x86_64]"
    And I should get "    [ ] SLES12-SP2-Updates for x86_64 SUSE Linux Enterprise Server 12 SP2 x86_64 [sles12-sp2-updates-x86_64]"

  Scenario: Enable sles12-sp2-updates-x86_64
    When I execute mgr-sync "add channel sles12-sp2-updates-x86_64"
    And I execute mgr-sync "list channels"
    Then I should get "[I] SLES12-SP2-Pool for x86_64 SUSE Linux Enterprise Server 12 SP2 x86_64 [sles12-sp2-pool-x86_64]"
    And I should get "    [I] SLES12-SP2-Updates for x86_64 SUSE Linux Enterprise Server 12 SP2 x86_64 [sles12-sp2-updates-x86_64]"

  Scenario: Let mgr-sync time out
    When I remove the mgr-sync cache file
    And I execute mgr-sync refresh
    Then I should get "Timeout. No user input for 60 seconds. Exiting..."
