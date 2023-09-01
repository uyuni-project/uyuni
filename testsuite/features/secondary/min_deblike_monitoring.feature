# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.
# This feature depends on:
# - features/secondary/srv_monitoring.feature: as this feature disables/re-enables monitoring capabilities
# - sumaform: as it is configuring monitoring to be enabled after deployment

@skip_if_github_validation
@scope_monitoring
@scope_res
@deblike_minion
Feature: Monitor SUMA environment with Prometheus on a Debian-like Salt minion
  In order to monitor Uyuni server
  As an authorized user
  I want to enable Prometheus exporters

  Scenario: Pre-requisite: enable Prometheus exporters repository on the Debian-like minion
    When I enable the necessary repositories before installing Prometheus exporters on this "deblike_minion"

  Scenario: Log in as org admin user
    Given I am authorized

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

  Scenario: Apply highstate for Prometheus exporters on the Debian-like minion
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled" is completed

  Scenario: Visit monitoring endpoints on the Debian-like minion
    When I wait until "node" exporter service is active on "deblike_minion"
    And I visit "Prometheus node exporter" endpoint of this "deblike_minion"
    And I wait until "apache" exporter service is active on "deblike_minion"
    And I visit "Prometheus apache exporter" endpoint of this "deblike_minion"
    And I wait until "postgres" exporter service is active on "deblike_minion"
    And I visit "Prometheus postgres exporter" endpoint of this "deblike_minion"

  Scenario: Cleanup: undo Prometheus exporter formulas on the Debian-like minion
    When I follow "Formulas" in the content area
    And I uncheck the "prometheus-exporters" formula
    And I click on "Save"
    Then I wait until I see "Formula saved" text

  Scenario: Cleanup: apply highstate after test monitoring on the Debian-like minion
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled" is completed

  Scenario: Cleanup: disable Prometheus exporters repository on the Debian-like minion
    When I disable the necessary repositories before installing Prometheus exporters on this "deblike_minion" without error control
