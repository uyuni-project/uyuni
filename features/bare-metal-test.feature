# Copyright (c) 2010-2013 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Test Bare-metal discovery
    Background:
        Given I am authorized as "admin" with password "admin"

    Scenario: Enable Bare-metal discovery
        Given I follow "Admin"
          And I follow "SUSE Manager Configuration" in the left menu
         When I follow "Bare-metal systems" in class "content-nav"
         Then I should see a "Allows SUSE Manager to automatically add bare-metal systems capable of PXE booting to an organization." text
          And I should see a "Enable adding to this organization" button
         When I click on "Enable adding to this organization"
         Then I should see a "Automatic bare-metal system discovery has been successfully enabled" text
          And the pxe-default-profile should be enabled

    Scenario: Disable Bare-metal discovery
        Given I follow "Admin"
          And I follow "SUSE Manager Configuration" in the left menu
         When I follow "Bare-metal systems" in class "content-nav"
         Then I should see a "Allows SUSE Manager to automatically add bare-metal systems capable of PXE booting to an organization." text
          And I should see a "Disable adding to this organization" button
         When I click on "Disable adding to this organization"
         Then I should see a "Automatic bare-metal system discovery has been successfully disabled" text
          And the pxe-default-profile should be disabled

