# Copyright (c) 2015-16 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) check users page
#  2) create and delete users
#  3) Change permissions and roles in web UI
 
Feature: Manage users

  Scenario: Display active users page
    Given I am on the active Users page
    Then I should see a "Active Users" text
    And I should see a "Create User" link
    And I should see a "User List" link in the left menu
    And I should see a "Active" link in the left menu
    And I should see a "Deactivated" link in the left menu
    And I should see a "All" link in the left menu
    And I should see a "admin" link in the table first column
    And I should see a "Download CSV" link

  Scenario: Create a new user
    Given I am on the active Users page
    When I follow "Create User"
    And I enter "user1" as "login"
    And I enter "user1" as "desiredpassword"
    And I enter "user1" as "desiredpasswordConfirm"
    And I select "Mr." from "prefix"
    And I enter "Test" as "firstNames"
    And I enter "User" as "lastName"
    And I enter "galaxy-noise@suse.de" as "email"
    And I click on "Create Login"
    Then I should see a "Account user1 created, login information sent to galaxy-noise@suse.de" text
    And I should see a "user1" link
    And I should see a "normal user" text

  Scenario: Login as the new user
    Given I am authorized as "user1" with password "user1"
    Then I should see a "user1" link

  Scenario: Access user details
    Given I am on the active Users page
    And I follow "user1"
    Then I should see a "User Details" text
    And I should see a "Delete User" link
    And I should see a "Deactivate User" link
    And I should see a "Details" link
    And I should see a "System Groups" link
    And I should see a "Systems" link in the content area
    And I should see a "Channel Permissions" link
    And I should see a "Preferences" link in the content area
    And I should see a "Addresses" link
    And I should see a "user1" text
    And Option "Mr." is selected as "prefix"
    And I should see "Test" in field "firstNames"
    And I should see "User" in field "lastName"
    And I should see a "galaxy-noise@suse.de" link
    And I should see a "Administrative Roles" text
    And I should see a "Roles:" text
    And I should see a "Created:" text
    And I should see a "Last Sign In:" text

  Scenario: Add roles
    Given I am on the active Users page
    And I follow "user1"
    When the "role_satellite_admin" checkbox should be disabled
    And I check "role_org_admin"
    And I check "role_system_group_admin"
    And I check "role_channel_admin"
    And I check "role_activation_key_admin"
    And I check "role_config_admin"
    And I click on "Update"
    Then the "role_satellite_admin" checkbox should be disabled
    And I should see a "SUSE Manager Administrator" text
    And I should see "role_org_admin" as checked
    And I should see a "Organization Administrator" text
    And the "role_system_group_admin" checkbox should be disabled
    And I should see a "System Group Administrator - [ Admin Access ]" text
    And the "role_channel_admin" checkbox should be disabled
    And I should see a "Channel Administrator - [ Admin Access ]" text
    And the "role_activation_key_admin" checkbox should be disabled
    And I should see a "Activation Key Administrator - [ Admin Access ]" text
    And the "role_config_admin" checkbox should be disabled
    And I should see a "Configuration Administrator - [ Admin Access ]" text
    And I should see a "Above roles are granted via the Organization Administrator role." text

  Scenario: Verify user list
    Given I am on the active Users page
    Then Table row for "user1" should contain "Organization Administrator"
    And Table row for "user1" should contain "Channel Administrator"
    And Table row for "user1" should contain "Configuration Administrator"
    And Table row for "user1" should contain "System Group Administrator"
    And Table row for "user1" should contain "Activation Key Administrator"

  Scenario: Fail to deactivate organization administrator
    Given I am on the active Users page
    And I follow "user1"
    When I follow "Deactivate User"
    Then I should see a "This action will deactivate this user. This user will no longer be able to log in or perform actions unless it is reactivated." text
    When I click on "Deactivate User"
    Then I should see a "You cannot deactivate another organization administrator. Please remove the 'Organization Administrator' role from this user before attempting to deactivate their account." text
    When I follow "Deactivated"
    Then I should see a "No deactivated users." text

  Scenario: Remove role
    Given I am on the active Users page
    And I follow "user1"
    When I uncheck "role_org_admin"
    And I click on "Update"
    Then I should see "role_org_admin" as unchecked
    And I should see "role_system_group_admin" as checked
    And I should see "role_channel_admin" as checked
    And I should see "role_activation_key_admin" as checked
    And I should see "role_config_admin" as checked

  Scenario: Deactivate ordinary user
    Given I am on the active Users page
    And I follow "user1"
    Then I should see "role_org_admin" as unchecked
    When I follow "Deactivate User"
    Then I should see a "This action will deactivate this user. This user will no longer be able to log in or perform actions unless it is reactivated." text
    When I click on "Deactivate User"
    Then I should see a "Active Users" text
    And I should not see a "user1" link
    When I follow "Deactivated"
    Then I should see a "Deactivated Users" text
    And I should see a "user1" link
    When I follow "All"
    Then I should see a "user1" link

  Scenario: Reactivate user
    Given I am on the active Users page
    When I follow "Deactivated"
    And I follow "user1"
    Then I should see a "Reactivate User" link
    When I follow "Reactivate User"
    Then I should see a "This action will allow this user to access SUSE Manager. This user will retain all permissions, roles, and data that he or she had before being deactivated." text
    When I click on "Reactivate User"
    Then I should see a "Active Users" text
    And I should see a "user1" link
    When I follow "Deactivated"
    Then I should not see a "user1" link

  Scenario: Delete user
    Given I am on the active Users page
    And I follow "user1"
    When I follow "Delete User"
    Then I should see a "Confirm User Deletion" text
    And I should see a "This will delete this user permanently." text
    When I click on "Delete User"
    Then I should see a "Active Users" text
    And I should not see a "user1" link

  Scenario: Display the CSV separator preference
    Given I am authorized as "testing" with password "testing"
    And I follow "Your Preferences"
    Then I should see a "CSV Files" text
    And I should see a "Configure a separator character to be used in downloadable CSV files:" text
    And I should see a "Comma" text
    And I should see a "Semicolon" text

  Scenario: Configure the CSV separator char to semicolon
    Given I am authorized as "testing" with password "testing"
    And I follow "Your Preferences"
    And I choose ";"
    And I click on "Save Preferences"
    Then I should see a "Preferences modified" text
    And radio button "radio-semicolon" is checked

  Scenario: Cleanup: configure the CSV separator char to comma
    Given I am authorized as "testing" with password "testing"
    And I follow "Your Preferences"
    And I choose ","
    And I click on "Save Preferences"
    Then I should see a "Preferences modified" text
    And radio button "radio-comma" is checked
