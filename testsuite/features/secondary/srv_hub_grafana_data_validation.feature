# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

# This feature covers plans C-04, C-05, and C-06 (pragmatic automated subset):
# numeric cross-checks between Grafana dashboard data and hub reportdb SQL queries.
#
# Full pixel-level panel validation is manual (see "Left manual" section below).
# Left manual:
#   - C-04: pie/panel visual parity, organisation distribution charts, SCAP compliance %
#   - C-05: deregistration dynamics (covered manually; ordering prevents automation here)
#   - C-06: SCAP / history panels without data, OpenSCAP scan history
#   - C-07: single-server mode (out of scope for this pass)
#
# Prerequisites:
# - srv_hub_grafana_setup.feature and srv_hub_grafana_dashboards.feature completed
# - At least one peripheral registered (server2) with reporting data in hub reportdb
# - monitoring_server is a hub minion (for the C-06 action trigger)
#
# Cleanup: this feature disables the Grafana formula and applies highstate at the end,
# restoring pre-suite state. Run AFTER srv_hub_grafana_dashboards.feature and BEFORE
# srv_hub_verification_cleanup.feature.

@scope_hub
@hub_full_topology
@server2
@monitoring_server
Feature: Grafana hub reporting data cross-validation and cleanup (C-04, C-05, C-06)
  In order to confirm Grafana reporting dashboards reflect accurate hub reporting data
  As an authorized user
  I want to cross-check Grafana panel queries against hub reportdb SQL and then remove the Grafana setup

  Scenario: Log in as admin for data validation (C-04)
    Given I am authorized for the "Admin" section

  Scenario: Fleet Overview total systems count matches hub reportdb (C-04)
    Then the Grafana fleet overview total systems panel should match the reportdb system count on "monitoring_server"

  Scenario: Fleet Overview systems-by-organization distribution sums to total systems (C-04)
    Then the Fleet Overview systems-by-organization distribution should sum to the total system count on "monitoring_server"

  Scenario: Fleet Overview channel and outstanding patch panels return non-null values (C-04)
    Then the Grafana fleet overview channel and patch panels should return non-null values on "monitoring_server"

  Scenario: Hub Overview peripheral count matches registered peripherals in reportdb (C-05)
    Then the Grafana hub overview peripheral count should match the number of registered peripherals on "monitoring_server"

  Scenario: Hub Overview per-peripheral table has one row per peripheral in reportdb (C-05)
    Then the Grafana hub overview per-peripheral table should have one row per registered peripheral on "monitoring_server"

  Scenario: Hub Overview system inventory contains hub-managed and peripheral-managed entries (C-05)
    Then the Grafana hub overview system inventory should contain entries managed by the hub and by peripherals on "monitoring_server"

  Scenario: Trigger fresh highstate action on monitoring server for C-06 validation (C-06)
    When I trigger a fresh highstate action on "monitoring_server"

  Scenario: Run hub reporting aggregation task after fresh action (C-06)
    When I schedule the reporting update task on "server"
    Then I should see a "FINISHED" text

  Scenario: Reportdb latest actions include the recent highstate action (C-06)
    Then the hub reportdb latest actions should include a recent action for "monitoring_server"

  Scenario: Reportdb user accounts table includes the admin user (C-06)
    Then the hub reportdb user accounts table should include the admin user

  Scenario: Cleanup - disable Grafana formula on the monitoring system
    Given I am on the Systems overview page of this "monitoring_server"
    When I follow "Formulas" in the content area
    And I uncheck the "grafana" formula
    And I click on "Save"
    Then I should see a "Formula saved." text
    And the "grafana" formula should be unchecked

  Scenario: Cleanup - apply highstate to stop Grafana and restore pre-suite state
    Given I am on the Systems overview page of this "monitoring_server"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled" is completed
    And the "grafana-server" service should be stopped on "monitoring_server"
