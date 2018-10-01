# COPYRIGHT (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Reboot required after patch
  In order to avoid systems with different running/installed kernel
  As an authorized user
  I want to see systems that need a reboot

  Scenario: Check requiring reboot in the web UI
    Given I am authorized
    And I follow "Home" in the left menu
    And I follow "Systems" in the left menu
    And I follow "Overview" in the left menu
    When I click System List, under Systems node
    Then I should see a "All" link in the left menu
    And  I follow "All" in the left menu
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
    And I follow "Admin"
    And I follow "Task Schedules"
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
    And I follow "Software" in the left menu
    And I click System List, under Systems node
    And I follow "All" in the left menu
    And I follow this "sle-client" link
    Then I should see a "The system requires a reboot" text
    And I follow "Software" in the left menu
    And I click System List, under Systems node
    And I follow "Requiring Reboot" in the left menu
    Then I should see "sle-client" as link

  Scenario: Cleanup: remove packages and restore non-update repo after needing reboot tests
    And I run "zypper -n rm andromeda-dummy" on "sle-client"
    And I run "zypper -n mr -d Devel_Galaxy_BuildRepo" on "sle-client"
