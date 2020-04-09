# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

@centos_minion
Feature: Monitor SUMA environment with Prometheus on a CentOS minion
  In order to monitore SUSE Manager server
  As an authorized user
  I want to enable Prometheus exporters

  Scenario: Pre-requisite: enable Prometheus exporters repository on the CentOS minion
    When I enable repository "tools_pool_repo" on this "ceos_ssh_minion"

  Scenario: Apply Prometheus exporter formulas on the CentOS minion
    Given I am on the Systems overview page of this "ceos_ssh_minion"
    When I follow "Formulas" in the content area
    And I should see a "Choose formulas:" text
    And I should see a "Monitoring" text
    And I check the "prometheus-exporters" formula
    And I click on "Save"
    Then I should see a "Formula saved" text

  Scenario: Configure Prometheus exporter formula on the CentOS minion
    Given I am on the Systems overview page of this "ceos_ssh_minion"
    When I follow "Formulas" in the content area
    And I follow "Prometheus Exporters" in the content area
    And I should see a "Enable and configure Prometheus exporters for managed systems." text
    And I check "node" exporter
    And I check "apache" exporter
    And I check "postgres" exporter
    And I click on "Save"
    Then I should see a "Formula saved" text

  Scenario: Apply highstate for Prometheus exporters on the CentOS minion
    Given I am on the Systems overview page of this "ceos_ssh_minion"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled by admin" is completed

  Scenario: Visit monitoring endpoints on the CentOS minion
    When I visit "Prometheus node exporter" endpoint of this "ceos_ssh_minion"
    And I visit "Prometheus apache exporter" endpoint of this "ceos_ssh_minion"
    And I visit "Prometheus postgres exporter" endpoint of this "ceos_ssh_minion"

  Scenario: Cleanup: undo Prometheus exporter formulas on the CentOS minion
    Given I am on the Systems overview page of this "ceos_ssh_minion"
    When I follow "Formulas" in the content area
    And I uncheck the "prometheus-exporters" formula
    And I click on "Save"
    Then I should see a "Formula saved" text

  Scenario: Cleanup: apply highstate after test monitoring on the CentOS minion
    Given I am on the Systems overview page of this "ceos_ssh_minion"
    And I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled by admin" is completed

  Scenario: Cleanup: disable Prometheus exporters repository on the CentOS minion
    And I disable repository "tools_pool_repo" on this "ceos_ssh_minion" without error control
