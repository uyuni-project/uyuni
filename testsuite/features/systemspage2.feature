# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Explore the main landing page
  In Order to validate completeness of the systems page
  As a authorized user
  I want to see all the texts and links

  Scenario: Completeness of the System Overview table
    Given I am authorized
    When I follow "Systems"
    Then The table should have a column named "System"
    And The table should have a column named "Updates"
    And The table should have a column named "Patches"
    And The table should have a column named "Packages"
    And The table should have a column named "Configs"
    And The table should have a column named "Base Channel"
    And The table should have a column named "System Type"

  Scenario: Completeness of the All Systems table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    Then The table should have a column named "System"
    And The table should have a column named "Updates"
    And The table should have a column named "Patches"
    And The table should have a column named "Packages"
    And The table should have a column named "Configs"
    And The table should have a column named "Base Channel"
    And The table should have a column named "System Type"

  Scenario: Completeness of the Virtual System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    And I follow "Virtual Systems" in the left menu
    Then The table should have a column named "System"
    And The table should have a column named "Updates"
    And The table should have a column named "Status"
    And The table should have a column named "Base Software Channel"

  Scenario: Completeness of the Out-of-Date System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    And I follow "Out of Date" in the left menu
    Then The table should have a column named "System"
    And The table should have a column named "Updates"
    And The table should have a column named "Patches"
    And The table should have a column named "Packages"
    And The table should have a column named "Configs"
    And The table should have a column named "Base Channel"
    And The table should have a column named "System Type"

  Scenario: Completeness of the Requiring Reeboot System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    And I follow "Requiring Reboot" in the left menu
    Then The table should have a column named "System"
    And The table should have a column named "Updates"
    And The table should have a column named "Patches"
    And The table should have a column named "Packages"
    And The table should have a column named "Configs"
    And The table should have a column named "Base Channel"
    And The table should have a column named "System Type"

  Scenario: Completeness of the Non Compliant System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    And I follow "Non Compliant" in the left menu
    Then The table should have a column named "System"
    And The table should have a column named "Packages"
    And The table should have a column named "Base Channel"
    And I should see this client as link

  Scenario: Completeness of the Without System Type table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    And I follow "Without System Type" in the left menu
    Then The table should have a column named "System"
    And The table should have a column named "Updates"
    And The table should have a column named "Patches"
    And The table should have a column named "Packages"
    And The table should have a column named "Configs"
    And The table should have a column named "Base Channel"
    And The table should have a column named "System Type"

  Scenario: Completeness of the Ungrouped System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    And I follow "Ungrouped" in the left menu
    Then The table should have a column named "System"
    And The table should have a column named "Updates"
    And The table should have a column named "Patches"
    And The table should have a column named "Packages"
    And The table should have a column named "Configs"
    And The table should have a column named "Base Channel"
    And The table should have a column named "System Type"

  Scenario: Completeness of the Inactive System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    And I follow "Inactive" in the left menu
    Then The table should have a column named "System"
    And The table should have a column named "Updates"
    And The table should have a column named "Patches"
    And The table should have a column named "Packages"
    And The table should have a column named "Configs"
    And The table should have a column named "Base Channel"
    And The table should have a column named "System Type"

  Scenario: Completeness of the Recently Registered System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    And I follow "Recently Registered" in the left menu
    Then The table should have a column named "System"
    And The table should have a column named "Updates"
    And The table should have a column named "Base Channel"
    And The table should have a column named "Date Registered"
    And The table should have a column named "Registered by"
    And The table should have a column named "System Type"

  Scenario: Completeness of the Proxy System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    And I follow "Proxy" in the left menu
    Then The table should have a column named "System"
    And The table should have a column named "Updates"
    And The table should have a column named "Patches"
    And The table should have a column named "Packages"
    And The table should have a column named "Configs"
    And The table should have a column named "Base Channel"
    And The table should have a column named "System Type"

  Scenario: Completeness of the Duplicate System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    And I follow "Duplicate Systems" in the left menu
    Then The table should have a column named "System"
    And The table should have a column named "Last Checked in"

  Scenario: Completeness of the System currency table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    And I follow "System Currency" in the left menu
    Then The table should have a column named "System"
    And The table should have a column named "Security (Critical)"
    And The table should have a column named "Security (Important)"
    And The table should have a column named "Security (Moderate)"
    And The table should have a column named "Security (Low)"
    And The table should have a column named "Bug Fixes"
    And The table should have a column named "Enhancements"
    And The table should have a column named "Score"

  Scenario: Check Non Compliant Systems page
    Given I am on the Systems overview page of this client
    When I follow "Software"
    And I follow "Non Compliant" in the content area
    Then I should see a "Non Compliant" text
    And I should see a "The following packages are installed on this system and are not present in any of its channels." text
    And I should see a "Remove Packages" button
