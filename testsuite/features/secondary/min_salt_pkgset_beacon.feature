# Copyright (c) 2016-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_salt
Feature: System package list is updated if packages are manually installed or removed

  Scenario: Pre-requisite: install milkyway-dummy-1.0 package
    When I enable repository "test_repo_rpm_pool" on this "sle_minion"
    And I run "zypper -n ref" on "sle_minion"
    And I install old package "milkyway-dummy-1.0" on this "sle_minion" without error control

  Scenario: Pre-requisite: refresh package list and check installed packages on SLE minion client
    When I refresh packages list via spacecmd on "sle_minion"
    And I wait until refresh package list on "sle_minion" is finished
    Then spacecmd should show packages "milkyway-dummy-1.0" installed on "sle_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: ensure the errata cache is computed before package list tests
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "milkyway-dummy" as the filtered package name
    And I click on the filter button until page does contain "milkyway-dummy-1.0" text
    And I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    When I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Remove manually a package on a minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "List / Remove"
    And I enter "milkyway-dummy" as the filtered package name
    And I click on the filter button
    Then I should see a "milkyway-dummy" text
    When I remove package "milkyway-dummy" from this "sle_minion"
    And I click on the filter button until page does not contain "milkyway-dummy" text

  Scenario: Install manually a package on a minion
    When I follow "Software" in the content area
    And I follow "List / Remove"
    And I enter "milkyway-dummy" as the filtered package name
    And I click on the filter button
    Then I should not see a "milkyway-dummy" text
    When I install package "milkyway-dummy" on this "sle_minion"
    And I click on the filter button until page does not contain "milkyway-dummy" text

  Scenario: Cleanup: remove milkyway-dummy packages from SLES minion
    When I disable repository "test_repo_rpm_pool" on this "sle_minion"
    And I remove package "milkyway-dummy" from this "sle_minion" without error control
    And I run "zypper -n ref" on "sle_minion"
