# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Explore the main landing page
  In Order to validate completeness of the landing page
  As a authorized user
  I want to see all the options and preferences

  Scenario: Completeness of the toplevel bar
    Given I am authorized
    When I go to the home page
    And I should see a "User" text
    And I should see a "Organization" text
    And I should see a "Preferences" link
    And I should see a Sign Out link

  Scenario: Completeness of the main navigation bar
    Given I am authorized
    When I go to the home page
    Then I should see a "Overview" link in the tab bar
    And I should see a "Systems" link
    And I should see a "Errata" link
    And I should see a "Channels" link
    And I should see a "Configuration" link
    And I should see a "Schedule" link
    And I should see a "Help" link

  Scenario: Completeness of the left sidebar
    Given I am authorized
    When I go to the home page
    Then I should see a "Your Account" link in the left menu
    And I should see a "Your Preferences" link in the left menu
    And I should see a "Locale Preferences" link in the left menu
    And I should see a "Subscription Management" link in the left menu
    And I should see a "Your Organization" link in the left menu

  Scenario: Completeness of the main content
    Given I am authorized
    When I go to the home page
    Then I should see a "Tasks" text
    And I should see a "Inactive Systems" text
    And I should see a "Most Critical Systems" text
    And I should see a "Recently Scheduled Actions" text
    And I should see a "Relevant Security Errata" text
    And I should see a "System Group Name" text
    And I should see a "Recently Registered Systems" text
