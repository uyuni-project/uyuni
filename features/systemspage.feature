#
# Explore the systems page
#
Feature: Explore the main landing page
  In Order to validate completeness of the systems page
  As a authorized user
  I want to see all the texts and links

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
     And I should see a "Systems" text

  Scenario: Check sidebar link destination for Systems => Virtual Systems
    Given I am on the Systems page
      And I follow "Systems" in "sidenav"
      And I follow "Virtual Systems" in "sidenav"
    Then I should see a "Virtual Systems" text
     And I should see a "No Virtual Systems." text
     And the current path is "rhn/systems/VirtualSystemsList.do"

  Scenario: Check sidebar link destination for Systems => Out of Date
    Given I am on the Systems page
      And I follow "Systems" in "sidenav"
      And I follow "Out of Date" in "sidenav"
    Then I should see a "Out of Date Systems" text
     And I should see a "No systems." text
     And the current path is "rhn/systems/OutOfDate.do"

  Scenario: Check sidebar link destination for Systems => Unentitled
    Given I am on the Systems page
      And I follow "Systems" in "sidenav"
      And I follow "Unentitled" in "sidenav"
    Then I should see a "Unentitled Systems" text
     And I should see a "No systems." text
     And the current path is "rhn/systems/Unentitled.do"

  Scenario: Check sidebar link destination for Systems => Ungrouped
    Given I am on the Systems page
      And I follow "Systems" in "sidenav"
      And I follow "Ungrouped" in "sidenav"
    Then I should see a "Ungrouped Systems" text
     And I should see a "No systems." text
     And the current path is "rhn/systems/Ungrouped.do"

  Scenario: Check sidebar link destination for Systems => Inactive
    Given I am on the Systems page
      And I follow "Systems" in "sidenav"
      And I follow "Inactive" in "sidenav"
    Then I should see a "Inactive Systems" text
     And I should see a "No systems." text
     And the current path is "rhn/systems/Inactive.do"

  Scenario: Check sidebar link destination for Systems => Recently Registered
    Given I am on the Systems page
      And I follow "Systems" in "sidenav"
      And I follow "Recently Registered" in "sidenav"
    Then I should see a "Recently Registered Systems" text
     And I should see a "No systems." text
     And I should see a "View systems registered:" text
     And the current path is "rhn/systems/Registered.do"

  @wip
  Scenario: Check sidebar link destination for Systems => Duplicate Systems
    Given I am on the Systems page
      And I follow "Systems" in "sidenav"
      And I follow "Duplicate Systems" in "sidenav"
    Then I should see a "Duplicate System Profiles" text
     And I should see a "No systems." text
     And the current path is "rhn/systems/DuplicateIPList.do"

  Scenario: Check sidebar link destination for Systems => System Currency
    Given I am on the Systems page
      And I follow "Systems" in "sidenav"
      And I follow "System Currency" in "sidenav"
    Then I should see a "System Currency Report" text
     And I should see a "No systems." text
     And the current path is "rhn/systems/SystemCurrency.do"



