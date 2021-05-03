# Copyright (c) 2015-2018 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_visualization
Feature: Main landing page options and preferences

  Background: Clear browser cookies
    When I clear browser cookies

  Scenario: Access the Login page
    Given I am not authorized
    When I go to the home page
    Then I should see something

  Scenario: Access the About page
    Given I am not authorized
    When I go to the home page
    And I follow "About"
    Then I should see a "About $PRODUCT" text

  Scenario: Access the Copyright Notice
    Given I am not authorized
    When I go to the home page
    And I follow "Copyright Notice"
    Then I should see a "Copyright (c) 2011 - 2021 SUSE LLC." text

@susemanager
  Scenario: Access the EULA
    Given I am not authorized
    When I go to the home page
    And I follow "Copyright Notice"
    And I follow "SUSE MANAGER LICENSE AGREEMENT"
    Then I should see a "SUSE Manager License Agreement" text

  Scenario: Log into SUSE Manager
    Given I am not authorized
    When I go to the home page
    And I enter "testing" as "username"
    And I enter "testing" as "password"
    And I click on "Sign In"
    Then I should be logged in

  Scenario: Log out of SUSE Manager
    Given I am authorized
    When I sign out
    Then I should not be authorized

  Scenario: Top level bar
    Given I am authorized
    When I go to the home page
    And I should see a "Manage" link
    And I should see a "admin" text
    And I should see a "SUSE Test" link
    And I should see a "Preferences" link
    And I should see a Sign Out link

  Scenario: Main menu as regular user
    Given I am authorized
    Then I should see a "Home" link
    And I should see a "Systems" link
    And I should see a "Salt" link
    And I should see a "Images" link
    And I should see a "Patches" link
    And I should see a "Software" link
    And I should see a "Audit" link
    And I should see a "Configuration" link
    And I should see a "Schedule" link
    And I should see a "Users" link
    And I should not see a "Admin" link
    And I should see a "Help" link
    And I should see a "External Links" link

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Main menu as administrator
    Then I should see a "Home" link
    And I should see a "Systems" link
    And I should see a "Salt" link
    And I should see a "Images" link
    And I should see a "Patches" link
    And I should see a "Software" link
    And I should see a "Audit" link
    And I should see a "Configuration" link
    And I should see a "Schedule" link
    And I should see a "Users" link
    And I should see a "Admin" link
    And I should see a "Help" link
    And I should see a "External Links" link

  Scenario: Main content
    Given I am authorized
    Then I should see a "Tasks" text
    And I should see a "Inactive Systems" text
    And I should see a "Most Critical Systems" text
    And I should see a "Recently Scheduled Actions" text
    And I should see a "Relevant Security Patches" text
    And I should see a "System Group Name" text
    And I should see a "Recently Registered Systems" text
