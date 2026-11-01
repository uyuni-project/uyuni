# Copyright (c) 2019-2025 SUSE LLC
# SPDX-License-Identifier: MIT

Feature: Do not let Taskomatic tasks interfere with our BV tests

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Disable scheduled reposyncs
    When I follow the left menu "Admin > Task Schedules"
    And I follow "mgr-sync-refresh-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    And I click on "Disable Schedule"

  Scenario: Disable scheduled Cobbler syncs
    When I follow the left menu "Admin > Task Schedules"
    And I follow "cobbler-sync-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    And I click on "Disable Schedule"
