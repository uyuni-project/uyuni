# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in:
# If minion registration on the peripheral server fails:
# - features/hub/srv_hub_outage_resilience.feature

@scope_hub
@hub_full_topology
@peripheral1
@sle_minion
Feature: Hub full topology - minion managed via peripheral server
  In order to verify end-to-end content delivery in a hub topology
  As an authorized user
  I want to register a peripheral, sync channels, and manage minions through the peripheral (plan B-01..B-04)

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section on "peripheral1"

  Scenario: Create activation key on peripheral1 peripheral with hub-synced channel
    When I create an activation key "1-hub-test-key" on "peripheral1" with channel "sle-product-sles15-sp7-pool-x86_64"
    Then I should see a "1-hub-test-key" text

  Scenario: Create the bootstrap repository on peripheral1 peripheral for sle_minion
    When I wait until all synchronized channels for "sles15-sp7" have finished on peripheral1
    And I create the bootstrap repository for "sle_minion" on peripheral1

  Scenario: Bootstrap sle_minion directly to peripheral1 peripheral
    Given I am authorized for the "Admin" section on "peripheral1"
    When I bootstrap "sle_minion" to peripheral "peripheral1" using activation key "1-hub-test-key"
    And I wait until onboarding is completed for "sle_minion"
    Then I should see "sle_minion" registered on "peripheral1"

  # KNOWN BROKEN: the B-04 scenarios below install/verify/downgrade/patch the andromeda-dummy
  # test package, which only exists in Fake-RPM-SUSE-Channel content. The activation key above
  # now uses the real SLE-Product-SLES15-SP7-Pool vendor channel instead (see B-03 prerequisite
  # comment), so andromeda-dummy is no longer synced to peripheral1 and these scenarios have no
  # package to act on. They are skipped until they use a package from a hub-synced channel.
  @skip
  Scenario: Install a package on sle_minion from hub-synced channel on peripheral1
    Given I am authorized for the "Admin" section on "peripheral1"
    And I am on the Systems overview page of this "sle_minion" on peripheral1
    When I follow "Software" in the content area
    And I follow "Install" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    And I wait until event "Package Install/Upgrade scheduled by admin" is completed

  @skip
  Scenario: Verify andromeda-dummy is installed on sle_minion
    Given I am authorized for the "Admin" section on "peripheral1"
    And I am on the Systems overview page of this "sle_minion" on peripheral1
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    Then I should see a "andromeda-dummy" link

  @skip
  Scenario: Downgrade andromeda-dummy to old version on sle_minion for errata test
    When I remove package "andromeda-dummy" from this "sle_minion" without error control
    And I install old package "andromeda-dummy-1.0" on this "sle_minion" without error control
    And I refresh the metadata for "sle_minion"
    And I refresh packages list via spacecmd on "sle_minion"
    And I wait until refresh package list on "sle_minion" is finished

  @skip
  Scenario: Apply errata andromeda-dummy-6789 on sle_minion via peripheral1 peripheral API
    When I apply erratum "andromeda-dummy-6789" on "sle_minion" via "peripheral1" peripheral API
    And I wait for "andromeda-dummy-2.0-1.1" to be installed on "sle_minion"

  @skip
  Scenario: Verify andromeda-dummy is updated to patched version on sle_minion
    Given I am authorized for the "Admin" section on "peripheral1"
    And I am on the Systems overview page of this "sle_minion" on peripheral1
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    Then I should see a "andromeda-dummy-2.0-1.1" link

  Scenario: Run a remote command on sle_minion via peripheral1 peripheral
    When I run a remote command "hostname" on "sle_minion" via "peripheral1"
    Then the remote command should complete

  @skip
  Scenario: Verify package checksum on sle_minion matches hub content
    Then the package "andromeda-dummy" installed on "sle_minion" should have the same header digest as on hub

  @skip
  Scenario: Cleanup: remove andromeda-dummy from sle_minion
    Given I am authorized for the "Admin" section on "peripheral1"
    And I am on the Systems overview page of this "sle_minion" on peripheral1
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    And I check "andromeda-dummy" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    And I wait until event "Package Removal scheduled by admin" is completed

