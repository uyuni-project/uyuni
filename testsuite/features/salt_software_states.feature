# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check the Salt package state UI
  In Order to test salt package states.
  As the testing

  Scenario: Subscribe to base channel
    Given I am on the Systems overview page of this minion
    When I follow "Software" in the content area
    Then I follow "Software Channels" in the content area
    And I select "SLES11-SP3-Updates x86_64 Channel" from "new_base_channel_id"
    And I click on "Confirm"
    And I click on "Modify Base Software Channel"
    And I should see a "System's Base Channel has been updated." text

  Scenario: Test package removal through the UI
    Given I am on the Systems overview page of this minion
    Then I follow "States" in the content area
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "milkyway-dummy" text
    And "milkyway-dummy" is installed on "minion"
    And I change the state of "milkyway-dummy" to "Removed" and ""
    Then I should see a "1 Changes" text
    And I click save
    And I click apply
    And "milkyway-dummy" is not installed 

   Scenario: Test package installation through the UI
    Given I am on the Systems overview page of this minion
    Then I follow "States" in the content area
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "milkyway-dummy" text
    And "milkyway-dummy" is not installed
    And I change the state of "milkyway-dummy" to "Installed" and ""
    Then I should see a "1 Changes" text
    And I click save
    And I click apply
    And I wait for "milkyway-dummy" to be installed

   Scenario: Test package installation with any through the UI
    Given I am on the Systems overview page of this minion
    Then I follow "States" in the content area
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "virgo-dummy" text
    And "virgo-dummy-1.0" is installed on "minion"
    And I change the state of "virgo-dummy" to "Installed" and "Any"
    Then I should see a "1 Changes" text
    And I click save
    And I click apply
    And I wait for "virgo-dummy-1.0" to be installed

  Scenario: Test package upgrade through the UI
    Given I am on the Systems overview page of this minion
    Then I follow "States" in the content area
    And I should see a "Package States" text
    And I list packages with "dummy"
    Then I should see a "andromeda-dummy" text
    And "andromeda-dummy-1.0" is installed on "minion"
    And I change the state of "andromeda-dummy" to "Installed" and "Latest"
    Then I should see a "1 Changes" text
    And I click save
    And I click apply
    And I wait for "andromeda-dummy-2.0-1.1" to be installed

  Scenario: I verify the system status of the salt ui
    Given I am on the Systems overview page of this minion
    Then I follow "States" in the content area
    And I should see a "Package States" text
    And I should see a "milkyway-dummy" text
    And I should see a "andromeda-dummy" text
    And I should see a "virgo-dummy" text

  Scenario: Test Salt presence ping mechanism on active minion
    Given I am on the Systems overview page of this minion
    Then I follow "States" in the content area
    And I follow "Highstate" in the content area
    And I wait for "6" seconds
    And I should see "pkg_removed" or "running as PID" loaded in the textarea

  Scenario: Test Salt presence ping mechanism on unreachable minion
    Given I am on the Systems overview page of this minion
    Then I follow "States" in the content area
    And I run "pkill salt-minion" on "sle-minion"
    And I follow "Highstate" in the content area
    And I wait for "6" seconds
    And I run "rcsalt-minion restart" on "sle-minion"
    And I should see "No reply from minion" loaded in the textarea
