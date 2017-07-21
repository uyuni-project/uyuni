# Copyright (c) 2015-16 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Install a patch the client via salt through the UI

  Scenario: Prequisite: Install virgo-dummy-1.0 packages
    Given I am authorized as "admin" with password "admin"
    And I run "zypper -n mr -e Devel_Galaxy_BuildRepo" on "sle-minion"
    And I run "zypper -n ref" on "sle-minion"
    And I run "zypper -n in --oldpackage virgo-dummy-1.0" on "sle-minion" without error control
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I reload the page
    And I try to reload page until it does not contain "RUNNING" text

   Scenario: Install an patch to the minion
    Given I am on the Systems overview page of this "sle-minion"
    And I follow "Software" in the content area
    And I follow "Patches" in the content area
    When I check "virgo-dummy-3456" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    And I wait for "5" seconds
    Then I should see a "1 patch update has been scheduled for" text
    And I wait for "virgo-dummy-2.0-1.1" to be installed on this "sle-minion"

  Scenario: CLEANUP: Remove virgo-dummy packages from sle-minion
    Given I am authorized as "admin" with password "admin"
    And I run "zypper -n mr -d Devel_Galaxy_BuildRepo" on "sle-minion"
    And I run "zypper -n rm virgo-dummy" on "sle-minion" without error control
    And I run "zypper -n ref" on "sle-minion"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I reload the page
    And I try to reload page until it does not contain "RUNNING" text
