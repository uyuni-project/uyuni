# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Bare metal discovery

  Scenario: Delete the normal traditional client for bare metal feature
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I should see a "System profile" text
    And I should see a "has been deleted" text

  Scenario: Enable bare metal discovery
    Given I am authorized as "admin" with password "admin"
    And I follow "Admin"
    And I follow "Manager Configuration" in the left menu
    And I follow "General" in the left menu
    When I follow "Bare-metal systems" in the content area
    Then I should see a "Allows $PRODUCT to automatically add bare-metal systems capable of PXE booting to an organization." text
    And I should see a "Enable adding to this organization" button
    When I click on "Enable adding to this organization"
    Then I should see a "Automatic bare-metal system discovery has been successfully enabled" text
    And the PXE default profile should be enabled

  Scenario: Register a client for bare metal discovery
    When I register using "1-spacewalk-bootstrap-activation-key" key
    Then I should see "sle-client" in spacewalk

  Scenario: Check registration values of client
    Given I am on the Systems overview page of this "sle-client"
    Then I should see a "System Info" text
    And I should see a "Edit These Properties" link
    And I should not see a "[Management]" text

  Scenario: See the client in unprovisioned systems list
    Given I am on the Systems page
    And I click System List, under Systems node 
    And I follow "Unprovisioned Systems" in the left menu
    Then I should see a "Unprovisioned Systems" text
    And I should see a "Detected on" text
    And I should see a "Number of CPUs" text
    And I should see a "2" text
    And I should see a "Clock frequency" text
    And I should see the CPU frequency of the client
    And I should see a "RAM" text
    And I check the ram value
    And I should see a "Number of disks" text
    And I should see a "1" text
    And I should see a "MAC Address(es)" text
    And I check the MAC address value

  Scenario: Check unprovisioned system details
    Given I am on the Systems page
    And I click System List, under Systems node 
    When I follow this "sle-client" link
    Then I should see a "Details" link in the content area
    And I should not see a "Software" link in the content area
    And I should not see a "Configuration" link in the content area
    And I should see a "Provisioning" link in the content area
    And I should not see a "Groups" link in the content area
    And I should not see a "Events" link in the content area
    And I should see a "Overview" link in the content area
    And I should see a "Properties" link in the content area
    And I should not see a "Remote Command" link in the content area
    And I should not see a "Reactivation" link in the content area
    And I should see a "Hardware" link in the content area
    And I should see a "Migrate" link in the content area
    And I should see a "Notes" link in the content area
    And I should not see a "Custom Info" link in the content area

  Scenario: Check Provisioning page for this client
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Provisioning" in the content area
    Then I should see a "Autoinstallation" link in the content area
    And I should not see a "Snapshots" link in the content area
    And I should not see a "Snapshot Tags" link in the content area
    And I should see a "Power Management" link in the content area
    And I should see a "Schedule" link in the content area

  Scenario: Check SSM with bare metal system
    Given I am on the Systems page
    When I check the "sle-client" client
    And I wait for "30" seconds
    Then I am on System Set Manager Overview
  
  Scenario: Check SSM page for bare metal system
    Given I am authorized as "admin" with password "admin"
    And I am on System Set Manager Overview
    Then I should see a "List the systems" link in the content area
    And I should see a "Autoinstall" link in the content area
    And I should see a "Configure power management" link in the content area
    And I should see a "power management operations" link in the content area
    And I should see a "Delete" link in the content area
    And I should see a "Migrate" link in the content area
    And I should not see a "Patches" link in the content area
    And I should not see a "Packages" link in the content area
    And I should see a "Groups" link in the content area
    And I should not see a "Channels" link in the content area
    And I should not see a "Audit" link in the content area
    And I follow "Clear"

  Scenario: Cleanup: delete the bare metal system profile
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"

  Scenario: Cleanup: disable bare metal discovery
    Given I am authorized as "admin" with password "admin"
    And I follow "Admin"
    And I follow "Manager Configuration" in the left menu
    And I follow "General" in the left menu
    When I follow "Bare-metal systems" in the content area
    Then I should see a "Allows $PRODUCT to automatically add bare-metal systems capable of PXE booting to an organization." text
    And I should see a "Disable adding to this organization" button
    When I click on "Disable adding to this organization"
    Then I should see a "Automatic bare-metal system discovery has been successfully disabled" text
    And the PXE default profile should be disabled
  
  Scenario: Cleanup: register a traditional client after bare metal tests
    When I register using "1-SUSE-DEV-x86_64" key
    Then I should see "sle-client" in spacewalk
