# Copyright (c) 2010-2013 Novell, Inc.
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
         Then I should see a "System Status" text
          And I should see a "Edit These Properties" link
          And I should not see a "[Monitoring]" text
          And I should not see a "[Provisioning]" text
          And I should not see a "[Management]" text

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
          And I should see a "Overview" link in element "contentnav-row2"
          And I should see a "Properties" link in element "contentnav-row2"
          And I should not see a "Remote Command" link in element "contentnav-row2"
          And I should not see a "Reactivation" link in element "contentnav-row2"
          And I should see a "Hardware" link in element "contentnav-row2"
          And I should see a "Migrate" link in element "contentnav-row2"
          And I should see a "Notes" link in element "contentnav-row2"
          And I should not see a "Custom Info" link in element "contentnav-row2"

    Scenario: check tab links "Provisioning"
        Given I am on the Systems overview page of this client
         When I follow "Provisioning" in the content area
         Then I should see a "Kickstart" link in element "contentnav-row2"
          And I should not see a "Snapshots" link in element "contentnav-row2"
          And I should not see a "Snapshot Tags" link in element "contentnav-row2"
          And I should see a "Power Management" link in element "contentnav-row2"
          And I should see a "Schedule" link in the content area

    Scenario: Delete the system profile
        Given I am on the Systems overview page of this client
         When I follow "delete system"
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

