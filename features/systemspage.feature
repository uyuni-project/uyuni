#
# Explore the systems page
#
Feature: Explore the main landing page
  In Order to validate completeness of the systems page
  As a authorized user
  I want to see all the texts and links

  @first 
  Scenario: Completeness of the side navigation bar and the content frame
    Given I am authorized
    When I follow "Systems"
    Then I should see a "System Overview" text
     And I should see a "No systems." text
     And I should see a "Overview" link in "sidenav"
     And I should see a "Systems" link in "sidenav"
     And I should see a "System Groups" link in "sidenav"
     And I should see a "System Set Manager" link in "sidenav"
     And I should see a "Advanced Search" link in "sidenav"
     And I should see a "Activation Keys" link in "sidenav"
     And I should see a "Stored Profiles" link in "sidenav"
     And I should see a "Custom System Info" link in "sidenav"
     And I should see a "Kickstart" link in "sidenav"
     And I should see a "View System Groups" link
     And I should see a "Download CSV" link
     And I should see a "Sign Out" link

  @second
  Scenario: Completeness of the main navigation bar
    Given I am authorized
    When I follow "Systems"
    Then I should see a "Overview" link in "mainNavWrap"
     And I should see a "Systems" link in "mainNavWrap"
     And I should see a "Errata" link in "mainNavWrap"
     And I should see a "Channels" link in "mainNavWrap"
     And I should see a "Audit" link in "mainNavWrap"
     And I should see a "Configuration" link in "mainNavWrap"
     And I should see a "Schedule" link in "mainNavWrap"
     And I should see a "Users" link in "mainNavWrap"
     And I should see a "Monitoring" link in "mainNavWrap"
     And I should see a "Help" link in "mainNavWrap"

  @third
  Scenario: Check sidebar link destination for Systems
    Given I am on the Systems page
    When I follow "Systems" in "sidenav"
    Then I should see a "All" link in "sidenav"
     And I should see a "Virtual Systems" link in "sidenav"
     And I should see a "Out of Date" link in "sidenav"
     And I should see a "Unentitled" link in "sidenav"
     And I should see a "Ungrouped" link in "sidenav"
     And I should see a "Inactive" link in "sidenav"
     And I should see a "Recently Registered" link in "sidenav"
     And I should see a "Duplicate Systems" link in "sidenav"
     And I should see a "System Currency" link in "sidenav"

