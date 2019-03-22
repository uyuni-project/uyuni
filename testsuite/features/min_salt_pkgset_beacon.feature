# Copyright (c) 2016-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: System package list is updated if packages are manually installed or removed

  Scenario: Pre-requisite: install milkyway-dummy-1.0 packages
    Given I am authorized as "admin" with password "admin"
    And I run "zypper -n mr -e Devel_Galaxy_BuildRepo" on "sle-minion"
    And I run "zypper -n ref" on "sle-minion"
    And I run "zypper -n in --oldpackage milkyway-dummy-1.0" on "sle-minion" without error control

  Scenario: Pre-requisite: refresh package list first on SLE minion machine
    When I refresh packages list via spacecmd on "sle-minion"
    And I wait until refresh package list on "sle-minion" is finished

  Scenario: Pre-requisite: ensure the errata cache is computed
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "milkyway-dummy" in the css "input[placeholder='Filter by Package Name: ']"
    And I click on the css "button.spacewalk-button-filter" until page does contain "milkyway-dummy-1.0" text
    Then I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    Then I click on "Single Run Schedule"
    And I should see a "bunch was scheduled" text
    Then I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Remove manually a package on a minion
    Given I am on the Systems overview page of this "sle-minion"
    And I follow "Software" in the content area
    And I follow "List / Remove"
    And I enter "milkyway-dummy" in the css "input[placeholder='Filter by Package Name: ']"
    And I click on the css "button.spacewalk-button-filter"
    And I should see a "milkyway-dummy" text
    Then I remove package "milkyway-dummy" from this "sle-minion"
    And I click on the css "button.spacewalk-button-filter" until page does not contain "milkyway-dummy" text

  Scenario: Install manually a package on a minion
    Given I am on the Systems overview page of this "sle-minion"
    And I follow "Software" in the content area
    And I follow "List / Remove"
    And I enter "milkyway-dummy" in the css "input[placeholder='Filter by Package Name: ']"
    And I click on the css "button.spacewalk-button-filter"
    And I should not see a "milkyway-dummy" text
    Then I install package "milkyway-dummy" on this "sle-minion"
    And I click on the css "button.spacewalk-button-filter" until page does not contain "milkyway-dummy" text

  Scenario: Cleanup: remove milkyway-dummy packages from SLES minion
    Given I am authorized as "admin" with password "admin"
    And I run "zypper -n mr -d Devel_Galaxy_BuildRepo" on "sle-minion"
    And I run "zypper -n rm milkyway-dummy" on "sle-minion" without error control
    And I run "zypper -n ref" on "sle-minion"
