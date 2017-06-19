# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Use salt formulas
  In order to use simple forms to apply changes to minions
  As an authorized user
  I want to be able to install and use salt formulas

  Scenario: Install a formula package on the server
     Given I am authorized
     When I manually install the "locale" formula on the server
     And I reload the page
     And I follow "Salt"
     And I follow "Formula Catalog"
     Then I should see a "locale" text

  Scenario: Enable the formula on the minion
     Given I am on the Systems overview page of this "sle-minion"
     When I follow "Formulas" in the content area
     Then I should see a "Choose formulas:" text
     And I should see a "General System Configuration" text
     And I should see a "Locale" text
     When I check the "locale" formula
     And I click on "Save"
     Then the "locale" formula should be checked

  Scenario: Parametrize the formula on the minion
     Given I am on the Systems overview page of this "sle-minion"
     When I follow "Formulas" in the content area
     And I follow "Locale" in the content area
     And I select "Etc/GMT-5" in timezone name field
     And I select "French" in language field
     And I select "French (Canada)" in keyboard layout field
     And I click on "Save Formula"
     Then I should see a "Formula saved!" text
     When I refresh the pillar data
     Then the pillar data for "formulas" should be "- locale" on "sle-minion"
     And the pillar data for "timezone:name" should be "Etc/GMT-5" on "sle-minion"
     And the pillar data for "keyboard_and_language:keyboard_layout" should be "French (Canada)" on "sle-minion"
     And the pillar data for "keyboard_and_language:language" should be "French" on "sle-minion"
     And the pillar data for "formulas" should be empty on "ssh-minion"
     And the pillar data for "timezone" should be empty on "ssh-minion"
     And the pillar data for "keyboard_and_language" should be empty on "ssh-minion"

  Scenario: Apply the parametrized formula via the highstate
     Given I am on the Systems overview page of this "sle-minion"
     And I follow "States" in the content area
     And I click on "Apply Highstate"
     Then I should see a "Applying the highstate has been scheduled." text
     And I wait for "40" seconds
     And the timezone on "sle-minion" should be "+05"
     And the keymap on "sle-minion" should be "ca.map.gz"
     And the language on "sle-minion" should be "fr_FR.UTF-8"

  Scenario: Check the pillar data
     When I refresh the pillar data
     Then the pillar data for "formulas" should be "- locale" on "sle-minion"
     And the pillar data for "timezone:name" should be "Etc/GMT-5" on "sle-minion"
     And the pillar data for "keyboard_and_language:keyboard_layout" should be "French (Canada)" on "sle-minion"
     And the pillar data for "keyboard_and_language:language" should be "French" on "sle-minion"
     And the pillar data for "formulas" should be empty on "ssh-minion"
     And the pillar data for "timezone" should be empty on "ssh-minion"
     And the pillar data for "keyboard_and_language" should be empty on "ssh-minion"

  Scenario: Reset the formula on the minion
     Given I am on the Systems overview page of this "sle-minion"
     When I follow "Formulas" in the content area
     And I follow "Locale" in the content area
     And I click on "Clear values" and confirm
     And I click on "Save Formula"
     Then I should see a "Formula saved!" text
     And I refresh the pillar data
     And the pillar data for "formulas" should be "- locale" on "sle-minion"
     And the pillar data for "timezone:name" should be "CET" on "sle-minion"
     And the pillar data for "keyboard_and_language:keyboard_layout" should be "English (US)" on "sle-minion"
     And the pillar data for "keyboard_and_language:language" should be "English (US)" on "sle-minion"
     And the pillar data for "formulas" should be empty on "ssh-minion"
     And the pillar data for "timezone" should be empty on "ssh-minion"
     And the pillar data for "keyboard_and_language" should be empty on "ssh-minion"

  Scenario: Apply the reset formula via the highstate
     Given I am on the Systems overview page of this "sle-minion"
     And I follow "States" in the content area
     And I click on "Apply Highstate"
     Then I should see a "Applying the highstate has been scheduled." text
     And I wait for "40" seconds
     And the timezone on "sle-minion" should be "CET"
     And the keymap on "sle-minion" should be "us.map.gz"
     And the language on "sle-minion" should be "en_US.UTF-8"

  Scenario: Disable the formula on the minion
     Given I am on the Systems overview page of this "sle-minion"
     When I follow "Formulas" in the content area
     Then I should see a "Choose formulas:" text
     And I should see a "General System Configuration" text
     And I should see a "Locale" text
     When I uncheck the "locale" formula
     And I click on "Save"
     Then I refresh the pillar data
     And the "locale" formula should be unchecked
     And the pillar data for "formulas" should be empty on "sle-minion"
     And the pillar data for "timezone" should be empty on "sle-minion"
     And the pillar data for "keyboard_and_language" should be empty on "sle-minion"
     And the pillar data for "formulas" should be empty on "ssh-minion"
     And the pillar data for "timezone" should be empty on "ssh-minion"
     And the pillar data for "keyboard_and_language" should be empty on "ssh-minion"

  Scenario: Assing formula to minion via group formula
     Given I am on the groups page
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
     And I follow "Target Systems"
     And I check the "sle-minion" client
     And I click on "Add Systems"
     Then I should see a "1 systems were added to locale-formula-group server group." text
     And I refresh the pillar data
     And the pillar data for "formulas" should be "- locale" on "sle-minion"
     And the pillar data for "timezone:name" should be "CET" on "sle-minion"
     And the pillar data for "keyboard_and_language:keyboard_layout" should be "English (US)" on "sle-minion"
     And the pillar data for "keyboard_and_language:language" should be "English (US)" on "sle-minion"
     And the pillar data for "formulas" should be empty on "ssh-minion"
     And the pillar data for "timezone" should be empty on "ssh-minion"
     And the pillar data for "keyboard_and_language" should be empty on "ssh-minion"
