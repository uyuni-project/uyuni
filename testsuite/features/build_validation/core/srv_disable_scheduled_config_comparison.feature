# Copyright (c) 2026 SUSE LLC
# SPDX-License-Identifier: MIT

Feature: Do not let the nightly configuration comparison interfere with our BV tests

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Disable the scheduled comparison of configuration files
    When I follow the left menu "Admin > Task Schedules"
    And I follow "compare-configs-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    And I click on "Disable Schedule"
