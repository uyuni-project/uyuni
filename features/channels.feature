#@wip
Feature: Explore the Channels page
  In Order to validate completeness of the Channels page and it's subpages
  As an authorized user
  I want to see all the texts and links

  Background:
    Given I am testing channels

  Scenario: Completeness of Channels page
    When I follow "Channels"
    Then I should see a "Full Software Channel List" text
     And I should see a "Software Channels" link in the left menu
     And I should see a "All Channels" link in the left menu
     And I should see a "Popular Channels" link in the left menu
     And I should see a "My Channels" link in the left menu
     And I should see a "Shared Channels" link in the left menu
     And I should see a "Retired Channels" link in the left menu
     And I should see a "Package Search" link in the left menu
     And I should see a "Manage Software Channels" link in the left menu
     And I should see a "All Channels" link in the tab bar
     And I should see a "Popular Channels" link in the tab bar
     And I should see a "My Channels" link in the tab bar
     And I should see a "Shared Channels" link in the tab bar
     And I should see a "Retired Channels" link in the tab bar

  Scenario: Completeness of Channels page
    When I follow "Channels"
    When I follow "Popular Channels" in the left menu
    Then I should see a "Popular Channels" text

