# Copyright (c) 2015-2019 SUSE LLC
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
    Then I should get "[ ] SUSE Linux Enterprise Server 12 x86_64"
    And I should get "[ ] SUSE Manager Proxy 2.1 x86_64"

  Scenario: List all products
    When I execute mgr-sync "list products --expand"
    Then I should get "[ ] SUSE Linux Enterprise Server 12 x86_64"
    And I should get "[ ] SUSE Manager Proxy 2.1 x86_64"
    And I should get "  [ ] SUSE Cloud 4 x86_64"
    And I should get "  [ ] SUSE Linux Enterprise High Availability Extension 12 x86_64"

  Scenario: List products with filter
    When I execute mgr-sync "list products --expand --filter x86_64"
    Then I should get "[ ] SUSE Linux Enterprise Server for SAP All-in-One 11 SP4 x86_64"
    And I shouldn't get "ppc64"
    And I shouldn't get "s390x"

  Scenario: Run spacewalk-repo-sync with custom URLs
    When I call spacewalk-repo-sync for channel "test_base_channel" with a custom url "http://localhost/pub/TestRepo/"
    Then I should see "Channel: test_base_channel" in the output
    And I should see "Sync completed." in the output
    And I should see "Total time:" in the output
    And I should see "Repo URL:" in the output

  Scenario: Enable sles12-sp4-pool-x86_64
    # This automaticaly enables all required channels
    When I execute mgr-sync "add channel sles12-sp4-pool-x86_64"
    And I execute mgr-sync "list channels"
    Then I should get "[I] SLES12-SP4-Pool for x86_64 SUSE Linux Enterprise Server 12 SP4 x86_64 [sles12-sp4-pool-x86_64]"
    And I should get "    [I] SLES12-SP4-Updates for x86_64 SUSE Linux Enterprise Server 12 SP4 x86_64 [sles12-sp4-updates-x86_64]"
    And I should get "    [ ] SLE-Module-Containers12-Pool for x86_64 Containers Module 12 x86_64 [sle-module-containers12-pool-x86_64-sp4]"

  Scenario: Enable sle-module-containers12-pool-x86_64-sp4
    # This automatically enables all required channels
    When I execute mgr-sync "add channel sle-module-containers12-pool-x86_64-sp4"
    And I execute mgr-sync "list channels"
    Then I should get "[I] SLES12-SP4-Pool for x86_64 SUSE Linux Enterprise Server 12 SP4 x86_64 [sles12-sp4-pool-x86_64]"
    And I should get "    [I] SLE-Module-Containers12-Pool for x86_64 Containers Module 12 x86_64 [sle-module-containers12-pool-x86_64-sp4]"
    And I should get "    [I] SLE-Module-Containers12-Updates for x86_64 Containers Module 12 x86_64 [sle-module-containers12-updates-x86_64-sp4]"

  Scenario: Let mgr-sync time out
    When I remove the mgr-sync cache file
    And I execute mgr-sync refresh
    Then I should get "Timeout. No user input for 60 seconds. Exiting..."

  Scenario: Cleanup: abort all reposync activity
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Task Schedules"
    And I follow "mgr-sync-refresh-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    And I click on "Delete Schedule"
    Then I make sure no spacewalk-repo-sync is in execution
