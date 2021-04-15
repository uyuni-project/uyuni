# Copyright (c) 2016-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_salt
Feature: Salt package states

  Scenario: Pre-requisite: install old packages on SLES minion
    Given I am authorized as "admin" with password "admin"
    Then I apply highstate on "sle_minion"
    And I enable repository "test_repo_rpm_pool" on this "sle_minion"
    And I run "zypper -n ref" on "sle_minion"
    And I install old package "milkyway-dummy-1.0" on this "sle_minion" without error control
    And I install old package "virgo-dummy-1.0" on this "sle_minion" without error control
    And I install old package "andromeda-dummy-1.0" on this "sle_minion" without error control

  Scenario: Pre-requisite: refresh package list and check installed packages on SLE minion
    When I refresh packages list via spacecmd on "sle_minion"
    And I wait until refresh package list on "sle_minion" is finished
    Then spacecmd should show packages "milkyway-dummy-1.0 virgo-dummy-1.0 andromeda-dummy-1.0" installed on "sle_minion"

  Scenario: Pre-requisite: ensure the errata cache is computed before software states tests
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button until page does contain "andromeda-dummy-1.0" text
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    Then I click on "Single Run Schedule"
    And I should see a "bunch was scheduled" text
    Then I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Accepted minion has a base channel
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    Then the system should have a base channel set

  Scenario: Remove a package through the UI
    Given I am on the Systems overview page of this "sle_minion"
    Then I follow "States" in the content area
    And I follow "Packages"
    And I follow "Search"
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "milkyway-dummy" text
    And "milkyway-dummy" should be installed on "sle_minion"
    And I change the state of "milkyway-dummy" to "Removed" and ""
    Then I should see a "1 Change" text
    And I click save
    Then I wait until I see "Package states have been saved." text
    And I click apply
    And I wait for "milkyway-dummy" to be uninstalled on "sle_minion"

  Scenario: Install a package through the UI
    Given I am on the Systems overview page of this "sle_minion"
    Then I follow "States" in the content area
    And I follow "Packages"
    And I follow "Search"
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "milkyway-dummy" text
    And "milkyway-dummy" should not be installed on "sle_minion"
    And I change the state of "milkyway-dummy" to "Installed" and ""
    Then I should see a "1 Change" text
    And I click save
    Then I wait until I see "Package states have been saved." text
    And I click apply
    And I wait for "milkyway-dummy" to be installed on "sle_minion"

  Scenario: Install an already installed package through the UI
    Given I am on the Systems overview page of this "sle_minion"
    Then I follow "States" in the content area
    And I follow "Packages"
    And I follow "Search"
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "virgo-dummy" text
    And "virgo-dummy-1.0" should be installed on "sle_minion"
    And I change the state of "virgo-dummy" to "Installed" and "Any"
    Then I should see a "1 Change" text
    And I click save
    Then I wait until I see "Package states have been saved." text
    And I click apply
    And I wait for "virgo-dummy-1.0" to be installed on "sle_minion"

  Scenario: Upgrade a package through the UI
    Given I am on the Systems overview page of this "sle_minion"
    Then I follow "States" in the content area
    And I follow "Packages"
    And I follow "Search"
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "andromeda-dummy" text
    And "andromeda-dummy-1.0" should be installed on "sle_minion"
    And I change the state of "andromeda-dummy" to "Installed" and "Latest"
    Then I should see a "1 Change" text
    And I click save
    Then I wait until I see "Package states have been saved." text
    And I click apply
    And I wait for "andromeda-dummy-2.0-1.1" to be installed on "sle_minion"

  Scenario: Verify the package states
    Given I am on the Systems overview page of this "sle_minion"
    Then I follow "States" in the content area
    And I follow "Packages"
    And I should see a "Package States" text
    And I should see a "milkyway-dummy" text
    And I should see a "andromeda-dummy" text
    And I should see a "virgo-dummy" text

  Scenario: Use Salt presence mechanism on an active minion
    Given I am on the Systems overview page of this "sle_minion"
    Then I follow "States" in the content area
    And I follow "Highstate" in the content area
    And I click on "Show full highstate output"
    And I wait for "6" seconds
    And I should see a "pkg_removed" or "running as PID" text in element "highstate"

  Scenario: Use Salt presence mechanism on an unreachable minion
    Given I am on the Systems overview page of this "sle_minion"
    Then I follow "States" in the content area
    And I run "pkill salt-minion" on "sle_minion"
    And I follow "Highstate" in the content area
    And I click on "Show full highstate output"
    And I wait until I see "No reply from minion" text

  Scenario: Cleanup: restart the salt service on SLES minion
    When I run "rcsalt-minion restart" on "sle_minion"

  Scenario: Cleanup: remove old packages from SLES minion
    When I disable repository "test_repo_rpm_pool" on this "sle_minion"
    And I remove package "milkyway-dummy" from this "sle_minion" without error control
    And I remove package "virgo-dummy" from this "sle_minion" without error control
    And I remove package "andromeda-dummy" from this "sle_minion" without error control
    And I run "zypper -n ref" on "sle_minion"
