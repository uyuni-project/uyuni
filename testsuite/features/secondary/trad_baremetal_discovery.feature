# Copyright (c) 2015-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_traditional_client
Feature: Bare metal discovery

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Delete the normal traditional client for bare metal feature
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    Then I should see a "System profile" text
    When I wait until I see "has been deleted" text
    Then "sle_client" should not be registered

  Scenario: Enable bare metal discovery
    When I follow the left menu "Admin > Manager Configuration > General"
    When I follow "Bare-metal systems" in the content area
    Then I should see a "Allows $PRODUCT to automatically add bare-metal systems capable of PXE booting to an organization." text
    And I should see a "Enable adding to this organization" button
    When I click on "Enable adding to this organization"
    Then I should see a "Automatic bare-metal system discovery has been successfully enabled" text
    And the PXE default profile should be enabled

  Scenario: Register a client for bare metal discovery
    When I bootstrap traditional client "sle_client" using bootstrap script with activation key "1-spacewalk-bootstrap-activation-key" from the proxy
    Then I should see "sle_client" via spacecmd

  Scenario: Check registration values of client
    Given I am on the Systems overview page of this "sle_client"
    Then I should see a "System Info" text
    And I should see a "Edit These Properties" link
    And I should not see a "[Management]" text

  Scenario: See the client in unprovisioned systems list
    When I follow the left menu "Systems > System List > Unprovisioned Systems"
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
    When I follow the left menu "Systems > System List"
    When I follow this "sle_client" link
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
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Provisioning" in the content area
    Then I should see a "Autoinstallation" link in the content area
    And I should not see a "Snapshots" link in the content area
    And I should not see a "Snapshot Tags" link in the content area
    And I should see a "Power Management" link in the content area
    And I should see a "Schedule" link in the content area

  Scenario: Check SSM with bare metal system
    Given I am on the Systems page
    When I check the "sle_client" client
    And I wait for "30" seconds
    Then I am on System Set Manager Overview

  Scenario: Check SSM page for bare metal system
    Given I am on System Set Manager Overview
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
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_client" should not be registered

  Scenario: Cleanup: disable bare metal discovery
    When I follow the left menu "Admin > Manager Configuration > General"
    And I follow "Bare-metal systems" in the content area
    Then I should see a "Allows $PRODUCT to automatically add bare-metal systems capable of PXE booting to an organization." text
    And I should see a "Disable adding to this organization" button
    When I click on "Disable adding to this organization"
    Then I should see a "Automatic bare-metal system discovery has been successfully disabled" text
    And the PXE default profile should be disabled

  Scenario: Cleanup: register a traditional client after bare metal tests
    When I bootstrap traditional client "sle_client" using bootstrap script with activation key "1-SUSE-KEY-x86_64" from the proxy
    Then I should see "sle_client" via spacecmd

  Scenario: Cleanup: remove remaining systems from SSM after bare metal tests
    When I follow "Clear"
