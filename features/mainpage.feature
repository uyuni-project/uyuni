# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Explore the main landing page
  In Order to validate completeness of the landing page
  As a authorized user
  I want to see all the options and preferences

  Scenario: Accessing the Login page
    Given I am not authorized
    When I go to the home page
    Then I should see something

  Scenario: Accessing the About page
    Given I am not authorized
    When I go to the home page
    And I follow "About"
    Then I should see a "About SUSE Manager" text

  Scenario: Accessing the Copyright Notice
    Given I am not authorized
    When I go to the home page
    And I follow "Copyright Notice"
    Then I should see a "Copyright (c) 2011 - 2016 SUSE LLC." text

  Scenario: Accessing the EULA
    Given I am not authorized
    When I go to the home page
    And I follow "Copyright Notice"
    And I follow "SUSE MANAGER LICENSE AGREEMENT"
    Then I should see a "SUSE Manager License Agreement" text

  Scenario: Log into the host
    Given I am not authorized
    When I go to the home page
    And I enter "testing" as "username"
    And I enter "testing" as "password"
    And I click on "Sign In"
    Then I should be logged in

  Scenario: Log out of the host
    Given I am authorized
    When I sign out
    Then I should not be authorized

  Scenario: Completeness of the toplevel bar
    Given I am authorized
    When I go to the home page
    And I should see a "User" text
    And I should see a "Organization" text
    And I should see a "Preferences" link
    And I should see a Sign Out link

  Scenario: Completeness of the main navigation bar
    Given I am authorized
    Then I should see a "Overview" link
    And I should see a "Systems" link
    And I should see a "Errata" link
    And I should see a "Channels" link
    And I should see a "Configuration" link
    And I should see a "Schedule" link
    And I should see a "Help" link

  Scenario: Completeness of the left sidebar
    Given I am authorized
    When I follow "User Account" in the left menu
    Then I should see a "Your Account" link in the left menu
    And I should see a "Your Preferences" link in the left menu
    And I should see a "Locale Preferences" link in the left menu
    And I should see a "Your Organization" link in the left menu

  Scenario: Completeness of the main content
    Given I am authorized
    Then I should see a "Tasks" text
    And I should see a "Inactive Systems" text
    And I should see a "Most Critical Systems" text
    And I should see a "Recently Scheduled Actions" text
    And I should see a "Relevant Security Errata" text
    And I should see a "System Group Name" text
    And I should see a "Recently Registered Systems" text
