# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@sle_minion
@scope_salt
Feature: Lock packages on SLES salt minion

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: install packages needed for locking test
    When I install package "orion-dummy" on this "sle_minion"
    And I install package "milkyway-dummy" on this "sle_minion"
    And I remove package "hoag-dummy" from this "sle_minion" without error control

  Scenario: Lock a package on the client
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    And I check row with "hoag-dummy-1.1-1.1" and arch of "sle_minion"
    And I click on "Lock"
    Then I should see a "Packages has been requested for being locked." text
    When I wait until event "Lock packages scheduled by admin" is completed
    Then "hoag-dummy-1.1-1.1" is locked on "sle_minion"
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    Then package "hoag-dummy-1.1-1.1" is reported as locked

  Scenario: Attempt to install a locked package on the client
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    And package "hoag-dummy-1.1-1.1" is reported as locked
    And I follow "Install"
    And I check row with "hoag-dummy-1.1-1.1" and arch of "sle_minion"
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    When I follow "Events"
    And I follow "History"
    And I wait until I see the event "Package Install/Upgrade scheduled by admin" completed during last minute, refreshing the page
    And I follow the event "Package Install/Upgrade scheduled by admin" completed during last minute
    Then the package scheduled is "hoag-dummy-1.1-1.1"
    And the action status is "Failed"

  Scenario: Unlock a package on the client
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    And package "hoag-dummy-1.1-1.1" is reported as locked
    And I check row with "hoag-dummy-1.1-1.1" and arch of "sle_minion"
    And I click on "Unlock"
    Then I should see a "Packages has been requested for being unlocked." text
    When I wait until event "Lock packages scheduled by admin" is completed
    Then "hoag-dummy-1.1-1.1" is unlocked on "sle_minion"
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    Then package "hoag-dummy-1.1-1.1" is reported as unlocked

  Scenario: Schedule a package lock
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    And I check row with "hoag-dummy-1.1-1.1" and arch of "sle_minion"
    And I click on "Lock"
    Then I should see a "Packages has been requested for being locked." text
    And package "hoag-dummy-1.1-1.1" is reported as pending to be locked

  Scenario: Schedule another package lock
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    When I check row with "milkyway-dummy-2.0-1.1" and arch of "sle_minion"
    And I click on "Lock"
    Then I should see a "Packages has been requested for being locked." text
    When I wait until event "Lock packages scheduled by admin" is completed
    Then "hoag-dummy-1.1-1.1" is locked on "sle_minion"
    And "milkyway-dummy-2.0-1.1" is locked on "sle_minion"
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    Then package "hoag-dummy-1.1-1.1" is reported as locked
    And package "milkyway-dummy-2.0-1.1" is reported as locked

  Scenario: Mix package locks and unlock events
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    And package "hoag-dummy-1.1-1.1" is reported as locked
    And package "milkyway-dummy-2.0-1.1" is reported as locked
    When I check row with "orion-dummy-1.1-1.1" and arch of "sle_minion"
    And I click on "Lock"
    Then I should see a "Packages has been requested for being locked." text
    When I follow "Lock / Unlock"
    And I check row with "milkyway-dummy-2.0-1.1" and arch of "sle_minion"
    And I uncheck row with "hoag-dummy-1.1-1.1" and arch of "sle_minion"
    And I click on "Unlock"
    Then I should see a "Packages has been requested for being unlocked." text
    When I wait until event "Lock packages scheduled by admin" is completed
    Then "hoag-dummy-1.1-1.1" is locked on "sle_minion"
    And "milkyway-dummy-2.0-1.1" is unlocked on "sle_minion"
    And "orion-dummy-1.1-1.1" is locked on "sle_minion"
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    Then package "hoag-dummy-1.1-1.1" is reported as locked
    And package "milkyway-dummy-2.0-1.1" is reported as unlocked
    And package "orion-dummy-1.1-1.1" is reported as locked

  Scenario: Mix package locks and unlock events part 2
    Given I am on the Systems overview page of this "sle_minion"
    And I follow "Software" in the content area
    And I follow "Lock / Unlock"
    When I click on "Select All"
    And I click on "Unlock"
    Then I should see a "Packages has been requested for being unlocked." text
    And only packages "hoag-dummy-1.1-1.1, orion-dummy-1.1-1.1" are reported as pending to be unlocked
    When I wait until event "Lock packages scheduled by admin" is completed
    Then "hoag-dummy-1.1-1.1" is unlocked on "sle_minion"
    And "orion-dummy-1.1-1.1" is unlocked on "sle_minion"
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    Then package "hoag-dummy-1.1-1.1" is reported as unlocked
    And package "orion-dummy-1.1-1.1" is reported as unlocked

  Scenario: Cleanup: remove packages after testing locks
    Then I remove package "orion-dummy" from this "sle_minion"
    And I remove package "milkyway-dummy" from this "sle_minion"
