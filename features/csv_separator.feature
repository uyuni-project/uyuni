# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Change a user's preference for CSV file separator character
  In order to change the CSV separator
  As an authorized user
  I want to switch back and forth between ',' and ';'

  Scenario: Verify availability of the CSV separator preference
    Given I am authorized as "testing" with password "testing"
    And I follow "Your Preferences"
    Then I should see a "CSV Files" text
    And I should see a "Configure a separator character to be used in downloadable CSV files:" text
    And I should see a "Comma" text
    And I should see a "Semicolon" text

  Scenario: Configure the CSV separator char to semicolon
    Given I am authorized as "testing" with password "testing"
    And I follow "Your Preferences"
    And I choose ";"
    And I click on "Save Preferences"
    Then I should see a "Preferences modified" text
    And radio button "radio-semicolon" is checked

  Scenario: Configure the CSV separator char to comma
    Given I am authorized as "testing" with password "testing"
    And I follow "Your Preferences"
    And I choose ","
    And I click on "Save Preferences"
    Then I should see a "Preferences modified" text
    And radio button "radio-comma" is checked
