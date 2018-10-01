# Copyright (c) 2010-2017 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Main landing page texts and links

  Background:
    Given I am authorized
    And I follow "Home" in the left menu
    And I follow "Systems" in the left menu
    And I follow "Overview" in the left menu

  Scenario: Completeness of the side navigation bar and the content frame
    Then I should see a "System Overview" text
    And I should see a "No systems." text
    And I should see a "Overview" link in the left menu
    And I should see a "Systems" link in the left menu
    And I should see a "System Groups" link in the left menu
    And I should see a "System Set Manager" link in the left menu
    And I should see a "Advanced Search" link in the left menu
    And I should see a "Activation Keys" link in the left menu
    And I should see a "Stored Profiles" link in the left menu
    And I should see a "Custom System Info" link in the left menu
    And I should see a "Autoinstallation" link in the left menu
    And I should see a "View System Groups" link
    And I should see a "Software Crashes" link in the left menu
    And I should see a "Download CSV" link
    And I should see a Sign Out link

  Scenario: Check sidebar link destination for Systems
    When I click System List, under Systems node
    Then I should see a "All" link in the left menu
    And I should see a "Physical Systems" link in the left menu
    And I should see a "Virtual Systems" link in the left menu
    And I should see a "Out of Date" link in the left menu
    And I should see a "Requiring Reboot" link in the left menu
    And I should see a "Non Compliant" link in the left menu
    And I should see a "Without System Type" link in the left menu
    And I should see a "Ungrouped" link in the left menu
    And I should see a "Inactive" link in the left menu
    And I should see a "Recently Registered" link in the left menu
    And I should see a "Proxy" link in the left menu
    And I should see a "Duplicate Systems" link in the left menu
    And I should see a "System Currency" link in the left menu
    And I should see a "System Types" link in the left menu
    And I should see a "Systems" text

  Scenario: Check sidebar link destination for Systems => Physical Systems
    When I click System List, under Systems node
    And I follow "Physical Systems" in the left menu
    Then I should see a "Physical Systems" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/PhysicalList.do"

  Scenario: Check sidebar link destination for Systems => Virtual Systems
    When I click System List, under Systems node
    And I follow "Virtual Systems" in the left menu
    Then I should see a "Virtual Systems" text
    And I should see a "No Virtual Systems." text
    And the current path is "/rhn/systems/VirtualList.do"

  Scenario: Check sidebar link destination for Systems => Out of Date
    When I click System List, under Systems node
    And I follow "Out of Date" in the left menu
    Then I should see a "Out of Date Systems" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/OutOfDate.do"

  Scenario: Check sidebar link destination for Systems => Requiring Reboot
    When I click System List, under Systems node
    And I follow "Requiring Reboot" in the left menu
    Then I should see a "Systems Requiring Reboot" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/RequiringReboot.do"

  Scenario: Check sidebar link destination for Systems => Non Compliant
    When I click System List, under Systems node
    And I follow "Non Compliant" in the left menu
    Then I should see a "Non Compliant Systems" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/ExtraPackagesSystems.do"

  Scenario: Check sidebar link destination for Systems => Without System Type
    When I click System List, under Systems node
    And I follow "Without System Type" in the left menu
    Then I should see a "Systems without System Type" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/Unentitled.do"

  Scenario: Check sidebar link destination for Systems => Ungrouped
    When I click System List, under Systems node
    And I follow "Ungrouped" in the left menu
    Then I should see a "Ungrouped Systems" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/Ungrouped.do"

  Scenario: Check sidebar link destination for Systems => Inactive
    When I click System List, under Systems node
    And I follow "Inactive" in the left menu
    Then I should see a "Inactive Systems" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/Inactive.do"

  Scenario: Check sidebar link destination for Systems => Recently Registered
    When I click System List, under Systems node
    And I follow "Recently Registered" in the left menu
    Then I should see a "Recently Registered Systems" text
    And I should see a "No systems." text
    And I should see a "View systems registered:" text
    And the current path is "/rhn/systems/Registered.do"

  Scenario: Check sidebar link destination for Systems => Proxy
    When I click System List, under Systems node
    And I follow "Proxy" in the left menu
    Then I should see a "Proxy Servers" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/ProxyList.do"

  Scenario: Check sidebar link destination for Systems => Duplicate Systems
    When I click System List, under Systems node
    And I follow "Duplicate Systems" in the left menu
    Then I should see a "Duplicate Systems" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/DuplicateIPList.do"
    And I should see a "Duplicate IP Address" link
    And I should see a "Duplicate Hostname" link
    And I should see a "Duplicate IPv6 Address" link
    And I should see a "Duplicate MAC Address" link

  Scenario: Check sidebar link destination for Systems => System Currency
    When I click System List, under Systems node
    And I follow "System Currency" in the left menu
    Then I should see a "System Currency Report" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/SystemCurrency.do"

  Scenario: Check sidebar link destination for Systems => System Types
    When I click System List, under Systems node
    And I follow "System Types" in the left menu
    Then I should see a "System Types" text
    And I should see a "Management:" text
    And I should see a "Salt:" text
    And I should see a "Foreign:" text
    And I should see a "Virtualization Host:" text
    And the current path is "/rhn/systems/SystemEntitlements.do"

  Scenario: Check sidebar link destination for Systems => System Groups
    When I follow "System Groups" in the left menu
    Then I should see a "System Groups" text
    And I should see a "Create Group" link
    And I should see a "Your organization has no system groups." text

  Scenario: Check sidebar link destination for Systems => System Set Manager
    When I am on System Set Manager Overview
    Then I should see a "System Set Manager" text
    And I should see a "Task Log" link in the left menu
    And I should see a "Overview" link in the content area
    And I should see a "Systems" link in the content area
    And I should see a "Patches" link in the content area
    And I should see a "Packages" link in the content area
    And I should see a "Groups" link in the content area
    And I should see a "Channels" link in the content area
    And I should see a "Configuration" link in the content area
    And I should see a "Provisioning" link in the content area
    And I should see a "Misc" link in the content area

  Scenario: Check sidebar link destination for Systems => Advanced Search
    When I follow "Advanced Search" in the left menu
    Then I should see a "Advanced Search" text

  Scenario: Check sidebar link destination for Systems => Activation Keys
    When I follow "Activation Keys" in the left menu
    Then I should see a "Activation Keys" text
    And I should see a "Create Key" link
    And I should see a "No activation keys available" text

  Scenario: Check sidebar link destination for Systems => Stored Profiles
    When I follow "Stored Profiles" in the left menu
    Then I should see a "Stored Profiles" text
    And I should see a "No stored profiles." text

  Scenario: Check sidebar link destination for Systems => Custom System Info
    When I follow "Custom System Info" in the left menu
    Then I should see a "Custom System Info Keys" text
    And I should see a "Create Key" link
    And I should see a "No Custom Info Keys Found" text

  Scenario: Check sidebar link destination for Systems => Autoinstallation
    When I am on Autoinstallation Overview page
    Then I should see a "Autoinstallation Overview" text
    And I should see a "Profiles" link in the left menu
    And I should see a "Unprovisioned" link in the left menu
    And I should see a "GPG and SSL Keys" link in the left menu
    And I should see a "Distributions" link in the left menu
    And I should see a "File Preservation" link in the left menu
    And I should see a "Autoinstallation Snippets" link in the left menu
    And I should see a "Create Kickstart Profile" link
    And I should see a "Upload Kickstart/Autoyast File" link
    And I should see a "View a List of Autoinstallation Profiles" link
    And I should see a "Create a New Kickstart Profile" link
    And I should see a "Upload a New Kickstart/AutoYaST File" link

  Scenario: Check sidebar link destination for Systems => Autoinstallation => Profiles
    When I follow "Autoinstallation" in the left menu
    And I follow "Profiles" in the left menu
    Then I should see a "Autoinstallation Profiles" text
    And I should see a "Create Kickstart Profile" link
    And I should see a "Upload Kickstart/Autoyast File" link

  Scenario: Check sidebar link destination for Systems => Autoinstallation => Unprovisioned
    When I follow "Autoinstallation" in the left menu
    And I follow "Unprovisioned" in the left menu
    Then I should see a "Unprovisioned Autoinstallation By IP" text
    And I should see a "No Ip Ranges Found" text

  Scenario: Check sidebar link destination for Systems => Autoinstallation => GPG and SSL Keys
    When I follow "Autoinstallation" in the left menu
    And I follow "GPG and SSL Keys" in the left menu
    Then I should see a "GPG Public Keys and SSL Certificates" text
    And I should see a "Create Stored Key/Cert" link
    And I should see a "Reference Guide" link
    And I should see a "RHN-ORG-TRUSTED-SSL-CERT" link

  Scenario: Check sidebar link destination for Systems => Autoinstallation => Distributions
    When I follow "Autoinstallation" in the left menu
    And I follow "Distributions" in the left menu
    Then I should see a "Autoinstallable Distributions" text
    And I should see a "No autoinstallable distributions available." text
    And I should see a "Create Distribution" link

  Scenario: Check sidebar link destination for Systems => Autoinstallation => File Preservation
    When I follow "Autoinstallation" in the left menu
    And I follow "File Preservation" in the left menu
    Then I should see a "File Preservation" text
    And I should see a "Reference Guide" link
    And I should see a "Create File Preservation List" link

  Scenario: Check sidebar link destination for Systems => Autoinstallation => Autoinstallation Snippets
    When I follow "Autoinstallation" in the left menu
    And I follow "Autoinstallation Snippets" in the left menu
    Then I should see a "Autoinstallation Snippets" text
    And I should see a "No autoinstallation snippets found." text
    And I should see a "Create Snippet" link
    And I should see a "Default Snippets" link in the content area
    And I should see a "Custom Snippets" link in the content area
    And I should see a "All Snippets" link in the content area

  Scenario: Check "Create Kickstart Profile" page Systems => Autoinstallation => Profiles => Create Kickstart Profile
    When I follow "Autoinstallation" in the left menu
    And I follow "Profiles" in the left menu
    And I follow "Create Kickstart Profile"
    Then I should see a "Step 1: Create Kickstart Profile" text

  Scenario: Check "Upload Kickstart/Autoyast File" page Systems => Autoinstallation => Profiles => Upload Kickstart/Autoyast File
    When I follow "Autoinstallation" in the left menu
    And I follow "Profiles" in the left menu
    And I follow "Upload Kickstart/Autoyast File"
    Then I should see a "Create Autoinstallation Profile" text
    And I should see a "File Contents:" text
    And I should see a "Autoinstallation Details" text

  Scenario: Check "create kickstart distribution" page Systems => Autoinstallation => Distributions => create new kickstart distribution
    When I follow "Autoinstallation" in the left menu
    And I follow "Distributions" in the left menu
    And I follow "Create Distribution"
    Then I should see a "Create Autoinstallable Distribution" text
    And I should see a "Distribution Label" text
