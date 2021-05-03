# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_power_management
Feature: Redfish Power management

  Scenario: Setup a Redfish host
    When the server starts mocking a Redfish host

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Save power management values for Redfish
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Provisioning" in the content area
    And I follow "Power Management" in the content area
    And I enter the server hostname as the redfish server address
    And I enter "ipmiusr" as "powerUsername"
    And I enter "test" as "powerPassword"
    And I select "Redfish" from "powerType"
    And I click on "Save"
    Then I should see a "Power settings saved" text
    And the cobbler report should contain ":8443" for "sle_minion"
    And the cobbler report should contain "Power Management Username      : ipmiusr" for "sle_minion"
    And the cobbler report should contain "Power Management Password      : test" for "sle_minion"
    And the cobbler report should contain "Power Management Type          : redfish" for "sle_minion"

  Scenario: Test Redfish functions
    When I follow "Provisioning" in the content area
    And I follow "Power Management" in the content area
    And I click on "Power On"
    And I click on "Get status"
    Then I should see the power is "On"
    When I click on "Power Off"
    Then I should see a "system has been powered off" text
    And I should see the power is "Unknown"
    When I click on "Get status"
    Then I should see the power is "Off"
    When I click on "Power On"
    Then I should see a "system has been powered on" text
    When I click on "Get status"
    Then I should see the power is "On"
    When I click on "Reboot"
    Then I should see a "system has been rebooted" text
    When I click on "Get status"
    Then I should see the power is "On"

  Scenario: Check power management SSM configuration for Redfish
    And I am on the System Overview page
    When I follow "Clear"
    And I check the "sle_minion" client
    And I am on System Set Manager Overview
    And I follow "Configure power management" in the content area
    Then I should see "sle_minion" as link
    And I should see a "Change Power Management Configuration" text
    And I should see a "Type" text
    And I should see a "Network address" text
    And I should see a "Username" text
    And I should see a "Password" text
    And I should see a "System identifier" text
    And I should see a "Update" button
    When I enter "testing" as "powerUsername"
    And I enter "qwertz" as "powerPassword"
    And I click on "Update"
    Then I should see a "Configuration successfully saved for 1 system(s)" text
    And the cobbler report should contain "Power Management Username      : testing" for "sle_minion"
    And the cobbler report should contain "Power Management Password      : qwertz" for "sle_minion"
    And the cobbler report should contain ":8443" for "sle_minion"
    And the cobbler report should contain "Power Management Type          : redfish" for "sle_minion"

  Scenario: Check power management SSM operation for Redfish
    And I am on System Set Manager Overview
    When I follow "power management operations" in the content area
    Then I should see "sle_minion" as link
    And I should see a "Power On" button
    And I should see a "Power Off" button
    And I should see a "Reboot" button

  Scenario: Cleanup: reset Redfish values
    Given I am logged in via XML-RPC powermgmt as user "admin" and password "admin"
    And I want to operate on this "sle_minion"
    When I set power management value "" for "powerAddress"
    And I set power management value "" for "powerUsername"
    And I set power management value "" for "powerPassword"
    And I set power management value "ipmitool" for "powerType"
    Then the cobbler report should contain "Power Management Address       :" for "sle_minion"
    And the cobbler report should contain "Power Management Username      :" for "sle_minion"
    And the cobbler report should contain "Power Management Password      :" for "sle_minion"
    And the cobbler report should contain "Power Management Type          : ipmitool" for "sle_minion"

  Scenario: Cleanup: tear down the Redfish host
    When the server stops mocking a Redfish host

  Scenario: Cleanup: remove remaining systems from SSM after Redfish power management tests
    When I follow "Clear"
