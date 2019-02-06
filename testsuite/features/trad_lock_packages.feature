# Copyright (c) 2015-17 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Lock packages on traditional client
 
  Background:
    Given I am on the Systems overview page of this "sle-client"

  Scenario: Pre-requisite: install package needed for test
    When I run "zypper -n in orion-dummy" on "sle-client"
    And I run "zypper -n in milkyway-dummy" on "sle-client"
    And I run "zypper -n rm hoag-dummy" on "sle-client" without error control

  Scenario: Lock a package on the client
    When I follow "Software" in the content area
    And I follow "Lock"
    And I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Lock"
    And I run "rhn_check -vvv" on "sle-client"
    Then I should see a "Packages has been requested for being locked." text
    And "hoag-dummy-1.1-2.1" is locked on this client
    When I follow "Lock"
    Then package "hoag-dummy-1.1-2.1" is reported as locked

  Scenario: Attempt to install a locked package on the client
    When I follow "Software" in the content area
    And I follow "Lock"
    And package "hoag-dummy-1.1-2.1" is reported as locked
    And I follow "Install"
    And I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    And I run "rhn_check -vvv" on "sle-client"
    Then I should see a "1 package install has been scheduled for" text
    When I follow "Events"
    And I follow "History"
    And I wait until I see the event "Package Install/Upgrade scheduled by admin" completed during last minute, refreshing the page
    And I follow the event "Package Install/Upgrade scheduled by admin" completed during last minute
    Then the package scheduled is "hoag-dummy-1.1-2.1"
    And the action status is "Failed"

  Scenario: Unlock a package on the client
    When I follow "Software" in the content area
    And I follow "Lock"
    And package "hoag-dummy-1.1-2.1" is reported as locked
    And I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Unlock"
    And I run "rhn_check -vvv" on "sle-client"
    Then I should see a "Packages has been requested for being unlocked." text
    And "hoag-dummy-1.1-2.1" is unlocked on this client
    When I follow "Lock"
    Then package "hoag-dummy-1.1-2.1" is reported as unlocked

  Scenario: Schedule a package lock
    When I follow "Software" in the content area
    And I follow "Lock"
    And I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Lock"
    Then I should see a "Packages has been requested for being locked." text
    And package "hoag-dummy-1.1-2.1" is reported as pending to be locked

  Scenario: Schedule another package lock
    And I follow "Software" in the content area
    And I follow "Lock"
    And package "hoag-dummy-1.1-2.1" is reported as pending to be locked
    And package "hoag-dummy-1.1-2.1" cannot be selected
    When I check "milkyway-dummy-2.0-1.1" in the list
    And I click on "Lock"
    Then I should see a "Packages has been requested for being locked." text
    When I follow "Lock"
    Then package "hoag-dummy-1.1-2.1" is reported as pending to be locked
    And package "hoag-dummy-1.1-2.1" cannot be selected
    And package "milkyway-dummy-2.0-1.1" is reported as pending to be locked
    And package "milkyway-dummy-2.0-1.1" cannot be selected
    When I run "rhn_check -vvv" on "sle-client"
    Then "hoag-dummy-1.1-2.1" is locked on this client
    And "milkyway-dummy-2.0-1.1" is locked on this client
    When I follow "Lock"
    Then package "hoag-dummy-1.1-2.1" is reported as locked
    And package "milkyway-dummy-2.0-1.1" is reported as locked

  Scenario: Mix package locks and unlock events
    And I follow "Software" in the content area
    And I follow "Lock"
    And package "hoag-dummy-1.1-2.1" is reported as locked
    And package "milkyway-dummy-2.0-1.1" is reported as locked
    When I check "orion-dummy-1.1-1.1" in the list
    And I click on "Lock"
    Then I should see a "Packages has been requested for being locked." text
    When I follow "Lock"
    Then package "hoag-dummy-1.1-2.1" is reported as locked
    And package "milkyway-dummy-2.0-1.1" is reported as locked
    And package "orion-dummy-1.1-1.1" is reported as pending to be locked
    When I check "milkyway-dummy-2.0-1.1" in the list
    And I uncheck "hoag-dummy-1.1-2.1" in the list
    And I click on "Unlock"
    Then I should see a "Packages has been requested for being unlocked." text
    When I follow "Lock"
    Then package "hoag-dummy-1.1-2.1" is reported as locked
    And package "milkyway-dummy-2.0-1.1" is reported as pending to be unlocked
    And package "orion-dummy-1.1-1.1" is reported as pending to be locked
    When I run "rhn_check -vvv" on "sle-client"
    Then "hoag-dummy-1.1-2.1" is locked on this client
    And "milkyway-dummy-2.0-1.1" is unlocked on this client
    And "orion-dummy-1.1-1.1" is locked on this client
    When I follow "Lock"
    Then package "hoag-dummy-1.1-2.1" is reported as locked
    And package "milkyway-dummy-2.0-1.1" is reported as unlocked
    And package "orion-dummy-1.1-1.1" is reported as locked

  Scenario: Mix package locks and unlock events part 2
    And I follow "Software" in the content area
    And I follow "Lock"
    When I select all the packages
    And I click on "Unlock"
    Then I should see a "Packages has been requested for being unlocked." text
    When I follow "Lock"
    Then only packages "hoag-dummy-1.1-2.1, orion-dummy-1.1-1.1" are reported as pending to be unlocked
    When I run "rhn_check -vvv" on "sle-client"
    And I follow "Lock"
    Then package "hoag-dummy-1.1-2.1" is reported as unlocked
    And package "orion-dummy-1.1-1.1" is reported as unlocked

  Scenario: Cleanup: remove packages after testing locks
    Then I remove package "orion-dummy" from this "sle-client"
    And I remove package "milkyway-dummy" from this "sle-client"
