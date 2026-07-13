# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

# This feature covers plan C-02: deploying the Grafana reporting stack on the
# dedicated monitoring server via the Grafana Salt formula in hub reporting mode.
#
# Prerequisites:
# - server2 registered as a peripheral (srv_hub_setup_registration.feature)
# - hub reportdb populated with peripheral data (srv_hub_reporting.feature)
# - monitoring_server bootstrapped as a minion of the hub
#
# This feature can cause failures in:
# - features/secondary/srv_hub_grafana_dashboards.feature
# - features/secondary/srv_hub_grafana_data_validation.feature
# If Grafana formula setup fails, those features will have no Grafana to test.

@scope_hub
@hub_full_topology
@server2
@monitoring_server
Feature: Grafana formula setup in hub reporting mode on the monitoring server (C-02)
  In order to visualize hub reporting data from the monitoring server
  As an authorized user
  I want to enable and configure the Grafana formula in hub mode and verify the deployment

  Scenario: Log in as admin for Grafana formula setup (C-02)
    Given I am authorized for the "Admin" section

  Scenario: Enable Grafana formula on the monitoring system (C-02)
    Given I am on the Systems overview page of this "monitoring_server"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I check the "grafana" formula
    And I click on "Save"
    Then I should see a "Formula saved." text
    And the "grafana" formula should be checked

  Scenario: Configure Grafana formula for hub reporting mode (C-02)
    Given I am on the Systems overview page of this "monitoring_server"
    When I follow "Formulas" in the content area
    And I follow first "Grafana" in the content area
    And I enable Grafana in the formula
    And I set the Grafana admin username in the formula
    And I set the Grafana admin password in the formula
    And I set the Prometheus datasource URL for "monitoring_server"
    And I enable the Report DB datasource in the Grafana formula
    And I enable the hub server mode for the Report DB in the formula
    And I check the "MLM server" Grafana dashboard checkbox
    And I check the "MLM clients" Grafana dashboard checkbox
    And I check the "PostgreSQL" Grafana dashboard checkbox
    And I check the "Apache HTTPD" Grafana dashboard checkbox
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Apply highstate on the monitoring system to deploy Grafana (C-02)
    Given I am on the Systems overview page of this "monitoring_server"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled" is completed

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
