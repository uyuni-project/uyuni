# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

# This feature covers plans C-04, C-05, and C-06 (pragmatic automated subset):
# sanity checks on the hub reportdb data that feeds the Grafana dashboards.
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
# - At least one peripheral registered (peripheral1) with reporting data in hub reportdb
# - monitoring_server is a hub minion (for the C-06 action trigger)
#
# Cleanup: this feature disables the Grafana formula and applies highstate at the end,
# restoring pre-suite state. Run AFTER srv_hub_grafana_dashboards.feature and BEFORE
# srv_hub_verification_cleanup.feature.
#
# This feature also owns final peripheral1 deregistration for the reporting+Grafana stretch
# of the run set (moved here from srv_hub_reporting.feature, since srv_hub_grafana_setup.feature
# needed peripheral1 to stay registered through that stretch). srv_hub_verification_cleanup.feature,
# which runs next, does its own fresh peripheral registration and would hit a duplicate-
# registration error if peripheral1 were still registered when it starts.

@scope_hub
@hub_full_topology
@peripheral1
@monitoring_server
Feature: Grafana hub reporting data cross-validation and cleanup
  In order to confirm Grafana reporting dashboards reflect accurate hub reporting data
  As an authorized user
  I want to check the hub reportdb data behind the Grafana dashboards and then remove the Grafana setup

  Scenario: Log in as admin for data validation
    Given I am authorized for the "Admin" section

  Scenario: Hub reportdb contains systems
    Then the hub reportdb system count should be positive

  Scenario: Hub reportdb contains channels
    Then the hub reportdb channel count should be positive

  Scenario: Hub reportdb contains systems from at least one peripheral
    Then the hub reportdb should contain systems from at least one peripheral

  Scenario: Hub reportdb contains hub-managed and peripheral-managed systems
    Then the hub reportdb should contain systems managed by the hub and by peripherals

  Scenario: Trigger fresh highstate action on monitoring server for C-06 validation
    When I store the current last event id for "monitoring_server"
    And I schedule a highstate for "monitoring_server" via API
    And I wait until a new "Apply highstate" event is completed for "monitoring_server"

  Scenario: Run hub reporting aggregation task after fresh action
    When I schedule the reporting update task on "hub"
    Then I should see a "FINISHED" text

  Scenario: Reportdb latest actions include the recent highstate action
    Then the hub reportdb latest actions should include a recent action for "monitoring_server"

  Scenario: Reportdb user accounts table includes the admin user
    Then the hub reportdb user accounts table should include the admin user

  Scenario: Cleanup: disable Grafana formula on the monitoring system
    Given I am on the Systems overview page of this "monitoring_server"
    When I follow "Formulas" in the content area
    And I uncheck the "grafana" formula
    And I click on "Save"
    Then I should see a "Formula saved." text
    And the "grafana" formula should be unchecked

  Scenario: Cleanup: apply highstate to stop Grafana and restore pre-suite state
    Given I am on the Systems overview page of this "monitoring_server"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled" is completed
    And I wait until "grafana-server" service is inactive on "monitoring_server"

  Scenario: Cleanup: deregister peripheral1 from hub
    When I unregister "peripheral1" from hub
    Then I should not see "peripheral1" hostname
