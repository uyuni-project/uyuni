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

  Scenario: Check sidebar link destination for Systems => Duplicate Systems
    Given I am on the Systems page
      And I follow "Systems" in "sidenav"
      And I follow "Duplicate Systems" in "sidenav"
    Then I should see a "Duplicate System Profiles" text
     And I should see a "No systems." text
     And the current path is "rhn/systems/DuplicateIPList.do"
     And I should see a "Duplicate IP Address" link
     And I should see a "Duplicate Hostname" link
     And I should see a "Duplicate MAC Address" link
     And I should see a "Show All" link
     And I should see a "Hide All" link

  Scenario: Check sidebar link destination for Systems => System Currency
    Given I am on the Systems page
      And I follow "Systems" in "sidenav"
      And I follow "System Currency" in "sidenav"
    Then I should see a "System Currency Report" text
     And I should see a "No systems." text
     And the current path is "rhn/systems/SystemCurrency.do"

  Scenario: Check sidebar link destination for Systems => System Groups
    Given I am on the Systems page
      And I follow "System Groups" in "sidenav"
    Then I should see a "System Groups" text
     And I should see a "create new group" link
     And I should see a "Your organization has no system groups." text

  @failed
  Scenario: Check sidebar link destination for Systems => System Set Manager
    Given I am on the Systems page
      And I follow "System Set Manager" in "sidenav"
    Then I should see a "System Set Manager" text
     And I should see a "Status" link in "sidenav"
     And I should see a "Overview" link in "content-nav"
     And I should see a "Systems" link in "content-nav"
     And I should see a "Errata" link in "content-nav"
     And I should see a "Packages" link in "content-nav"
     And I should see a "Groups" link in "content-nav"
     And I should see a "Channels" link in "content-nav"
     And I should see a "Configuration" link in "content-nav"
     And I should see a "Provisioning" link in "content-nav"
     And I should see a "Misc" link in "content-nav"

  Scenario: Check sidebar link destination for Systems => Advanced Search
    Given I am on the Systems page
      And I follow "Advanced Search" in "sidenav"
    Then I should see a "System Search" text

  Scenario: Check sidebar link destination for Systems => Activation Keys
    Given I am on the Systems page
      And I follow "Activation Keys" in "sidenav"
    Then I should see a "Activation Keys" text
     And I should see a "create new key" link
     And I should see a "No activation keys available" text

  Scenario: Check sidebar link destination for Systems => Stored Profiles
    Given I am on the Systems page
      And I follow "Stored Profiles" in "sidenav"
    Then I should see a "Stored Profiles" text
     And I should see a "No stored profiles." text

  Scenario: Check sidebar link destination for Systems => Custom System Info
    Given I am on the Systems page
      And I follow "Custom System Info" in "sidenav"
    Then I should see a "Custom System Info Keys" text
     And I should see a "create new key" link
     And I should see a "No Custom Info Keys Found" text

  Scenario: Check sidebar link destination for Systems => Kickstart
    Given I am on the Systems page
      And I follow "Kickstart" in "sidenav"
    Then I should see a "Kickstart Overview" text
     And I should see a "Profiles" link in "sidenav"
     And I should see a "Bare Metal" link in "sidenav"
     And I should see a "GPG and SSL Keys" link in "sidenav"
     And I should see a "Distributions" link in "sidenav"
     And I should see a "File Preservation" link in "sidenav"
     And I should see a "Kickstart Snippets" link in "sidenav"
     And I should see a "create new kickstart profile" link
     And I should see a "upload new kickstart file" link
     And I should see a "View a List of Kickstart Profiles" link
     And I should see a "Create a New Kickstart Profile" link
     And I should see a "Upload a New Kickstart File" link

  @failed
  Scenario: Check sidebar link destination for Systems => Kickstart => Profiles
    Given I am on the Systems page
      And I follow "Kickstart" in "sidenav"
      And I follow "Profiles" in "sidenav"
    Then I should see a "Kickstart Profiles" text
     And I should see a "create new kickstart profile" link
     And I should see a "upload new kickstart file" link

  Scenario: Check sidebar link destination for Systems => Kickstart => Bare Metal
    Given I am on the Systems page
      And I follow "Kickstart" in "sidenav"
      And I follow "Bare Metal" in "sidenav"
    Then I should see a "Bare Metal Kickstart By IP" text
     And I should see a "No Ip Ranges Found" text

  Scenario: Check sidebar link destination for Systems => Kickstart => GPG and SSL Keys
    Given I am on the Systems page
      And I follow "Kickstart" in "sidenav"
      And I follow "GPG and SSL Keys" in "sidenav"
    Then I should see a "GPG Public Keys and SSL Certificates" text
     And I should see a "create new stored key/cert" link
     And I should see a "RHN Reference Guide" link
     And I should see a "RHN-ORG-TRUSTED-SSL-CERT" link

  Scenario: Check sidebar link destination for Systems => Kickstart => Distributions
    Given I am on the Systems page
      And I follow "Kickstart" in "sidenav"
      And I follow "Distributions" in "sidenav"
    Then I should see a "Kickstartable Distributions" text
     And I should see a "No kickstartable distributions available." text
     And I should see a "create new distribution" link

  Scenario: Check sidebar link destination for Systems => Kickstart => File Preservation
    Given I am on the Systems page
      And I follow "Kickstart" in "sidenav"
      And I follow "File Preservation" in "sidenav"
    Then I should see a "File Preservation" text
     And I should see a "RHN Reference Guide" link
     And I should see a "create new file preservation list" link

  Scenario: Check sidebar link destination for Systems => Kickstart => Kickstart Snippets
    Given I am on the Systems page
      And I follow "Kickstart" in "sidenav"
      And I follow "Kickstart Snippets" in "sidenav"
    Then I should see a "Kickstart Snippets" text
     And I should see a "No kickstart snippets found." text
     And I should see a "create new snippet" link
     And I should see a "Default Snippets" link in "content-nav"
     And I should see a "Custom Snippets" link in "content-nav"
     And I should see a "All Snippets" link in "content-nav"















