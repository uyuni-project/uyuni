# Copyright (c) 2026 SUSE LLC
# SPDX-License-Identifier: MIT
#
# This feature can cause failures in:
# If reportdb aggregation from peripheral fails:
# - features/hub/srv_hub_grafana_data_validation.feature

@scope_hub
@hub_server_to_server
@peripheral1
Feature: Hub reportdb aggregation from peripheral servers
  In order to get a consolidated view of all managed systems
  As an authorized user
  I want to verify that peripheral reporting data aggregates into the hub reportdb (plan C-01)

  # peripheral1 stays registered after this feature on purpose: srv_hub_verification_cleanup.feature
  # runs next and deregisters it. sle_minion, bootstrapped by srv_hub_minion_on_peripheral.feature,
  # is removed again by the last scenarios of this feature.

  # The hub reportdb only gets populated once a real managed system exists behind a
  # peripheral, so the "one row per peripheral" scenario is skipped unless sle_minion is
  # registered on peripheral1. Without it, "one row per peripheral" would find 0 distinct
  # mgm_id rows instead of the expected >= 2.

  Scenario:  Log in as admin user on hub
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

  @sle_minion
  Scenario: Verify hub reportdb contains one row per peripheral
    Given "sle_minion" is registered on "peripheral1"
    And I am authorized for the "Admin" section on "server"
    When I schedule a task to update ReportDB
    Then the hub reportdb should contain one row per peripheral

  @sle_minion
  Scenario: Verify synced_date is recent in hub reportdb system table
    Given "sle_minion" is registered on "peripheral1"
    Then the hub reportdb "system" table should have a recent synced_date

  @sle_minion
  Scenario: Re-run hub reporting task and verify synced_date refreshes
    Given "sle_minion" is registered on "peripheral1"
    And I am authorized for the "Admin" section on "peripheral1"
    And I schedule the reporting update task on "peripheral1"
    When I remember the latest synced_date of the hub reportdb "system" table
    And I schedule the reporting update task on "hub"
    Then the hub reportdb "system" table should have a newer synced_date than remembered

  @sle_minion
  Scenario: Verify hub reportdb has a system row for sle_minion of peripheral1
    Given "sle_minion" is registered on "peripheral1"
    Then the hub reportdb system table should contain a row for "sle_minion" of "peripheral1"

  # The scenarios below always run and are idempotent: they leave the hub without sle_minion data
  @sle_minion
  Scenario: Cleanup: remove sle_minion from peripheral1
    When I delete "sle_minion" system using the api from "peripheral1"
    And I perform a full salt minion cleanup on "sle_minion"
    Then I should not see "sle_minion" registered on "peripheral1"

  @sle_minion
  Scenario: Cleanup: refresh reporting on peripheral1 after removing sle_minion
    Given I am authorized for the "Admin" section on "peripheral1"
    When I schedule the reporting update task on "peripheral1"
    Then I should see a "FINISHED" text

  @sle_minion
  Scenario: Cleanup: refresh hub reporting after removing sle_minion
    When I schedule the reporting update task on "hub"
    Then I should see a "FINISHED" text

  @sle_minion
  Scenario: Verify sle_minion is gone from the hub reportdb system table
    Then the hub reportdb system table should not contain a row for "sle_minion" of "peripheral1"
