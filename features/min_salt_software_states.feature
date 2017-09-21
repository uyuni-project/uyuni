# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Salt states

  Scenario: Pre-requisite: install milkyway-dummy-1.0 and virgo-dummy-1.0 packages
    Given I am authorized as "admin" with password "admin"
    Then I apply highstate on "sle-minion"
    And I run "zypper -n mr -e Devel_Galaxy_BuildRepo" on "sle-minion"
    And I run "zypper -n ref" on "sle-minion"
    And I run "zypper -n in --oldpackage milkyway-dummy-1.0" on "sle-minion" without error control
    And I run "zypper -n in --oldpackage virgo-dummy-1.0" on "sle-minion" without error control
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I reload the page
    And I reload the page until it does contain a "FINISHED" text in the table first row

  Scenario: Accepted minion has a base channel
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    Then the system should have a Base channel set

  Scenario: Remove a package through the UI
    Given I am on the Systems overview page of this "sle-minion"
    Then I follow "States" in the content area
    And I follow "Packages"
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "milkyway-dummy" text
    And "milkyway-dummy" is installed on "sle-minion"
    And I change the state of "milkyway-dummy" to "Removed" and ""
    Then I should see a "1 Changes" text
    And I click save
    And I click apply
    And "milkyway-dummy" is not installed 

  Scenario: Install a package through the UI
    Given I am on the Systems overview page of this "sle-minion"
    Then I follow "States" in the content area
    And I follow "Packages"
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "milkyway-dummy" text
    And "milkyway-dummy" is not installed
    And I change the state of "milkyway-dummy" to "Installed" and ""
    Then I should see a "1 Changes" text
    And I click save
    And I click apply
    And I wait for "milkyway-dummy" to be installed on this "sle-minion"

  Scenario: Install an already installed package through the UI
    Given I am on the Systems overview page of this "sle-minion"
    Then I follow "States" in the content area
    And I follow "Packages"
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "virgo-dummy" text
    And "virgo-dummy-1.0" is installed on "sle-minion"
    And I change the state of "virgo-dummy" to "Installed" and "Any"
    Then I should see a "1 Changes" text
    And I click save
    And I click apply
    And I wait for "virgo-dummy-1.0" to be installed on this "sle-minion"

  Scenario: Upgrade a package through the UI
    Given I am on the Systems overview page of this "sle-minion"
    Then I follow "States" in the content area
    And I follow "Packages"
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "andromeda-dummy" text
    And "andromeda-dummy-1.0-4.1" is installed on "sle-minion"
    And I change the state of "andromeda-dummy" to "Installed" and "Latest"
    Then I should see a "1 Changes" text
    And I click save
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
    And I wait for "6" seconds
    And I run "rcsalt-minion restart" on "sle-minion"
    And I should see a "No reply from minion" text in element "highstate"

  Scenario: Cleanup: remove milkyway-dummy and virgo-dummy packages from SLES minion
    Given I am authorized as "admin" with password "admin"
    And I run "zypper -n mr -d Devel_Galaxy_BuildRepo" on "sle-minion"
    And I run "zypper -n rm milkyway-dummy" on "sle-minion" without error control
    And I run "zypper -n rm virgo-dummy" on "sle-minion" without error control
    And I run "zypper -n ref" on "sle-minion"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I reload the page
    And I reload the page until it does contain a "FINISHED" text in the table first row
