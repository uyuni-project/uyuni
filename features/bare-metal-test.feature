# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test Bare-metal discovery

  Scenario: Enable Bare-metal discovery
    Given I am authorized as "admin" with password "admin"
    And I follow "Admin"
    And I follow "SUSE Manager Configuration" in the left menu
    When I follow "Bare-metal systems" in the content area
    Then I should see a "Allows SUSE Manager to automatically add bare-metal systems capable of PXE booting to an organization." text
    And I should see a "Enable adding to this organization" button
    When I click on "Enable adding to this organization"
    Then I should see a "Automatic bare-metal system discovery has been successfully enabled" text
    And the pxe-default-profile should be enabled

  Scenario: Register a client
    Given I am root
    When I register using "1-spacewalk-bootstrap-activation-key" key
    Then I should see this client in spacewalk

  Scenario: check registration values
    Given I am on the Systems overview page of this client
    Then I should see a "System Info" text
    And I should see a "Edit These Properties" link
    And I should not see a "[Management]" text

  Scenario: see the client in Bare metal specific system list
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow "Bare Metal Systems" in the left menu
    Then I should see a "Bare Metal Systems" text
    And I should see a "Detected on" text
    And I should see a "Number of CPUs" text
    And I should see a "2" text
    And I should see a "Clock frquency" text
    And I should see a "2 GHz" text
    And I should see a "RAM" text
    And I check the ram value
    And I should see a "Number of disks" text
    And I should see a "1" text
    And I should see a "MAC Address(es)" text
    And I check the MAC address value

  Scenario: check tab links "Details"
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    When I follow this client link
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

  Scenario: check tab links "Provisioning"
    Given I am on the Systems overview page of this client
    When I follow "Provisioning" in the content area
    Then I should see a "Kickstart" link in the content area
    And I should not see a "Snapshots" link in the content area
    And I should not see a "Snapshot Tags" link in the content area
    And I should see a "Power Management" link in the content area
    And I should see a "Schedule" link in the content area

  Scenario: check SSM with bare-metal system
    Given I am on the Systems page
    When I check this client
    And I wait for "30" seconds
    And I follow "System Set Manager" in the left menu
    Then I should see a "At least one system in the set is bare metal: some actions will not be available. Check bare metal systems." text

  Scenario: check SSM page for bare-metal system
    Given I am on the Systems page
    When I follow "System Set Manager" in the left menu
    Then I should see a "List the systems" link in the content area
    And I should see a "Autoinstall" link in the content area
    And I should see a "Configure power management" link in the content area
    And I should see a "power management operations" link in the content area
    And I should see a "Delete" link in the content area
    And I should see a "Migrate" link in the content area
    And I should not see a "Errata" link in the content area
    And I should not see a "Packages" link in the content area
    And I should not see a "Groups" link in the content area
    And I should not see a "Channels" link in the content area
    And I should not see a "Audit" link in the content area
    And I follow "Clear"

  Scenario: Delete the system profile
    Given I am on the Systems overview page of this client
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"

  Scenario: Disable Bare-metal discovery
    Given I am authorized as "admin" with password "admin"
    And I follow "Admin"
    And I follow "SUSE Manager Configuration" in the left menu
    When I follow "Bare-metal systems" in the content area
    Then I should see a "Allows SUSE Manager to automatically add bare-metal systems capable of PXE booting to an organization." text
    And I should see a "Disable adding to this organization" button
    When I click on "Disable adding to this organization"
    Then I should see a "Automatic bare-metal system discovery has been successfully disabled" text
    And the pxe-default-profile should be disabled

