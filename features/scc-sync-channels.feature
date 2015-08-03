# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: mgr-sync channel listing and enablement
  In Order to validate correct working of mgr-sync
  As user root
  I want to be able to list available channels and enable them

  Scenario: list available channels
    When I execute mgr-sync "list channels -e" with user "admin" and password "admin"
    Then I want to get "[ ] SLES11-SP1-Pool for x86_64 SUSE Linux Enterprise Server 11 SP1 x86_64 [sles11-sp1-pool-x86_64]"
    And I want to get "    [ ] SLE11-SDK-SP1-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 11 SP1 [sle11-sdk-sp1-updates-x86_64]"

  Scenario: list available mandatory channels
    When I execute mgr-sync "list channels -e --no-optional"
    Then I want to get "[ ] SLES11-SP1-Pool for x86_64 SUSE Linux Enterprise Server 11 SP1 x86_64 [sles11-sp1-pool-x86_64]"
    And I want to get "    [ ] SLE11-SDK-SP1-Updates for x86_64 SUSE Linux Enterprise Software Development Kit 11 SP1 [sle11-sdk-sp1-updates-x86_64]"
    And I wont get "debuginfo"
    And I wont get "sles11-extras"

  Scenario: list products
    When I execute mgr-sync "list products"
    Then I want to get "[ ] SUSE Linux Enterprise Server 12 (x86_64)"
    And I want to get "[ ] SUSE Manager Proxy 2.1 (x86_64)"

  Scenario: list all products
    When I execute mgr-sync "list products --expand"
    Then I want to get "[ ] SUSE Linux Enterprise Server 12 (x86_64)"
    And I want to get "[ ] SUSE Manager Server 2.1 (s390x)"
    And I want to get "  [ ] SUSE Cloud 4 (x86_64)"
    And I want to get "  [ ] SUSE Linux Enterprise High Availability Extension 12 (x86_64)"

  Scenario: list products with filter
    When I execute mgr-sync "list products --expand --filter x86_64"
    Then I want to get "[ ] SUSE Linux Enterprise Server for SAP All-in-One 11 SP3 (x86_64)"
    And I wont get "ppc64"
    And I wont get "s390x"

  Scenario: enable sles11-sp3-pool-x86_64
    When I execute mgr-sync "add channel sles11-sp3-pool-x86_64"
    And I execute mgr-sync "list channels"
    Then I want to get "[I] SLES11-SP3-Pool for x86_64 SUSE Linux Enterprise Server 11 SP3 x86_64 [sles11-sp3-pool-x86_64]"
    And I want to get "    [ ] SLES11-SP3-Updates for x86_64 SUSE Linux Enterprise Server 11 SP3 x86_64 [sles11-sp3-updates-x86_64]"

  Scenario: enable sles11-sp3-updates-x86_64
    When I execute mgr-sync "add channel sles11-sp3-updates-x86_64"
    And I execute mgr-sync "list channels"
    Then I want to get "[I] SLES11-SP3-Pool for x86_64 SUSE Linux Enterprise Server 11 SP3 x86_64 [sles11-sp3-pool-x86_64]"
    And I want to get "    [I] SLES11-SP3-Updates for x86_64 SUSE Linux Enterprise Server 11 SP3 x86_64 [sles11-sp3-updates-x86_64]"

  Scenario: let mgr-sync time out
    When I remove the mgr-sync cache file
    And I execute mgr-sync refresh
    Then I want to get "Timeout. No user input for 60 seconds. Exiting..."
