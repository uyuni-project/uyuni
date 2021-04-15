# Copyright (c) 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_formulas
Feature: Use salt formulas
  In order to use simple forms to apply changes to minions
  As an authorized user
  I want to be able to install and use salt formulas

   Scenario: Log in as admin user
      Given I am authorized for the "Admin" section

   Scenario: Install the locale formula package on the server
     When I manually install the "locale" formula on the server
     And I synchronize all Salt dynamic modules on "sle_minion"

  Scenario: The new formula appears on the server
     When I follow the left menu "Salt > Formula Catalog"
     Then I should see a "locale" text in the content area

  Scenario: Enable the formula on the minion
     Given I am on the Systems overview page of this "sle_minion"
     When I follow "Formulas" in the content area
     Then I should see a "Choose formulas:" text
     And I should see a "General System Configuration" text
     And I should see a "Locale" text
     When I check the "locale" formula
     And I click on "Save"
     Then the "locale" formula should be checked

  Scenario: Parametrize the formula on the minion
     When I follow "Formulas" in the content area
     And I follow first "Locale" in the content area
     And I select "Etc/GMT-5" in timezone name field
     And I select "French" in language field
     And I select "French (Canada)" in keyboard layout field
     And I click on "Save Formula"
     Then I should see a "Formula saved" text

  Scenario: Check the pillar data after saving the formula
     When I refresh the pillar data
     Then the pillar data for "formulas" should be "- locale" on "sle_minion"
     And the pillar data for "timezone:name" should be "Etc/GMT-5" on "sle_minion"
     And the pillar data for "keyboard_and_language:keyboard_layout" should be "French (Canada)" on "sle_minion"
     And the pillar data for "keyboard_and_language:language" should be "French" on "sle_minion"

@ssh_minion
  Scenario: No other minion is affected by the formula
     Then the pillar data for "formulas" should be empty on "ssh_minion"
     And the pillar data for "timezone" should be empty on "ssh_minion"
     And the pillar data for "keyboard_and_language" should be empty on "ssh_minion"

  Scenario: Use the parametrized formula in test mode
     Given I am on the Systems overview page of this "sle_minion"
     And I follow "States" in the content area
     Then I should see the toggler "disabled"
     When I click on the "disabled" toggler
     And I click on "Apply Highstate"
     Then I should see a "Applying the highstate has been scheduled." text
     And I wait until event "Apply highstate in test-mode scheduled by admin" is completed

  Scenario: Apply the parametrized formula via the highstate
     And I follow "States" in the content area
     And I click on "Apply Highstate"
     Then I should see a "Applying the highstate has been scheduled." text
     When I wait until event "Apply highstate scheduled by admin" is completed
     Then the timezone on "sle_minion" should be "+05"
     And the keymap on "sle_minion" should be "ca.map.gz"
     And the language on "sle_minion" should be "fr_FR.UTF-8"

  Scenario: Reset the formula on the minion
     When I follow "Formulas" in the content area
     And I follow first "Locale" in the content area
     And I click on "Clear values" and confirm
     And I click on "Save Formula"
     Then I should see a "Formula saved" text

  Scenario: Check the pillar data after resetting the formula
     When I refresh the pillar data
     Then the pillar data for "formulas" should be "- locale" on "sle_minion"
     And the pillar data for "timezone:name" should be "CET" on "sle_minion"
     And the pillar data for "keyboard_and_language:keyboard_layout" should be "English (US)" on "sle_minion"
     And the pillar data for "keyboard_and_language:language" should be "English (US)" on "sle_minion"

  Scenario: Apply the reset formula via the highstate
     And I follow "States" in the content area
     And I click on "Apply Highstate"
     Then I should see a "Applying the highstate has been scheduled." text
     When I wait until event "Apply highstate scheduled by admin" is completed
     Then the timezone on "sle_minion" should be "CET"
     And the keymap on "sle_minion" should be "us.map.gz"
     And the language on "sle_minion" should be "en_US.UTF-8"

  Scenario: Disable the formula on the minion
     When I follow "Formulas" in the content area
     Then I should see a "Choose formulas:" text
     And I should see a "General System Configuration" text
     And I should see a "Locale" text
     When I uncheck the "locale" formula
     And I click on "Save"
     Then the "locale" formula should be unchecked

  Scenario: Check the pillar data after disabling the formula
     When I refresh the pillar data
     Then the pillar data for "formulas" should be empty on "sle_minion"
     And the pillar data for "timezone" should be empty on "sle_minion"
     And the pillar data for "keyboard_and_language" should be empty on "sle_minion"

  Scenario: Assign locale formula to minion via group formula
     When I follow the left menu "Systems > System Groups"
     When I follow "Create Group"
     And I enter "locale-formula-group" as "name"
     And I enter "Test group with locale formula added" as "description"
     And I click on "Create Group"
     Then I should see a "System group locale-formula-group created." text
     When I follow "Formulas" in the content area
     Then I should see a "Choose formulas:" text
     And I should see a "General System Configuration" text
     And I should see a "Locale" text
     When I check the "locale" formula
     And I click on "Save"
     And I follow "Target"
     And I check the "sle_minion" client
     And I click on "Add Systems"
     Then I should see a "1 systems were added to locale-formula-group server group." text

  Scenario: Check the pillar data after assigning group formula
     When I refresh the pillar data
     Then the pillar data for "formulas" should be "- locale" on "sle_minion"
     And the pillar data for "timezone:name" should be "CET" on "sle_minion"
     And the pillar data for "keyboard_and_language:keyboard_layout" should be "English (US)" on "sle_minion"
     And the pillar data for "keyboard_and_language:language" should be "English (US)" on "sle_minion"

@ssh_minion
  Scenario: No other minion is affected by the group formula
     Then the pillar data for "formulas" should be empty on "ssh_minion"
     And the pillar data for "timezone" should be empty on "ssh_minion"
     And the pillar data for "keyboard_and_language" should be empty on "ssh_minion"

  Scenario: Cleanup: remove "locale-formula-group" system group
     When I follow the left menu "Systems > System Groups"
     And I follow "locale-formula-group" in the content area
     And I follow "Delete Group" in the content area
     When I click on "Confirm Deletion"
     Then I should see a "System group" text
     And I should see a "locale-formula-group" text
     And I should see a "deleted" text

  Scenario: Cleanup: reset locale values on the minion
     Given I am on the Systems overview page of this "sle_minion"
     And I follow "States" in the content area
     And I click on "Apply Highstate"
     Then I should see a "Applying the highstate has been scheduled." text
     When I wait until event "Apply highstate scheduled by admin" is completed
     Then the timezone on "sle_minion" should be "CET"
     And the keymap on "sle_minion" should be "us.map.gz"
     And the language on "sle_minion" should be "en_US.UTF-8"

  Scenario: Cleanup: uninstall formula package from the server
     And I manually uninstall the "locale" formula from the server

  Scenario: Cleanup: remove remaining systems from SSM after formula tests
     When I follow "Clear"
