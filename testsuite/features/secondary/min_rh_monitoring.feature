# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.
# This feature depends on:
# - features/secondary/srv_monitoring.feature : As this feature disable/re-enable monitoring capabilities
# - sumaform : As it is configuring monitoring to be enabled after deployment

@scope_res
@scope_monitoring
@rh_minion
Feature: Monitor SUMA environment with Prometheus on a RedHat-like Salt minion
  In order to monitor Uyuni server
  As an authorized user
  I want to enable Prometheus exporters

  Scenario: Pre-requisite: enable Prometheus exporters repository on the RedHat-like minion
    When I enable the necessary repositories before installing Prometheus exporters on this "rh_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Apply Prometheus exporter formulas on the RedHat-like minion
    Given I am on the Systems overview page of this "rh_minion"
    When I follow "Formulas" in the content area
    And I should see a "Choose formulas:" text
    And I should see a "Monitoring" text
    And I check the "prometheus-exporters" formula
    And I click on "Save"
    Then I wait until I see "Formula saved" text

  Scenario: Configure Prometheus exporter formula on the RedHat-like minion
    When I follow "Formulas" in the content area
    And I follow "Prometheus Exporters" in the content area
    And I click on "Expand All Sections"
    And I should see a "Enable and configure Prometheus exporters for managed systems." text
    And I check "node" exporter
    And I check "apache" exporter
    And I check "postgres" exporter
    And I click on "Save"
    Then I should see a "Formula saved" text

  Scenario: Apply highstate for Prometheus exporters on the RedHat-like minion
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled by admin" is completed

  Scenario: Visit monitoring endpoints on the RedHat-like minion
    When I wait until "prometheus-node_exporter" service is active on "rh_minion"
    And I visit "Prometheus node exporter" endpoint of this "rh_minion"
    And I wait until "prometheus-apache_exporter" service is active on "rh_minion"
    And I visit "Prometheus apache exporter" endpoint of this "rh_minion"
    And I wait until "prometheus-postgres_exporter" service is active on "rh_minion"
    And I visit "Prometheus postgres exporter" endpoint of this "rh_minion"

  Scenario: Cleanup: undo Prometheus exporter formulas on the RedHat-like minion
    When I follow "Formulas" in the content area
    And I uncheck the "prometheus-exporters" formula
    And I click on "Save"
    Then I wait until I see "Formula saved" text

  Scenario: Cleanup: apply highstate after test monitoring on the RedHat-like minion
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled by admin" is completed

  Scenario: Cleanup: disable Prometheus exporters repository on the RedHat-like minion
    When I disable the necessary repositories before installing Prometheus exporters on this "rh_minion" without error control
