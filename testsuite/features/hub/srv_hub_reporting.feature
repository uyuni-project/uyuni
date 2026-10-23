# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in:
# If reportdb aggregation from peripheral fails:
# - features/hub/srv_hub_grafana_data_validation.feature

@scope_hub
@hub_full_topology
@peripheral1
Feature: Hub reportdb aggregation from peripheral servers
  In order to get a consolidated view of all managed systems
  As an authorized user
  I want to verify that peripheral reporting data aggregates into the hub reportdb (plan C-01)

  # peripheral1 stays registered after this feature on purpose: the following features need it
  # registered plus this feature's reportdb data. In hub_full_topology.yml the outage feature
  # re-checks the registration and deregisters it at the end (the Grafana features that would
  # otherwise do the final deregistration are commented out there).

  # The hub reportdb only gets populated once a real managed system exists behind a
  # peripheral, so the two scenarios below require sle_minion to already be bootstrapped
  # onto peripheral1 (see run_sets/hub_full_topology.yml, where srv_hub_minion_on_peripheral.feature
  # now runs earlier in the run set for exactly this reason). Without it, "one row per
  # peripheral" finds 0 distinct mgm_id rows instead of the expected >= 2.

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: peripheral1 is registered as peripheral for reporting tests
    When I follow the left menu "Admin > Hub Configuration > Peripherals Configuration"
    Then I should see "peripheral1" hostname

  Scenario: Trigger reporting update on peripheral1 peripheral
    Given I am authorized for the "Admin" section on "peripheral1"
    And I set the admin page size to "100" on "peripheral1"
    When I schedule the reporting update task on "peripheral1"
    Then I should see a "FINISHED" text

  Scenario: Trigger hub reporting aggregation task
    When I schedule the reporting update task on "hub"
    Then I should see a "FINISHED" text

  Scenario: Verify hub reportdb contains one row per peripheral
    Then the hub reportdb should contain one row per peripheral

  Scenario: Verify synced_date is recent in hub reportdb system table
    Then the hub reportdb "system" table should have a recent synced_date

  Scenario: Re-run hub reporting task and verify synced_date refreshes
    When I remember the latest synced_date of the hub reportdb "system" table
    And I schedule the reporting update task on "hub"
    Then the hub reportdb "system" table should have a newer synced_date than remembered
