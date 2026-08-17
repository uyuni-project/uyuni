# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in:
# If minion registration on the peripheral server fails:
# - features/hub/srv_hub_outage_resilience.feature

@scope_hub
@hub_full_topology
@server2
@sle_minion
Feature: Hub full topology - minion managed via peripheral server
  In order to verify end-to-end content delivery in a hub topology
  As an authorized user
  I want to register a peripheral, sync channels, and manage minions through the peripheral (plan B-01..B-04)

  # The two A-08 multicast scenarios below (borrowed from srv_hub_xmlrpc_operations.feature)
  # run here, not there, because this is the only place in run_sets/hub_full_topology.yml
  # where server2 is registered AND a real minion (sle_minion) exists at the same time.
  #
  # This feature intentionally does NOT delete sle_minion in its own cleanup below --
  # srv_hub_outage_resilience.feature (runs later, must be last in the run set) reuses
  # this same minion instance and owns its final cleanup instead.

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Register server2 as a peripheral on the hub (B-01 prerequisite)
    When I add "server2" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "server2" in peripherals list

#  @scc_credentials
#  @susemanager
#  Scenario: Synchronize SLES 15 SP7 product for minion bootstrap content (B-03 prerequisite)
#    When I follow the left menu "Admin > Setup Wizard > Products"
#    And I wait until I do not see "currently running" text
#    And I wait until I do not see "Loading" text
#    And I enter "SUSE Linux Enterprise Server 15 SP7" as the filtered product description
#    And I select "x86_64" from "product-arch-filter"
#    And I select "SUSE Linux Enterprise Server 15 SP7" as a product
#    Then I should see the "SUSE Linux Enterprise Server 15 SP7" selected
#    When I click the Add Product button
#    And I wait until I see "Selected channels/products were scheduled successfully for syncing." text
#    And I wait until I see "SUSE Linux Enterprise Server 15 SP7" product has been added
#    And I wait until all synchronized channels for "sles15-sp7" have finished

  Scenario: Sync the SLES 15 SP7 base channel and its modules from hub to server2 for minion bootstrap (B-03 prerequisite)
    # "Edit channels" is a parent/child tree -- checking the SLE-Product-SLES15-SP7-Pool row
    # alone only selects that one row. The client also needs its vendor module channels
    # (Basesystem, Server Applications, etc.), which are separate child rows with their own
    # checkboxes. "-SP7-" matches the pool channel and all "SLE-Module-...-SP7-..." /
    # "...-SP7-Updates" / "...-SP7-Installer-Updates" rows, while excluding unrelated custom
    # test channels nested under the same parent (e.g. Fake-RPM-SUSE-Channel) which don't
    # contain that substring.
    When I configure hub to sync all "-SP7-" channels to "server2"
#    And I configure hub to sync channel "ManagerTools-SLE15-Pool for x86_64 SP7" to "server2"
#    And I configure hub to sync channel "ManagerTools-SLE15-Updates for x86_64 SP7" to "server2"

  Scenario: Trigger channel sync from hub to server2 and wait for completion (B-03 prerequisite)
    Given I am authorized for the "Admin" section on "server2"
    When I initiate channel sync from peripheral "server2"
    Then I should see a "Successfully scheduled a channels synchronization." text
    And I wait at most 600 seconds until channel "sle-product-sles15-sp7-pool-x86_64" has been synced on "server2"
    Then channel "sle-product-sles15-sp7-pool-x86_64" should exist on "server2"

  Scenario: Create activation key on server2 peripheral with hub-synced channel (B-03)
    When I create an activation key "1-hub-test-key" on "server2" with channel "sle-product-sles15-sp7-pool-x86_64"
    Then I should see a "1-hub-test-key" text

