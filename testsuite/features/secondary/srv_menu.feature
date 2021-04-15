# Copyright (c) 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_visualization
Feature: Web UI - Main landing page menu, texts and links

  Background:
    Given I am authorized as "admin" with password "admin"

  Scenario: The Home menu
    When I follow the left menu "Home"
    And I should see a "Overview" link in the left menu
    Then I should see a "Overview" text
    And I should see a "Notification Messages" link in the left menu
    And I should see a "User Account" link in the left menu
    And I should see a "My Preferences" link in the left menu
    And I should see a "My Organization" link in the left menu

  Scenario: The user account submenu under the Home menu
    When I follow the left menu "Home > User Account"
    Then I should see a "My Account" text
    And I should see a "My Account" link in the left menu
    And I should see a "Addresses" link in the left menu
    And I should see a "Change Email" link in the left menu
    And I should see a "Account Deactivation" link in the left menu

  Scenario: The my orginization menu under the Home menu
    When I follow the left menu "Home > My Organization"
    Then I should see a "Organization Configuration" text
    And I should see a "Configuration" link in the left menu
    And I should see a "Organization Trusts" link in the left menu
    And I should see a "Configuration Channels" link in the left menu

  Scenario: The Salt menu
    When I follow the left menu "Salt"
    Then I should see a "Keys" text
    And I should see a "Keys" link in the left menu
    And I should see a "Remote Commands" link in the left menu
    And I should see a "Formula Catalog" link in the left menu

  Scenario: The content lifeycle menu
    When I follow the left menu "Content Lifecycle"
    Then I should see a "Content Lifecycle Projects" text
    And I should see a "Projects" link in the left menu
    And I should see a "Filters" link in the left menu

  Scenario: The images menu
    When I follow the left menu "Images"
    Then I should see a "Images" text
    And I should see a "Image List" link in the left menu
    And I should see a "Build" link in the left menu
    And I should see a "Profiles" link in the left menu
    And I should see a "Stores" link in the left menu

  Scenario: The audit menu
    When I follow the left menu "Audit"
    Then I should see a "CVE Audit" text
    And I should see a "CVE Audit" link in the left menu
    And I should see a "Subscription Matching" link in the left menu
    And I should see a "OpenSCAP" link in the left menu

  Scenario: The OpenSCAP submenu menu
    When I follow the left menu "Audit > OpenSCAP"
    Then I should see a "OpenSCAP Scans" text
    And I should see a "All Scans" link in the left menu
    And I should see a "XCCDF Diff" link in the left menu
    And I should see a "Advanced Search" link in the left menu

  Scenario: The configuration menu
    When I follow the left menu "Configuration"
    Then I should see a "Configuration Overview" text
    And I should see a "Overview" link in the left menu
    And I should see a "Channels" link in the left menu
    And I should see a "Files" link in the left menu

  Scenario: The files menu under the configuaration menu
    When I follow the left menu "Configuration > Files"
    Then I should see a "Centrally-Managed Configuration Files" text
    And I should see a "Centrally Managed" link in the left menu
    And I should see a "Locally Managed" link in the left menu

  Scenario: The systems submenu in the configuaration menu
    When I follow the left menu "Configuration > Systems"
    Then I should see a "Managed Systems" text
    And I should see a "Managed" link in the left menu
    And I should see a "Target" link in the left menu

  Scenario: The schedule menu
    When I follow the left menu "Schedule"
    Then I should see a "Pending Actions" text
    And I should see a "Pending Actions" link in the left menu
    And I should see a "Failed Actions" link in the left menu
    And I should see a "Archived Actions" link in the left menu
    And I should see a "Action Chains" link in the left menu

  Scenario: The users menu
    When I follow the left menu "Users"
    Then I should see a "Active Users" text
    And I should see a "Users" link in the left menu
    And I should see a "User List" link in the left menu
    And I should see a "Active" link in the left menu
    And I should see a "Deactivated" link in the left menu
    And I should see a "All" link in the left menu
    And I should see a "System Group Configuration" link in the left menu

  Scenario: The admin menu
    When I follow the left menu "Admin"
    Then I should see a "Setup Wizard" text
    And I should see a "Setup Wizard" link in the left menu
    And I should see a "Organizations" link in the left menu
    And I should see a "Users" link in the left menu
    And I should see a "Manager Configuration" link in the left menu
    And I should see a "ISS Configuration" link in the left menu
    And I should see a "Task Schedules" link in the left menu
    And I should see a "Task Engine Status" link in the left menu
    And I should see a "Show Tomcat Logs" link in the left menu

  Scenario: The Setup Wizard submenu of the admin menu
    When I follow the left menu "Admin > Setup Wizard"
    Then I should see a "HTTP Proxy" link in the left menu
    And I should see a "Organization Credentials" link in the left menu
    And I should see a "Products" link in the left menu

  Scenario: The manager configuration submenu of the admin menu
    When I follow the left menu "Admin > Manager Configuration"
    Then I should see a "Configuration - General Configuration" text
    And I should see a "General" link in the left menu
    And I should see a "Bootstrap Script" link in the left menu
    And I should see a "Organizations" link in the left menu
    And I should see a "Restart" link in the left menu
    And I should see a "Cobbler" link in the left menu
    And I should see a "Bare-metal systems" link in the left menu
    And I should see a "Monitoring" link in the left menu

  Scenario: The ISS configuration submenu of the admin menu
    When I follow the left menu "Admin > ISS Configuration"
    Then I should see a "ISS Configuration - Master Setup" text
    And I should see a "Master Setup" link in the left menu
    And I should see a "Slave Setup" link in the left menu

  Scenario: The Task Engine Status submenu of the admin menu
    When I follow the left menu "Admin > Task Engine Status"
    Then I should see a "Last Execution Times" link in the left menu
    And I should see a "Runtime Status" link in the left menu

  Scenario: The menu direct link accesses the first submenu level only
    When I follow the left menu "Patches > Patch List"
    Then I should see a "All Types" text in the content area
    And I should not see a "Patches Relevant to Your Systems" text in the content area
    When I follow the left menu "Configuration > Files"
    Then I should see a "Centrally-Managed Configuration Files" text in the content area
    And I should not see a "Locally Managed Configuration Files" text in the content area

  Scenario: Idempotency of complete menu path and direct link
    When I follow the left menu "Software > Manage"
    Then I should see a "Software Channel Management" text in the content area
    When I follow the left menu "Software > Manage > Channels"
    Then I should see a "Software Channel Management" text in the content area

  Scenario: Completeness of the side navigation bar and the content frame
    When I am on the Systems page
    Then I should see a "System Overview" text in the content area
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
    And I should see a "Download CSV" link
    And I should see a Sign Out link

  Scenario: Sidebar link destination for Systems
    When I follow the left menu "Systems > System List"
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

  Scenario: Sidebar link destination for Systems => Physical Systems
    When I follow the left menu "Systems > System List > Physical Systems"
    Then I should see a "Physical Systems" text
    And the current path is "/rhn/systems/PhysicalList.do"

  Scenario: Sidebar link destination for Systems => Virtual Systems
    When I follow the left menu "Systems > System List > Virtual Systems"
    Then I should see a "Virtual Systems" text
    And the current path is "/rhn/systems/VirtualList.do"

  Scenario: Sidebar link destination for Systems => Out of Date
    When I follow the left menu "Systems > System List > Out of Date"
    Then I should see a "Out of Date Systems" text
    And the current path is "/rhn/systems/OutOfDate.do"

  Scenario: Sidebar link destination for Systems => Requiring Reboot
    When I follow the left menu "Systems > System List > Requiring Reboot"
    Then I should see a "Systems Requiring Reboot" text
    And the current path is "/rhn/systems/RequiringReboot.do"

  Scenario: Sidebar link destination for Systems => Non Compliant
    When I follow the left menu "Systems > System List > Non Compliant"
    Then I should see a "Non Compliant Systems" text
    And the current path is "/rhn/systems/ExtraPackagesSystems.do"

  Scenario: Sidebar link destination for Systems => Without System Type
    When I follow the left menu "Systems > System List > Without System Type"
    Then I should see a "Systems without System Type" text
    And the current path is "/rhn/systems/Unentitled.do"

  Scenario: Sidebar link destination for Systems => Ungrouped
    When I follow the left menu "Systems > System List > Ungrouped"
    Then I should see a "Ungrouped Systems" text
    And the current path is "/rhn/systems/Ungrouped.do"

  Scenario: Sidebar link destination for Systems => Inactive
    When I follow the left menu "Systems > System List > Inactive"
    Then I should see a "Inactive Systems" text
    And the current path is "/rhn/systems/Inactive.do"

  Scenario: Sidebar link destination for Systems => Recently Registered
    When I follow the left menu "Systems > System List > Recently Registered"
    Then I should see a "Recently Registered Systems" text
    And I should see a "View systems registered:" text
    And the current path is "/rhn/systems/Registered.do"

  Scenario: Sidebar link destination for Systems => Proxy
    When I follow the left menu "Systems > System List > Proxy"
    Then I should see a "Proxy Servers" text
    And the current path is "/rhn/systems/ProxyList.do"

  Scenario: Sidebar link destination for Systems => Duplicate Systems
    When I follow the left menu "Systems > System List > Duplicate Systems"
    Then I should see a "Duplicate Systems" text
    And the current path is "/rhn/systems/DuplicateIPList.do"
    And I should see a "Duplicate IP Address" link
    And I should see a "Duplicate Hostname" link
    And I should see a "Duplicate IPv6 Address" link
    And I should see a "Duplicate MAC Address" link

  Scenario: Sidebar link destination for Systems => System Currency
    When I follow the left menu "Systems > System List > System Currency"
    Then I should see a "System Currency Report" text
    And the current path is "/rhn/systems/SystemCurrency.do"

  Scenario: Sidebar link destination for Systems => System Types
    When I follow the left menu "Systems > System List > System Types"
    Then I should see a "System Types" text
    And I should see a "Management:" text
    And I should see a "Salt:" text
    And I should see a "Foreign:" text
    And I should see a "Virtualization Host:" text
    And the current path is "/rhn/systems/SystemEntitlements.do"

  Scenario: Sidebar link destination for Systems => System Groups
    When I follow the left menu "Systems > System Groups"
    Then I should see a "System Groups" text
    And I should see a "Create Group" link
    And I should see a "Your organization has no system groups." text

  Scenario: Sidebar link destination for Systems => System Set Manager
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

  Scenario: Sidebar link destination for Systems => Advanced Search
    When I follow the left menu "Systems > Advanced Search"
    Then I should see a "Advanced Search" text

  Scenario: Sidebar link destination for Systems => Activation Keys
    When I follow the left menu "Systems > Activation Keys"
    Then I should see a "Activation Keys" text
    And I should see a "Create Key" link
    And I should see a "The following activation keys have been created for use by your organization." text

  Scenario: Sidebar link destination for Systems => Stored Profiles
    When I follow the left menu "Systems > Stored Profiles"
    Then I should see a "Stored Profiles" text
    And I should see a "No stored profiles." text

  Scenario: Sidebar link destination for Systems => Custom System Info
    When I follow the left menu "Systems > Custom System Info"
    Then I should see a "Custom System Info Keys" text
    And I should see a "Create Key" link
    And I should see a "No Custom Info Keys Found" text

  Scenario: Sidebar link destination for Systems => Autoinstallation
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

  Scenario: Sidebar link destination for Systems => Autoinstallation => Profiles
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    Then I should see a "Autoinstallation Profiles" text
    And I should see a "Create Kickstart Profile" link
    And I should see a "Upload Kickstart/Autoyast File" link

  Scenario: Sidebar link destination for Systems => Autoinstallation => Unprovisioned
    When I follow the left menu "Systems > Autoinstallation > Unprovisioned"
    Then I should see a "Unprovisioned Autoinstallation By IP" text
    And I should see a "No Ip Ranges Found" text

  Scenario: Sidebar link destination for Systems => Autoinstallation => GPG and SSL Keys
    When I follow the left menu "Systems > Autoinstallation > GPG and SSL Keys"
    Then I should see a "GPG Public Keys and SSL Certificates" text
    And I should see a "Create Stored Key/Cert" link
    And I should see a "Reference Guide" link
    And I should see a "RHN-ORG-TRUSTED-SSL-CERT" link

  Scenario: Sidebar link destination for Systems => Autoinstallation => Distributions
    When I follow the left menu "Systems > Autoinstallation > Distributions"
    Then I should see a "Autoinstallable Distributions" text
    And I should see a "No autoinstallable distributions available." text
    And I should see a "Create Distribution" link

  Scenario: Sidebar link destination for Systems => Autoinstallation => File Preservation
    When I follow the left menu "Systems > Autoinstallation > File Preservation"
    Then I should see a "File Preservation" text
    And I should see a "Reference Guide" link
    And I should see a "Create File Preservation List" link

  Scenario: Sidebar link destination for Systems => Autoinstallation => Autoinstallation Snippets
    When I follow the left menu "Systems > Autoinstallation > Autoinstallation Snippets"
    Then I should see a "Autoinstallation Snippets" text
    And I should see a "No autoinstallation snippets found." text
    And I should see a "Create Snippet" link
    And I should see a "Default Snippets" link in the content area
    And I should see a "Custom Snippets" link in the content area
    And I should see a "All Snippets" link in the content area

  Scenario: "Create Kickstart Profile" page Systems => Autoinstallation => Profiles => Create Kickstart Profile
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    And I follow "Create Kickstart Profile"
    Then I should see a "Step 1: Create Kickstart Profile" text

  Scenario: "Upload Kickstart/Autoyast File" page Systems => Autoinstallation => Profiles => Upload Kickstart/Autoyast File
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    And I follow "Upload Kickstart/Autoyast File"
    Then I should see a "Create Autoinstallation Profile" text
    And I should see a "File Contents:" text
    And I should see a "Autoinstallation Details" text

  Scenario: "Create kickstart distribution" page Systems => Autoinstallation => Distributions => create new kickstart distribution
    When I follow the left menu "Systems > Autoinstallation > Distributions"
    And I follow "Create Distribution"
    Then I should see a "Create Autoinstallable Distribution" text
    And I should see a "Distribution Label" text
