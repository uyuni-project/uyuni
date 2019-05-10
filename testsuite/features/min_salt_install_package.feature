# Copyright (c) 2015-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Install a patch on the client via Salt through the UI

  Scenario: Pre-requisite: install virgo-dummy-1.0 packages
    When I enable repository "Devel_Galaxy_BuildRepo" on this "sle-minion"
    And I run "zypper -n ref" on "sle-minion"
    And I run "zypper -n in --oldpackage virgo-dummy-1.0" on "sle-minion" without error control

  Scenario: Pre-requisite: refresh package list and check old packages installed on SLE minion client
    When I refresh packages list via spacecmd on "sle-minion"
    And I wait until refresh package list on "sle-minion" is finished
    Then spacecmd should show packages "virgo-dummy-1.0" installed on "sle-minion"

  Scenario: Pre-requisite: ensure the errata cache is computed before patchin Salt minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "virgo-dummy" in the css "input[placeholder='Filter by Package Name: ']"
    And I click on the css "button.spacewalk-button-filter" until page does contain "virgo-dummy" text
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Install a patch on the minion
    Given I am on the Systems overview page of this "sle-minion"
    And I follow "Software" in the content area
    And I follow "Patches" in the content area
    When I check "virgo-dummy-3456" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    And I wait for "5" seconds
    Then I should see a "1 patch update has been scheduled for" text
    And I wait for "virgo-dummy-2.0-1.1" to be installed on this "sle-minion"

  Scenario: Cleanup: remove virgo-dummy packages from SLES minion
    Given I am authorized as "admin" with password "admin"
    And I run "zypper -n mr -d Devel_Galaxy_BuildRepo" on "sle-minion"
    And I run "zypper -n rm virgo-dummy" on "sle-minion" without error control
    And I run "zypper -n ref" on "sle-minion"
