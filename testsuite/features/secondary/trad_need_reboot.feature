# Copyright (c) 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.

# TODO: This feature must run before install a patch in the client
# Feature dependency: trad_action_chain.feature

@scope_traditional_client
Feature: Reboot required after patch
  In order to avoid systems with different running/installed kernel
  As an authorized user
  I want to see systems that need a reboot

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Check requiring reboot link in the web UI
    When I follow the left menu "Systems > System List"
    Then I should see a "Requiring Reboot" link in the left menu

  Scenario: No reboot notice if no need to reboot
    Given I am on the Systems overview page of this "sle_client"
    Then I should not see a "The system requires a reboot" text

  Scenario: Enable old packages to test a "needing reboot"
    When I enable repository "test_repo_rpm_pool" on this "sle_client"
    And I run "zypper -n ref" on "sle_client"
    And I install old package "andromeda-dummy-1.0" on this "sle_client"
    And I run "rhn_check -vvv" on "sle_client"
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Display reboot required after installing a patch
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    And I check "andromeda-dummy-6789" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    And I run "rhn_check -vvv" on "sle_client"
    When I follow the left menu "Systems > System List > All"
    And I follow this "sle_client" link
    Then I should see a "The system requires a reboot" text
    When I follow the left menu "Systems > System List > Requiring Reboot"
    Then I should see "sle_client" as link

  Scenario: Cleanup: remove packages and restore non-update repo after needing reboot tests
    When I remove package "andromeda-dummy" from this "sle_client"
    And I disable repository "test_repo_rpm_pool" on this "sle_client"
