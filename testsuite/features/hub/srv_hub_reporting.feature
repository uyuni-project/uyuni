# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@server2
Feature: Hub reportdb aggregation from peripheral servers
  In order to get a consolidated view of all managed systems
  As an authorized user
  I want to verify that peripheral reporting data aggregates into the hub reportdb (plan C-01)

  # server2 stays registered after this feature on purpose: srv_hub_grafana_setup.feature
  # (runs next) needs it registered plus this feature's reportdb data. Final deregistration
  # for this stretch of the run set happens in srv_hub_grafana_data_validation.feature instead.

  # The hub reportdb only gets populated once a real managed system exists behind a
  # peripheral, so the two scenarios below require sle_minion to already be bootstrapped
  # onto server2 (see run_sets/hub_full_topology.yml, where srv_hub_minion_on_peripheral.feature
  # now runs earlier in the run set for exactly this reason). Without it, "one row per
  # peripheral" finds 0 distinct mgm_id rows instead of the expected >= 2.

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Prerequisite - server2 is registered as peripheral for reporting tests (C-01)
    When I follow the left menu "Admin > Hub Configuration > Peripherals Configuration"
    Then I should see the name of "server2"

  Scenario: Trigger reporting update on server2 peripheral (C-01)
    Given I am authorized for the "Admin" section on "server2"
    And I set the admin page size to "100" on "server2"
    When I schedule the reporting update task on "server2"
    Then I should see a "FINISHED" text

  Scenario: Trigger hub reporting aggregation task (C-01)
    When I schedule the reporting update task on "server"
    Then I should see a "FINISHED" text

  Scenario: Verify hub reportdb contains one row per peripheral (C-01)
    Then the hub reportdb should contain one row per peripheral

  Scenario: Verify synced_date is recent in hub reportdb system table (C-01)
    Then the hub reportdb "system" table should have a recent synced_date

  Scenario: Re-run hub reporting task and verify synced_date refreshes (C-01)
    When I schedule the reporting update task on "server"
    Then the hub reportdb "system" table should have a recent synced_date
