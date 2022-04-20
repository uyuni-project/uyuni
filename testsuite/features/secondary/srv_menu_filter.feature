# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_visualization
Feature: Left Menu Filter
  In order to verify the left menu filter works properly
  As an authorized user
  I want to be able to search different keywords in the menu filter 

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section
  
  Scenario: Search a word and expect the right result
    When I enter "Admin" as the left menu search field
    Then I should see a "Admin" link in the left menu  

  Scenario: Search a partial word and expect the right result
    When I enter "Formula" as the left menu search field
    Then I should see a "Formula Catalog" link in the left menu

  Scenario: Search a word with a different case and expect the right result
    When I enter "saLT" as the left menu search field
    Then I should see a "Salt" link in the left menu  

  @susemanager
  Scenario: Search a word and expect several results
    When I enter "SUSE" as the left menu search field
    Then I should see a "SUSE" text
    And I should see a "SUSE Manager Server" text
    And I should see a "SUSE Manager Proxy" text

  @uyuni
  Scenario: Search a word and expect several results
    When I enter "Uyuni" as the left menu search field
    Then I should see a "Uyuni Server" text
    And I should see a "Uyuni Proxy" text

  Scenario: Search a non-existing word and expect no results
    When I enter "Null" as the left menu search field
    Then I should see left menu empty
