# Copyright (c) 2015-2023 SUSE LLC
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
    Then I should get "[ ] SLE-Product-SLES15-SP4-Pool for x86_64 SUSE Linux Enterprise Server 15 SP4 x86_64 [sle-product-sles15-sp4-pool-x86_64]"
    And I should get "    [ ] SLE-Product-SLES15-SP4-Updates for x86_64 SUSE Linux Enterprise Server 15 SP4 x86_64 [sle-product-sles15-sp4-updates-x86_64]"
    And I should get "    [ ] SLE15-SP4-Installer-Updates for x86_64 SUSE Linux Enterprise Server 15 SP4 x86_64 [sle15-sp4-installer-updates-x86_64]"

  Scenario: List available mandatory channels
    When I execute mgr-sync "list channels -e --no-optional"
    Then I should get "[ ] SLE-Product-SLES15-SP4-Pool for x86_64 SUSE Linux Enterprise Server 15 SP4 x86_64 [sle-product-sles15-sp4-pool-x86_64]"
    And I should get "    [ ] SLE-Product-SLES15-SP4-Updates for x86_64 SUSE Linux Enterprise Server 15 SP4 x86_64 [sle-product-sles15-sp4-updates-x86_64]"
    And I shouldn't get "    [ ] SLE15-SP4-Installer-Updates for x86_64 SUSE Linux Enterprise Server 15 SP4 x86_64 [sle15-sp4-installer-updates-x86_64]"

@susemanager
  Scenario: List products
    When I execute mgr-sync "list products"
    Then I should get "[ ] SUSE Linux Enterprise Server 15 SP4 x86_64"
    And I should get "[ ] SUSE Manager Proxy 4.3 x86_64"

@susemanager
  Scenario: List all products for SUSE Manager
    When I execute mgr-sync "list products --expand"
    Then I should get "[ ] SUSE Linux Enterprise Server 15 SP4 x86_64"
    And I should get "[ ] SUSE Manager Proxy 4.3 x86_64"
    And I should get "  [ ] (R) SUSE Manager Client Tools for RHEL, Liberty and Clones 7 x86_64"
    And I should get "  [ ] (R) SUSE Manager Client Tools for SLE 15 x86_64"

  Scenario: List products with filter
    When I execute mgr-sync "list products --expand --filter x86_64"
    Then I should get "[ ] SUSE Linux Enterprise Server 15 SP4 x86_64"
    And I shouldn't get "ppc64"
    And I shouldn't get "s390x"

  Scenario: Run spacewalk-repo-sync with custom URLs
    When I call spacewalk-repo-sync for channel "fake_base_channel" with a custom url "http://localhost/pub/TestRepoRpmUpdates/"
    Then I should see "Channel: fake_base_channel" in the output
    And I should see "Sync completed." in the output
    And I should see "Total time:" in the output
    And I should see "Repo URL:" in the output

@susemanager
  Scenario: Enable RHEL 8 channels for Rocky 8
    When I execute mgr-sync "add channel rhel8-pool-x86_64"
    And I execute mgr-sync "add channel res-8-updates-x86_64"
    And I execute mgr-sync "add channel res-as-8-updates-x86_64"
    And I execute mgr-sync "add channel res-cb-8-updates-x86_64"
    And I execute mgr-sync "add channel res8-manager-tools-pool-x86_64"
    And I execute mgr-sync "add channel res8-manager-tools-updates-x86_64"
    And I execute mgr-sync "list channels"
    Then I should get "[I] RHEL8-Pool for x86_64 RHEL or SLES ES or CentOS 8 Base [rhel8-pool-x86_64]"
    And I should get "[I] RES-8-Updates for x86_64 SUSE Linux Enterprise Server with Expanded Support 8 x86_64 [res-8-updates-x86_64]"
    And I should get "[I] RES-AS-8-Updates for x86_64 SUSE Linux Enterprise Server with Expanded Support 8 x86_64 [res-as-8-updates-x86_64]"
    And I should get "[I] RES-CB-8-Updates for x86_64 SUSE Linux Enterprise Server with Expanded Support 8 x86_64 [res-cb-8-updates-x86_64]"

#@uyuni
#  Scenario: Enable RHEL 8 channels for Rocky 8
#    When I execute mgr-sync "add channel rhel8-pool-x86_64"
#    And I execute mgr-sync "add channel res-8-updates-x86_64"
#    And I execute mgr-sync "add channel res-as-8-updates-x86_64"
#    And I execute mgr-sync "add channel res-cb-8-updates-x86_64"
#    And I use spacewalk-common-channel to add channel "sll8-uyuni-client" with arch "x86_64"
#    And I execute mgr-sync "list channels"
#    Then I should get "[I] RHEL8-Pool for x86_64 RHEL or SLES ES or CentOS 8 Base [rhel8-pool-x86_64]"
#    And I should get "[I] RES-8-Updates for x86_64 SUSE Linux Enterprise Server with Expanded Support 8 x86_64 [res-8-updates-x86_64]"
#    And I should get "[I] RES-AS-8-Updates for x86_64 SUSE Linux Enterprise Server with Expanded Support 8 x86_64 [res-as-8-updates-x86_64]"
#    And I should get "[I] RES-CB-8-Updates for x86_64 SUSE Linux Enterprise Server with Expanded Support 8 x86_64 [res-cb-8-updates-x86_64]"

  Scenario: Let mgr-sync time out
    When I remove the mgr-sync cache file
    And I execute mgr-sync refresh
    Then I should get "Timeout. No user input for 60 seconds. Exiting..."
