# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

# This feature covers plan C-02: deploying the Grafana reporting stack on the
# dedicated monitoring server via the Grafana Salt formula in hub reporting mode.
#
# Prerequisites:
# - peripheral1 registered as a peripheral (srv_hub_setup_registration.feature)
# - hub reportdb populated with peripheral data (srv_hub_reporting.feature)
# - monitoring_server bootstrapped as a minion of the hub
#
# This feature can cause failures in:
# - features/secondary/srv_hub_grafana_dashboards.feature
# - features/secondary/srv_hub_grafana_data_validation.feature
# If Grafana formula setup fails, those features will have no Grafana to test.

@scope_hub
@hub_full_topology
@peripheral1
@monitoring_server
Feature: Grafana formula setup in hub reporting mode on the monitoring server (C-02)
  In order to visualize hub reporting data from the monitoring server
  As an authorized user
  I want to enable and configure the Grafana formula in hub mode and verify the deployment

  Scenario: Log in as admin for Grafana formula setup (C-02)
    Given I am authorized for the "Admin" section

  Scenario: Configure Grafana formula for hub reporting mode (C-02)
    Given I am on the Systems overview page of this "monitoring_server"
    When I follow "Formulas" in the content area
    When I follow "Grafana" in the content area
    And I click on "Expand All Sections"
    And I enable the Report DB datasource in the Grafana formula
    And I enable the hub server mode for the Report DB in the formula
    And I click on "Save Formula"
    When I follow "States" in the content area
    And I store the current last event id for "monitoring_server"
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until a new "Apply highstate" event is completed for "monitoring_server"
    # Visit monitoring endpoints on the minion
    When I wait until "grafana-server" service is active on "monitoring_server"
    And I visit "Grafana" endpoint of this "monitoring_server"

  Scenario: Verify grafana-server service is active on the monitoring node (C-02)
    Then the "grafana-server" service should be active on "monitoring_server"

  Scenario: Verify Grafana API health endpoint reports database ok (C-02)
    Then the Grafana API health endpoint should report database ok on "monitoring_server"

  Scenario: Verify Report DB datasource is provisioned and targets the hub reportdb (C-02)
    Then the Grafana Report DB datasource should target the hub reportdb on "monitoring_server"

  Scenario: Verify highstate is idempotent and produces no duplicate datasources (C-02)
    Given I am on the Systems overview page of this "monitoring_server"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled" is completed
    And there should be exactly one Grafana Report DB datasource on "monitoring_server"
