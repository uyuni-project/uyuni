# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

# This feature covers plan C-03: verifying that the three hub reporting dashboards
# are auto-provisioned by the Grafana formula and load without errors.
#
# Prerequisites:
# - srv_hub_grafana_setup.feature must have completed successfully
#   (Grafana running, Report DB datasource provisioned)
#
# Dashboards under test:
#   1. SUSE Multi-Linux Manager Fleet Overview & Security
#   2. SUSE Multi-Linux Manager Hub Overview
#   3. SUSE Multi-Linux Manager Reports & History

@scope_hub
@hub_full_topology
@server2
@monitoring_server
Feature: Grafana hub reporting dashboard provisioning verification (C-03)
  In order to confirm hub reporting dashboards are correctly provisioned
  As an authorized user
  I want to verify that Grafana auto-provisions all three hub reporting dashboards in the Reporting folder

  Scenario: Log in as admin for dashboard verification (C-03)
    Given I am authorized for the "Admin" section

  Scenario: Verify the hub fleet overview and security dashboard is provisioned (C-03)
    Then the Grafana Reporting folder should contain the hub fleet overview dashboard on "monitoring_server"

  Scenario: Verify the hub overview dashboard is provisioned (C-03)
    Then the Grafana Reporting folder should contain the hub overview dashboard on "monitoring_server"

  Scenario: Verify the hub reports and history dashboard is provisioned (C-03)
    Then the Grafana Reporting folder should contain the hub reports and history dashboard on "monitoring_server"

  Scenario: Verify each hub reporting dashboard loads without datasource errors (C-03)
    Then each hub reporting dashboard should load without errors on "monitoring_server"

  Scenario: Verify Hub Overview dashboard declares the Report DB datasource (C-03)
    Then the hub overview dashboard should declare the Report DB datasource on "monitoring_server"
