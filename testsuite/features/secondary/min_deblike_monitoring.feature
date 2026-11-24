# Copyright (c) 2022-2026 SUSE LLC
# SPDX-License-Identifier: MIT
# This feature depends on:
# - features/secondary/srv_monitoring.feature: as this feature disables/re-enables monitoring capabilities
# - sumaform: as it is configuring monitoring to be enabled after deployment

@scope_monitoring
@scope_res
@scope_deblike
@deblike_minion
Feature: Monitor MLM environment with Prometheus on a Debian-like Salt minion
  In order to monitor Uyuni server
  As an authorized user
  I want to enable Prometheus exporters

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Pre-requisite: Enable all the necessary repositories for Monitoring on Debian-like minion
    When I enable Debian-like "universe" repository on "deblike_minion"

  Scenario: Apply Prometheus exporter formulas on the Debian-like minion
    Given I am on the Systems overview page of this "deblike_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    And I should see a "Monitoring" text
    When I check the "prometheus-exporters" formula
    And I click on "Save"
    And I wait until I see "Formula saved" text

  Scenario: Configure Prometheus exporter formula on the Debian-like minion
    When I follow "Formulas" in the content area
    And I follow "Prometheus Exporters" in the content area
    And I click on "Expand All Sections"
    Then I should see a "Enable and configure Prometheus exporters for managed systems." text
    When I check "node" exporter
    And I check "apache" exporter
    And I check "postgres" exporter
    And I click on "Save"
    Then I should see a "Formula saved" text

@skip_if_github_validation
  Scenario: Apply highstate for Prometheus exporters on the Debian-like minion
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled" is completed

@skip_if_github_validation
  Scenario: Wait for services
     When I wait until "node" exporter service is active on "deblike_minion"
     And I wait until "apache" exporter service is active on "deblike_minion"
     And I wait until "postgres" exporter service is active on "deblike_minion"

@skip_if_github_validation
  Scenario: Verify Prometheus exporter packages are installed from client tools
    Then the "prometheus-node-exporter" package should be installed from the client tools channel on "deblike_minion"

@monitoring_server
@skip_if_github_validation
  Scenario: Verify Prometheus exporter targets and metrics on the Debian-like minion
    Then the Prometheus target "node_exporter" on "deblike_minion" should be up on "monitoring_server"
    And the Prometheus target "apache_exporter" on "deblike_minion" should be up on "monitoring_server"
    And the Prometheus target "postgres_exporter" on "deblike_minion" should be up on "monitoring_server"
    And the Prometheus metric "node_boot_time_seconds" for "deblike_minion" should have a positive value on "monitoring_server"
    And the Prometheus metric "apache_up" for "deblike_minion" should equal "1" on "monitoring_server"
    And the Prometheus metric "pg_up" for "deblike_minion" should equal "1" on "monitoring_server"

  Scenario: Cleanup: undo Prometheus exporter formulas on the Debian-like minion
    When I follow "Formulas" in the content area
    And I uncheck the "prometheus-exporters" formula
    And I click on "Save"
    Then I wait until I see "Formula saved" text

@skip_if_github_validation
  Scenario: Cleanup: apply highstate after test monitoring on the Debian-like minion
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled" is completed

  Scenario: Cleanup: Disable all the necessary repositories for Monitoring on Debian-like minion
    When I disable Debian-like "universe" repository on "deblike_minion"
