# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Explore the systems page
#
Feature: Explore the main landing page
  In Order to validate completeness of the systems page
  As a authorized user
  I want to see all the texts and links

  Scenario: Completeness of the System Overview table
    Given I am authorized
    When I follow "Systems"
    Then The table should have a column named "System"
     And The table should have a column named "Updates" 
     And The table should have a column named "Health" 
     And The table should have a column named "Patches" 
     And The table should have a column named "Packages" 
     And The table should have a column named "Configs" 
     And The table should have a column named "Base Channel" 
     And The table should have a column named "Entitlement" 

  Scenario: Completeness of the All Systems table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    Then The table should have a column named "System"
     And The table should have a column named "Updates" 
     And The table should have a column named "Health" 
     And The table should have a column named "Patches" 
     And The table should have a column named "Packages" 
     And The table should have a column named "Configs" 
     And The table should have a column named "Base Channel" 
     And The table should have a column named "Entitlement" 


  Scenario: Completeness of the Virtual System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    When I follow "Virtual Systems" in the left menu
    Then The table should have a column named "System"
     And The table should have a column named "Updates"
     And The table should have a column named "Status"
     And The table should have a column named "Base Software Channel"
 
  Scenario: Completeness of the Out-of-Date System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    When I follow "Out of Date" in the left menu
    Then The table should have a column named "System"
     And The table should have a column named "Updates" 
     And The table should have a column named "Health" 
     And The table should have a column named "Patches" 
     And The table should have a column named "Packages" 
     And The table should have a column named "Configs" 
     And The table should have a column named "Base Channel" 
     And The table should have a column named "Entitlement" 


  Scenario: Completeness of the Requiring Reeboot System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    When I follow "Requiring Reboot" in the left menu
    Then The table should have a column named "System"
     And The table should have a column named "Updates" 
     And The table should have a column named "Health" 
     And The table should have a column named "Patches" 
     And The table should have a column named "Packages" 
     And The table should have a column named "Configs" 
     And The table should have a column named "Base Channel" 
     And The table should have a column named "Entitlement" 

 
  Scenario: Completeness of the Non-Compliant System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    When I follow "Non-compliant Systems" in the left menu
    Then The table should have a column named "System"
     And The table should have a column named "Packages"
     And The table should have a column named "Base Channel"
 
  Scenario: Completeness of the Unentitled System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    When I follow "Unentitled" in the left menu
    Then The table should have a column named "System"
     And The table should have a column named "Updates" 
     And The table should have a column named "Health" 
     And The table should have a column named "Patches" 
     And The table should have a column named "Packages" 
     And The table should have a column named "Configs" 
     And The table should have a column named "Base Channel" 
     And The table should have a column named "Entitlement" 

  Scenario: Completeness of the Ungrouped System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    When I follow "Ungrouped" in the left menu
    Then The table should have a column named "System"
     And The table should have a column named "Updates" 
     And The table should have a column named "Health" 
     And The table should have a column named "Patches" 
     And The table should have a column named "Packages" 
     And The table should have a column named "Configs" 
     And The table should have a column named "Base Channel" 
     And The table should have a column named "Entitlement" 

  Scenario: Completeness of the Inactive System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    When I follow "Inactive" in the left menu
    Then The table should have a column named "System"
     And The table should have a column named "Updates" 
     And The table should have a column named "Health" 
     And The table should have a column named "Patches" 
     And The table should have a column named "Packages" 
     And The table should have a column named "Configs" 
     And The table should have a column named "Base Channel" 
     And The table should have a column named "Entitlement" 

  Scenario: Completeness of the Recently Registered System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    When I follow "Recently Registered" in the left menu
    Then The table should have a column named "System"
    Then The table should have a column named "Updates"
     And The table should have a column named "Base Channel"
     And The table should have a column named "Date Registered"
     And The table should have a column named "Registered by"
     And The table should have a column named "Entitlement"
 
  Scenario: Completeness of the Proxy System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    When I follow "Proxy" in the left menu
    Then The table should have a column named "System"
     And The table should have a column named "Updates" 
     And The table should have a column named "Health" 
     And The table should have a column named "Patches" 
     And The table should have a column named "Packages" 
     And The table should have a column named "Configs" 
     And The table should have a column named "Base Channel" 
     And The table should have a column named "Entitlement" 

 
  Scenario: Completeness of the Duplicate System table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    When I follow "Duplicate Systems" in the left menu
    Then The table should have a column named "System"
     And The table should have a column named "Last Checked in"
 
  Scenario: Completeness of the System currency table
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    When I follow "System Currency" in the left menu
    Then The table should have a column named "System"
     And The table should have a column named "Security (Critical)"
     And The table should have a column named "Security (Important)"
     And The table should have a column named "Security (Moderate)"
     And The table should have a column named "Security (Low)"
     And The table should have a column named "Bugfixes"
     And The table should have a column named "Enhancements"
     And The table should have a column named "Score"

