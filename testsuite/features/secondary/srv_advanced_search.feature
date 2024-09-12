# Copyright (c) 2022-2024 SUSE LLC.
# Licensed under the terms of the MIT license.

@scope_spacewalk_utils
@sle_minion
Feature: Advanced Search
  In order to check and maintain the minions
  As an authorized user
  I want to be able to search for specific systems according to location or other characteristics

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: No search results - inverse results
    Given I clean the search index on the server
    When I follow the left menu "Systems > Advanced Search"
    And I enter "sle_minion" hostname on the search field
    And I select "Hostname" from "Field to Search"
    And I check "invert"
    And I click on the search button
    Then I should see a "No results found." text

  Scenario: One search result for City
    Given I have a property "City" with value "Little Whinging" on "sle_minion"
    When I follow the left menu "Systems > Advanced Search"
    And I enter "Little Whinging" on the search field
    And I select "City" from "Field to Search"
    And I check "fineGrained"
    And I click on the search button
    Then I should land on system's overview page

  Scenario: One search result for State/Province
    Given I have a property "State/Province" with value "Surrey" on "sle_minion"
    When I follow the left menu "Systems > Advanced Search"
    And I enter "Surrey" on the search field
    And I select "State/Province" from "Field to Search"
    And I check "fineGrained"
    And I click on the search button
    Then I should land on system's overview page

  Scenario: One search result for Country
    Given I have a combobox property "Country" with value "Portugal (PT)" on "sle_minion"
    When I follow the left menu "Systems > Advanced Search"
    And I enter "PT" on the search field
    And I select "Country Code" from "Field to Search"
    And I check "fineGrained"
    And I click on the search button
    Then I should land on system's overview page

  Scenario: One search result for hostname using "Fine grained search results"
    When I follow the left menu "Systems > Advanced Search"
    And I enter "sle_minion" hostname on the search field
    And I select "Hostname" from "Field to Search"
    And I check "fineGrained"
    And I click on the search button
    Then I should land on system's overview page

  Scenario: List results for hostname
    When I follow the left menu "Systems > Advanced Search"
    And I enter "sle_minion" hostname on the search field
    And I select "Hostname" from "Field to Search"
    And I click on the search button
    Then I should see "sle_minion" hostname as first search result
