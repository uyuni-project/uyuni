# Copyright (c) 2016-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Salt package states

  Scenario: Pre-requisite: install old packages on SLES minion
    Given I am authorized as "admin" with password "admin"
    Then I apply highstate on "sle-minion"
    And I run "zypper -n mr -e Devel_Galaxy_BuildRepo" on "sle-minion"
    And I run "zypper -n ref" on "sle-minion"
    And I run "zypper -n in --oldpackage milkyway-dummy-1.0" on "sle-minion" without error control
    And I run "zypper -n in --oldpackage virgo-dummy-1.0" on "sle-minion" without error control
    And I run "zypper -n in --oldpackage andromeda-dummy-1.0" on "sle-minion" without error control

  Scenario: Pre-requisite: refresh package list and check installed packages on SLE minion
    When I refresh packages list via spacecmd on "sle-minion"
    And I wait until refresh package list on "sle-minion" is finished
    Then spacecmd should show packages "milkyway-dummy-1.0 virgo-dummy-1.0 andromeda-dummy-1.0" installed on "sle-minion"

  Scenario: Pre-requisite: ensure the errata cache is computed
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" in the css "input[placeholder='Filter by Package Name: ']"
    And I click on the css "button.spacewalk-button-filter" until page does contain "andromeda-dummy-1.0" text
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    Then I click on "Single Run Schedule"
    And I should see a "bunch was scheduled" text
    Then I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Accepted minion has a base channel
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    Then the system should have a base channel set

  Scenario: Remove a package through the UI
    Given I am on the Systems overview page of this "sle-minion"
    Then I follow "States" in the content area
    And I follow "Packages"
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "milkyway-dummy" text
    And "milkyway-dummy" should be installed on "sle-minion"
    And I change the state of "milkyway-dummy" to "Removed" and ""
    Then I should see a "1 Changes" text
    And I click save
    Then I wait until I see "Package states have been saved." text
    And I click apply
    And I wait for "milkyway-dummy" to be uninstalled on "sle-minion"

  Scenario: Install a package through the UI
    Given I am on the Systems overview page of this "sle-minion"
    Then I follow "States" in the content area
    And I follow "Packages"
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "milkyway-dummy" text
    And "milkyway-dummy" should not be installed on "sle-minion"
    And I change the state of "milkyway-dummy" to "Installed" and ""
    Then I should see a "1 Changes" text
    And I click save
    Then I wait until I see "Package states have been saved." text
    And I click apply
    And I wait for "milkyway-dummy" to be installed on this "sle-minion"

  Scenario: Install an already installed package through the UI
    Given I am on the Systems overview page of this "sle-minion"
    Then I follow "States" in the content area
    And I follow "Packages"
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "virgo-dummy" text
    And "virgo-dummy-1.0" should be installed on "sle-minion"
    And I change the state of "virgo-dummy" to "Installed" and "Any"
    Then I should see a "1 Changes" text
    And I click save
    Then I wait until I see "Package states have been saved." text
    And I click apply
    And I wait for "virgo-dummy-1.0" to be installed on this "sle-minion"

  Scenario: Upgrade a package through the UI
    Given I am on the Systems overview page of this "sle-minion"
    Then I follow "States" in the content area
    And I follow "Packages"
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "andromeda-dummy" text
    And "andromeda-dummy-1.0" should be installed on "sle-minion"
    And I change the state of "andromeda-dummy" to "Installed" and "Latest"
    Then I should see a "1 Changes" text
    And I click save
    Then I wait until I see "Package states have been saved." text
    And I click apply
    And I wait for "andromeda-dummy-2.0-1.1" to be installed on this "sle-minion"

  Scenario: Verify the package states
    Given I am on the Systems overview page of this "sle-minion"
    Then I follow "States" in the content area
    And I follow "Packages"
    And I should see a "Package States" text
    And I should see a "milkyway-dummy" text
    And I should see a "andromeda-dummy" text
    And I should see a "virgo-dummy" text

  Scenario: Use Salt presence mechanism on an active minion
    Given I am on the Systems overview page of this "sle-minion"
    Then I follow "States" in the content area
    And I follow "Highstate" in the content area
    And I wait for "6" seconds
    And I should see a "pkg_removed" or "running as PID" text in element "highstate"

  Scenario: Use Salt presence mechanism on an unreachable minion
    Given I am on the Systems overview page of this "sle-minion"
    Then I follow "States" in the content area
    And I run "pkill salt-minion" on "sle-minion"
    And I follow "Highstate" in the content area
    And I wait until I see "No reply from minion" text
    And I run "rcsalt-minion restart" on "sle-minion"

  Scenario: Cleanup: remove old packages from SLES minion
    Given I am authorized as "admin" with password "admin"
    And I run "zypper -n mr -d Devel_Galaxy_BuildRepo" on "sle-minion"
    And I run "zypper -n rm milkyway-dummy" on "sle-minion" without error control
    And I run "zypper -n rm virgo-dummy" on "sle-minion" without error control
    And I run "zypper -n rm andromeda-dummy" on "sle-minion" without error control
    And I run "zypper -n ref" on "sle-minion"
