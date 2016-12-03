# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: System package list in the UI is updated if packages are manually installed/removed using zypper or yum

  Scenario: Manually removing a package in a minion
    Given this minion key is accepted
    And I am on the Systems overview page of this minion
    When I follow "Software"
    And I follow "List / Remove"
    And I enter "milkyway-dummy" in the css "input[placeholder='Filter by Package Name: ']"
    And I click on the css "button.spacewalk-button-filter"
    And I should see a "milkyway-dummy" text
    Then I manually remove the "milkyway-dummy" package in the minion
    And I click on the css "button.spacewalk-button-filter" until page does not contain "milkyway-dummy" text

  Scenario: Manually installing a package in a minion
    Given this minion key is accepted
    And I am on the Systems overview page of this minion
    When I follow "Software"
    And I follow "List / Remove"
    And I enter "milkyway-dummy" in the css "input[placeholder='Filter by Package Name: ']"
    And I click on the css "button.spacewalk-button-filter"
    And I should not see a "milkyway-dummy" text
    Then I manually install the "milkyway-dummy" package in the minion
    And I click on the css "button.spacewalk-button-filter" until page does not contain "milkyway-dummy" text
