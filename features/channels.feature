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
     And I should see a "Software Channels" link in element "sidenav"
     And I should see a "All Channels" link in element "sidenav"
     And I should see a "Popular Channels" link in element "sidenav"
     And I should see a "My Channels" link in element "sidenav"
     And I should see a "Shared Channels" link in element "sidenav"
     And I should see a "Retired Channels" link in element "sidenav"
     And I should see a "Package Search" link in element "sidenav"
     And I should see a "Manage Software Channels" link in element "sidenav"
     And I should see a "All Channels" link in element "content-nav"
     And I should see a "Popular Channels" link in element "content-nav"
     And I should see a "My Channels" link in element "content-nav"
     And I should see a "Shared Channels" link in element "content-nav"
     And I should see a "Retired Channels" link in element "content-nav"

  Scenario: Completeness of Channels page
    When I follow "Channels"
    When I follow "Popular Channels" in element "sidenav"
    Then I should see a "Popular Channels" text

  Scenario: Check Packages in Test Base Channel
    When I follow "Channels"
     And I follow "Test Base Channel"
     And I follow "Packages"
    Then I should see some packages


