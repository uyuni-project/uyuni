# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.
# This feature depends on:
# - features/secondary/srv_monitoring.feature : As this feature disable/re-enable monitoring capabilities
# - sumaform : As it is configuring monitoring to be enabled after deployment

@sle_minion
@scope_monitoring
Feature: Monitor SUMA environment with Prometheus on a SLE Salt minion
  In order to monitor Uyuni server
  As an authorized user
  I want to enable Prometheus exporters

  Scenario: Pre-requisite: enable Prometheus exporters repository on the minion
    When I enable the necessary repositories before installing Prometheus exporters on this "sle_minion"
    And I refresh the metadata for "sle_minion"


  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Apply Prometheus and Prometheus exporter formulas
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Formulas" in the content area
    And I should see a "Choose formulas:" text
    And I should see a "Monitoring" text
    And I check the "prometheus" formula
    And I check the "prometheus-exporters" formula
    And I click on "Save"
    Then I wait until I see "Formula saved" text

  Scenario: Configure Prometheus formula
    When I follow "Formulas" in the content area
    And I follow "Prometheus" in the content area
    And I click on "Expand All Sections"
    And I enter "admin" as "Username"
    And I enter "admin" as "Password"
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Configure Prometheus exporter formula
    When I follow "Formulas" in the content area
    And I follow "Prometheus Exporters" in the content area
    And I click on "Expand All Sections"
    And I should see a "Enable and configure Prometheus exporters for managed systems." text
    And I check "node" exporter
    And I check "apache" exporter
    And I check "postgres" exporter
    And I click on "Save"
    Then I should see a "Formula saved" text

  Scenario: Apply highstate for Prometheus exporters
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled by admin" is completed

  Scenario: Visit monitoring endpoints on the minion
    When I wait until "prometheus" service is active on "sle_minion"
    And I visit "Prometheus" endpoint of this "sle_minion"
    And I wait until "node" exporter service is active on "sle_minion"
    And I visit "Prometheus node exporter" endpoint of this "sle_minion"
    And I wait until "apache" exporter service is active on "sle_minion"
    And I visit "Prometheus apache exporter" endpoint of this "sle_minion"
    And I wait until "postgres" exporter service is active on "sle_minion"
    And I visit "Prometheus postgres exporter" endpoint of this "sle_minion"

  Scenario: Cleanup: undo Prometheus and Prometheus exporter formulas
    When I follow "Formulas" in the content area
    And I uncheck the "prometheus" formula
    And I uncheck the "prometheus-exporters" formula
    And I click on "Save"
    Then I wait until I see "Formula saved" text

  Scenario: Cleanup: apply highstate after test monitoring
    And I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled by admin" is completed

  Scenario: Cleanup: disable Prometheus exporters repository
    When I disable the necessary repositories before installing Prometheus exporters on this "sle_minion" without error control
