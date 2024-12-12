# Copyright (c) 2015-2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in the following features:
# - features/secondary/trad_cve_audit.feature
# - features/secondary/trad_weak_deps.feature
# - features/secondary/trad_migrate_to_sshminion.feature
# These features can have failing scenarios if some packages are not properly unlocked.

@scope_traditional_client
Feature: Lock packages on traditional client

  Background:
    Given I am on the Systems overview page of this "sle_client"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: install packages needed for locking test
    When I install package "orion-dummy" on this "sle_client"
    And I install package "milkyway-dummy" on this "sle_client"
    And I remove package "hoag-dummy" from this "sle_client" without error control

  Scenario: Lock a package on the client
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    And I enter "hoag-dummy-1.1-1.1" as the filtered package name
    And I click on the filter button
    And I check row with "hoag-dummy-1.1-1.1" and arch of "sle_client"
    And I click on "Lock"
    And I run "rhn_check -vvv" on "sle_client"
    Then I should see a "Packages has been requested for being locked." text
    And "hoag-dummy-1.1-1.1" is locked on "sle_client"
    When I follow "Lock"
    Then package "hoag-dummy-1.1-1.1" is reported as locked

  Scenario: Attempt to install a locked package on the client
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    And package "hoag-dummy-1.1-1.1" is reported as locked
    And I follow "Install"
    And I enter "hoag-dummy-1.1-1.1" as the filtered package name
    And I click on the filter button
    And I check row with "hoag-dummy-1.1-1.1" and arch of "sle_client"
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    And I run "rhn_check -vvv" on "sle_client"
    Then I should see a "1 package install has been scheduled for" text
    When I follow "Events"
    And I follow "History"
    And I wait until I see the event "Package Install/Upgrade scheduled" completed during last minute, refreshing the page
    And I follow the event "Package Install/Upgrade scheduled" completed during last minute
    Then the package scheduled is "hoag-dummy-1.1-1.1"
    And the action status is "Failed"

  Scenario: Unlock a package on the client
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    And package "hoag-dummy-1.1-1.1" is reported as locked
    And I enter "hoag-dummy-1.1-1.1" as the filtered package name
    And I click on the filter button
    And I check row with "hoag-dummy-1.1-1.1" and arch of "sle_client"
    And I click on "Unlock"
    And I run "rhn_check -vvv" on "sle_client"
    Then I should see a "Packages has been requested for being unlocked." text
    And "hoag-dummy-1.1-1.1" is unlocked on "sle_client"
    When I follow "Lock"
    And I enter "hoag-dummy-1.1-1.1" as the filtered package name
    And I click on the filter button
    Then package "hoag-dummy-1.1-1.1" is reported as unlocked

  Scenario: Schedule a package lock
    When I follow "Software" in the content area
    And I follow "Lock / Unlock"
    And I enter "hoag-dummy-1.1-1.1" as the filtered package name
    And I click on the filter button
    And I check row with "hoag-dummy-1.1-1.1" and arch of "sle_client"
    And I click on "Lock"
    Then I should see a "Packages has been requested for being locked." text
    And package "hoag-dummy-1.1-1.1" is reported as pending to be locked

  Scenario: Schedule another package lock
    And I follow "Software" in the content area
    And I follow "Lock / Unlock"
    And package "hoag-dummy-1.1-1.1" is reported as pending to be locked
    And package "hoag-dummy-1.1-1.1" cannot be selected
    And I enter "milkyway-dummy-2.0-1.1" as the filtered package name
    And I click on the filter button
    When I check row with "milkyway-dummy-2.0-1.1" and arch of "sle_client"
    And I click on "Lock"
    Then I should see a "Packages has been requested for being locked." text
    When I follow "Lock / Unlock"
    Then package "hoag-dummy-1.1-1.1" is reported as pending to be locked
    And package "hoag-dummy-1.1-1.1" cannot be selected
    And package "milkyway-dummy-2.0-1.1" is reported as pending to be locked
    And package "milkyway-dummy-2.0-1.1" cannot be selected
    When I run "rhn_check -vvv" on "sle_client"
    Then "hoag-dummy-1.1-1.1" is locked on "sle_client"
    And "milkyway-dummy-2.0-1.1" is locked on "sle_client"
    When I follow "Lock / Unlock"
    Then package "hoag-dummy-1.1-1.1" is reported as locked
    And package "milkyway-dummy-2.0-1.1" is reported as locked

  Scenario: Mix package locks and unlock events
    And I follow "Software" in the content area
    And I follow "Lock / Unlock"
    And package "hoag-dummy-1.1-1.1" is reported as locked
    And package "milkyway-dummy-2.0-1.1" is reported as locked
    And I enter "orion-dummy-1.1-1.1" as the filtered package name
    And I click on the filter button
    When I check row with "orion-dummy-1.1-1.1" and arch of "sle_client"
    And I click on "Lock"
    Then I should see a "Packages has been requested for being locked." text
    When I follow "Lock"
    Then package "hoag-dummy-1.1-1.1" is reported as locked
    And package "milkyway-dummy-2.0-1.1" is reported as locked
    And package "orion-dummy-1.1-1.1" is reported as pending to be locked
    And I enter "hoag-dummy-1.1-1.1" as the filtered package name
    And I click on the filter button
    And I uncheck row with "hoag-dummy-1.1-1.1" and arch of "sle_client"
    And I enter "milkyway-dummy-2.0-1.1" as the filtered package name
    And I click on the filter button
    When I check row with "milkyway-dummy-2.0-1.1" and arch of "sle_client"
    And I click on "Unlock"
    Then I should see a "Packages has been requested for being unlocked." text
    When I follow "Lock / Unlock"
    Then package "hoag-dummy-1.1-1.1" is reported as locked
    And package "milkyway-dummy-2.0-1.1" is reported as pending to be unlocked
    And package "orion-dummy-1.1-1.1" is reported as pending to be locked
    When I run "rhn_check -vvv" on "sle_client"
    Then "hoag-dummy-1.1-1.1" is locked on "sle_client"
    And "milkyway-dummy-2.0-1.1" is unlocked on "sle_client"
    And "orion-dummy-1.1-1.1" is locked on "sle_client"
    When I follow "Lock / Unlock"
    Then package "hoag-dummy-1.1-1.1" is reported as locked
    And package "orion-dummy-1.1-1.1" is reported as locked
    And I enter "milkyway-dummy-2.0-1.1" as the filtered package name
    And I click on the filter button
    And package "milkyway-dummy-2.0-1.1" is reported as unlocked

  Scenario: Mix package locks and unlock events part 2
    And I follow "Software" in the content area
    And I follow "Lock / Unlock"
    When I click on "Select All"
    And I click on "Unlock"
    Then I should see a "Packages has been requested for being unlocked." text
    When I follow "Lock"
    Then only packages "hoag-dummy-1.1-1.1, orion-dummy-1.1-1.1" are reported as pending to be unlocked
    When I run "rhn_check -vvv" on "sle_client"
    And I follow "Lock / Unlock"
    And I enter "hoag-dummy-1.1-1.1" as the filtered package name
    And I click on the filter button
    Then package "hoag-dummy-1.1-1.1" is reported as unlocked
    And I enter "orion-dummy-1.1-1.1" as the filtered package name
    And I click on the filter button
    And package "orion-dummy-1.1-1.1" is reported as unlocked

  Scenario: Cleanup: remove packages after testing locks
    Then I remove package "orion-dummy" from this "sle_client"
    And I remove package "milkyway-dummy" from this "sle_client"
