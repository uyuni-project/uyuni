# Copyright (c) 2022-2023 SUSE LLC
# Licensed under the terms of the MIT license.

@monitoring_server
Feature: Bootstrap the monitoring server

  Scenario: Clean up sumaform leftovers on the monitoring server
    When I perform a full salt minion cleanup on "monitoring_server"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap the monitoring server
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "monitoring_server" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-monitoring_server_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "monitoring_server"

  Scenario: Check the new bootstrapped monitoring server in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    And the Salt master can reach "monitoring_server"

@proxy
  Scenario: Check connection from monitoring server to proxy
    Given I am on the Systems overview page of this "monitoring_server"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of monitoring server
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "monitoring_server" hostname

  Scenario: Check events history for failures on monitoring server
    Given I am on the Systems overview page of this "monitoring_server"
    Then I check for failed events on history event page

  Scenario: Test Prometheus formula on monitoring server
    Given I am on the Systems overview page of this "monitoring_server"
    When I follow "Formulas" in the content area
    And I check the "prometheus" formula
    And I click on "Save"
    Then I wait until I see "Formula saved" text
    When I follow "Prometheus" in the content area
    And I click on "Expand All Sections"
    And I enter "admin" as "Username"
    And I enter "admin" as "Password"
    And I check the blackbox exporter
    And I click on "Save Formula"
    Then I should see a "Formula saved" text
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled by admin" is completed
    # Visit monitoring endpoints on the minion
    When I wait until "prometheus" service is active on "monitoring_server"
    And I visit "Prometheus" endpoint of this "monitoring_server"

  Scenario: Test Grafana formula on monitoring server
    Given I am on the Systems overview page of this "monitoring_server"
    When I follow "Formulas" in the content area
    And I check the "grafana" formula
    And I click on "Save"
    Then I wait until I see "Formula saved" text
    When I follow "Grafana" in the content area
    And I click on "Expand All Sections"
    And I enter the "monitoring_server" hostname as the Prometheus URL
    And I click on "Save Formula"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled by admin" is completed
    # Visit monitoring endpoints on the minion
    When I wait until "grafana-server" service is active on "monitoring_server"
    And I visit "Grafana" endpoint of this "monitoring_server"

@susemanager
  Scenario: Test Grafana dashboards of monitoring server
    When I visit the grafana dashboards of this "monitoring_server"
    And I wait until I do not see "Loading Grafana" text
    And I check radio button "View as list", if not checked
    # These are the 4 dashboards created by default when enabling the Grafana formula
    Then I should see a "Apache2" text
    And I should see a "PostgreSQL database insights" text
    And I should see a "SUSE Manager Client Systems" text
    And I should see a "SUSE Manager Server" text

@uyuni
  Scenario: Test Grafana dashboards of monitoring server
    When I visit the grafana dashboards of this "monitoring_server"
    And I wait until I do not see "Loading Grafana" text
    And I check radio button "View as list", if not checked
    # These are the 4 dashboards created by default when enabling the Grafana formula
    Then I should see a "Apache2" text
    And I should see a "PostgreSQL database insights" text
    And I should see a "Uyuni Client Systems" text
    And I should see a "Uyuni Server" text
