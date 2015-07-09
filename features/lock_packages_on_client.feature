# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Lock packages on client

  Scenario: Lock a package on the client
    Given I am on the Systems overview page of this client
    And I follow "Software" in the content area
    And I follow "Lock"
    When I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Lock"
    And I run rhn_check on this client
    Then I should see a "Packages has been requested for being locked." text
    And "hoag-dummy-1.1-2.1" is locked on this client
    Then I follow "Lock"
    Then Package "hoag-dummy-1.1-2.1" is reported as locked

  Scenario: Attempt to install a locked package on the client
    Given I am on the Systems overview page of this client
    And I follow "Software" in the content area
    And I follow "Lock"
    And Package "hoag-dummy-1.1-2.1" is reported as locked
    Then I follow "Install"
    When I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    And I run rhn_check on this client
    Then I should see a "1 package install has been scheduled for" text
    Then I follow "Events"
    And I follow "History"
    And I follow first "Package Install scheduled by testing"
    Then The package scheduled is "hoag-dummy-1.1-2.1"
    And The action status is "Failed"

  Scenario: Unlock a package on the client
    Given I am on the Systems overview page of this client
    And I follow "Software" in the content area
    And I follow "Lock"
    And Package "hoag-dummy-1.1-2.1" is reported as locked
    When I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Unlock"
    And I run rhn_check on this client
    Then I should see a "Packages has been requested for being unlocked." text
    And "hoag-dummy-1.1-2.1" is unlocked on this client
    Then I follow "Lock"
    Then Package "hoag-dummy-1.1-2.1" is reported as unlocked

  Scenario: Schedule a package lock
    Given I am on the Systems overview page of this client
    And I follow "Software" in the content area
    And I follow "Lock"
    When I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Lock"
    Then I should see a "Packages has been requested for being locked." text
    And Package "hoag-dummy-1.1-2.1" is reported as pending to be locked

  Scenario: Schedule another package lock
    Given I am on the Systems overview page of this client
    And I follow "Software" in the content area
    And I follow "Lock"
    And Package "hoag-dummy-1.1-2.1" is reported as pending to be locked
    And Package "hoag-dummy-1.1-2.1" cannot be selected
    When I check "milkyway-dummy-2.0-1.1" in the list
    And I click on "Lock"
    Then I should see a "Packages has been requested for being locked." text
    When I follow "Lock"
    Then Package "hoag-dummy-1.1-2.1" is reported as pending to be locked
    And Package "hoag-dummy-1.1-2.1" cannot be selected
    And Package "milkyway-dummy-2.0-1.1" is reported as pending to be locked
    And Package "milkyway-dummy-2.0-1.1" cannot be selected
    When I run rhn_check on this client
    Then "hoag-dummy-1.1-2.1" is locked on this client
    And "milkyway-dummy-2.0-1.1" is locked on this client
    When I follow "Lock"
    Then Package "hoag-dummy-1.1-2.1" is reported as locked
    And Package "milkyway-dummy-2.0-1.1" is reported as locked

  Scenario: Mix package locks and unlock events
    Given I am on the Systems overview page of this client
    And I follow "Software" in the content area
    And I follow "Lock"
    And Package "hoag-dummy-1.1-2.1" is reported as locked
    And Package "milkyway-dummy-2.0-1.1" is reported as locked
    When I check "orion-dummy-1.1-1.1" in the list
    And I click on "Lock"
    Then I should see a "Packages has been requested for being locked." text
    When I follow "Lock"
    Then Package "hoag-dummy-1.1-2.1" is reported as locked
    And Package "milkyway-dummy-2.0-1.1" is reported as locked
    And Package "orion-dummy-1.1-1.1" is reported as pending to be locked
    When I check "milkyway-dummy-2.0-1.1" in the list
    And I uncheck "hoag-dummy-1.1-2.1" in the list
    And I click on "Unlock"
    Then I should see a "Packages has been requested for being unlocked." text
    When I follow "Lock"
    Then Package "hoag-dummy-1.1-2.1" is reported as locked
    And Package "milkyway-dummy-2.0-1.1" is reported as pending to be unlocked
    And Package "orion-dummy-1.1-1.1" is reported as pending to be locked
    When I run rhn_check on this client
    Then "hoag-dummy-1.1-2.1" is locked on this client
    And "milkyway-dummy-2.0-1.1" is unlocked on this client
    And "orion-dummy-1.1-1.1" is locked on this client
    When I follow "Lock"
    Then Package "hoag-dummy-1.1-2.1" is reported as locked
    And Package "milkyway-dummy-2.0-1.1" is reported as unlocked
    And Package "orion-dummy-1.1-1.1" is reported as locked

  Scenario: Mix package locks and unlock events
    Given I am on the Systems overview page of this client
    And I follow "Software" in the content area
    And I follow "Lock"
    When I select all the packages
    And I click on "Unlock"
    Then I should see a "Packages has been requested for being unlocked." text
    When I follow "Lock"
    Then Only packages "hoag-dummy-1.1-2.1, orion-dummy-1.1-1.1" are reported as pending to be unlocked
    When I run rhn_check on this client
    And I follow "Lock"
    Then Package "hoag-dummy-1.1-2.1" is reported as unlocked
    And Package "orion-dummy-1.1-1.1" is reported as unlocked