#  @proxy
#  Scenario: Verify proxy is registered to server2 with proxy system type before bootstrap (B-02)
#    Then I should see "proxy" in "server2" system list as proxy type

  Scenario: Create the bootstrap repository on server2 peripheral for sle_minion (B-03 prerequisite)
    When I create the bootstrap repository for "sle_minion" on server2

  Scenario: Bootstrap sle_minion directly to server2 peripheral (B-03)
    Given I am authorized for the "Admin" section on "server2"
    When I bootstrap "sle_minion" to peripheral "server2" using activation key "1-hub-test-key"
    And I wait until onboarding is completed for "sle_minion"
    Then I should see "sle_minion" registered on "server2"

  Scenario: Execute multicast system list across all peripherals (A-08)
    Given I am connected to the hub XMLRPC API
    When I call hub.listServerIds via XMLRPC
    And I call multicast.system.list_systems via XMLRPC
    Then multicast response should have successful responses

  Scenario: Verify multicast response contains systems from peripheral (A-08)
    Then multicast response should contain systems from "server2"

  Scenario: Verify sle_minion is not listed on the hub directly (B-03)
    Then I should not see "sle_minion" registered on hub

  @proxy
  Scenario: Bootstrap sle_minion to server2 via proxy (B-03 via-proxy path)
    Given I am authorized for the "Admin" section on "server2"
    When I bootstrap "sle_minion" to peripheral "server2" using activation key "1-hub-test-key"
    And I wait until onboarding is completed for "sle_minion"
    Then I should see "sle_minion" registered on "server2"

  # KNOWN BROKEN: the B-04 scenarios below install/verify/downgrade/patch the andromeda-dummy
  # test package, which only exists in Fake-RPM-SUSE-Channel content. The activation key above
  # now uses the real SLE-Product-SLES15-SP7-Pool vendor channel instead (see B-03 prerequisite
  # comment), so andromeda-dummy is no longer synced to server2 and these scenarios have no
  # package to act on. Left as-is pending a follow-up decision on what real (or newly-synced
  # custom) package/channel these should use.
  Scenario: Install a package on sle_minion from hub-synced channel on server2 (B-04)
    Given I am authorized for the "Admin" section on "server2"
    And I am on the Systems overview page of this "sle_minion" on server2
    When I follow "Software" in the content area
    And I follow "Install" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    And I wait until event "Package Install/Upgrade scheduled by admin" is completed

  Scenario: Verify andromeda-dummy is installed on sle_minion (B-04)
    Given I am authorized for the "Admin" section on "server2"
    And I am on the Systems overview page of this "sle_minion" on server2
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    Then I should see a "andromeda-dummy" link

  Scenario: Downgrade andromeda-dummy to old version on sle_minion for errata test (B-04)
    When I remove package "andromeda-dummy" from this "sle_minion" without error control
    And I install old package "andromeda-dummy-1.0" on this "sle_minion" without error control
    And I refresh the metadata for "sle_minion"
    And I refresh packages list via spacecmd on "sle_minion"
    And I wait until refresh package list on "sle_minion" is finished

  Scenario: Apply errata andromeda-dummy-6789 on sle_minion via server2 peripheral API (B-04)
    When I apply erratum "andromeda-dummy-6789" on "sle_minion" via "server2" peripheral API
    And I wait for "andromeda-dummy-2.0-1.1" to be installed on "sle_minion"

  Scenario: Verify andromeda-dummy is updated to patched version on sle_minion (B-04)
    Given I am authorized for the "Admin" section on "server2"
    And I am on the Systems overview page of this "sle_minion" on server2
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    Then I should see a "andromeda-dummy-2.0-1.1" link

  Scenario: Run a remote command on sle_minion via server2 peripheral (B-04)
    When I run a remote command "hostname" on "sle_minion" via "server2"
    Then the remote command should complete on "sle_minion"

  Scenario: Verify package checksum on sle_minion matches hub content (B-04)
    Then the package "andromeda-dummy" checksum on "sle_minion" should match the same package on hub

  Scenario: Cleanup - remove andromeda-dummy from sle_minion
    Given I am authorized for the "Admin" section on "server2"
    And I am on the Systems overview page of this "sle_minion" on server2
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    And I check "andromeda-dummy" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    And I wait until event "Package Removal scheduled by admin" is completed

  Scenario: Cleanup - remove synced channels from server2
    When I remove synced channels from "server2"
    Then I should see a "Channel configuration updated" text

  Scenario: Cleanup - deregister server2 from hub
    When I unregister "server2" from hub
    Then I should not see the name of "server2"
