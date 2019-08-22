# COPYRIGHT (c) 2017-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Reboot required after patch
  In order to avoid systems with different running/installed kernel
  As an authorized user
  I want to see systems that need a reboot

  Scenario: Check requiring reboot link in the web UI
    Given I am authorized
    When I follow the left menu "Systems > System List"
    Then I should see a "Requiring Reboot" link in the left menu

  Scenario: No reboot notice if no need to reboot
    Given I am on the Systems overview page of this "sle-client"
    Then I should not see a "The system requires a reboot" text

  Scenario: Enable old packages to test a "needing reboot"
    Given I am authorized as "admin" with password "admin"
    When I run "zypper -n mr -e Devel_Galaxy_BuildRepo" on "sle-client"
    And I run "zypper -n ref" on "sle-client"
    And I run "zypper -n in --oldpackage andromeda-dummy-1.0" on "sle-client"
    And I run "rhn_check -vvv" on "sle-client"
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Display reboot required after installing a patch
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    And I check "andromeda-dummy-6789" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    And I run "rhn_check -vvv" on "sle-client"
    When I follow the left menu "Systems > System List > All"
    And I follow this "sle-client" link
    Then I should see a "The system requires a reboot" text
    When I follow the left menu "Systems > System List > Requiring Reboot"
    Then I should see "sle-client" as link

  Scenario: Cleanup: remove packages and restore non-update repo after needing reboot tests
    When I run "zypper -n rm andromeda-dummy" on "sle-client"
    And I run "zypper -n mr -d Devel_Galaxy_BuildRepo" on "sle-client"
