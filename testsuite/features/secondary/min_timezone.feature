# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.
@scope_visualization
@sle_minion
Feature: Correct timezone display 
#  1) create a user and assign him a timezone different than the server's timezone
#  2) test that the popups in some scheduling actions appear in user's prefered timezone 
#  3) some scheduler tests based on previous bugs are unavoidable

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create a new user in a timezone different than server's timezone
    When I follow the left menu "Users > User List > Active"
    And I follow "Create User"
    And I enter "MalaysianUser" as "login"
    And I enter "MalaysianUser" as "desiredpassword"
    And I enter "MalaysianUser" as "desiredpasswordConfirm"
    And I select "Mr." from "prefix"
    And I enter "Test" as "firstNames"
    And I enter "User" as "lastName"
    And I enter "galaxy-noise@suse.de" as "email"
    And I select "(GMT+0800) Malaysia" from "timezone"
    And I click on "Create Login"

  Scenario: Add roles for the Malaysian user
    When I follow the left menu "Users > User List > Active"
    And I follow "MalaysianUser"
    And the "role_satellite_admin" checkbox should be disabled
    And I check "role_org_admin"
    And I check "role_system_group_admin"
    And I check "role_channel_admin"
    And I check "role_activation_key_admin"
    And I check "role_config_admin"
    And I click on "Update"

  Scenario: Login as the new Malaysian user
    Given I am authorized as "MalaysianUser" with password "MalaysianUser"
    Then I should see a "MalaysianUser" link

  Scenario: Schedule a remote script in the future and see the correct timezone as a pop up
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Remote Command" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      ls
      """
    And I enter "00:00" as "date_timepicker_widget_input"
    And I click on "Schedule"
    # WORKAROUND for bsc #1195455, If the below line gets red, they probably fixed the bug, then we should remove AM from the below text
    Then I should see a "12:00:00 AM MYT" text
    
  Scenario: Login as the new Malaysian user if the previous scenario failed
    Given I am authorized as "MalaysianUser" with password "MalaysianUser"
    Then I should see a "MalaysianUser" link

  Scenario: Schedule a remote script to run now  and see the correct timezone details in history
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Remote Command" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      ls
      """
    And I click on "Schedule"
    And I follow "Events" in the content area
    And I follow "History" in the content area
    And I follow first "scheduled by MalaysianUser"
    Then I should see a "MYT" text
    # WORKAROUND for bsc #1195191, the below line is commented out but if the bug is fixed we should enable it. 
    # And I should not see a "PM" text
    
  Scenario: Login as the new Malaysian user if the previous scenario failed
    Given I am authorized as "MalaysianUser" with password "MalaysianUser"
    Then I should see a "MalaysianUser" link

  Scenario: Cleanup: Log in as admin user again
    Given I am authorized for the "Admin" section

  Scenario: Cleanup: Remove role
    When I follow the left menu "Users > User List > Active"
    And I follow "MalaysianUser"
    And I uncheck "role_org_admin"
    And I click on "Update"
    Then I should see "role_org_admin" as unchecked
    And I should see "role_system_group_admin" as checked
    And I should see "role_channel_admin" as checked
    And I should see "role_activation_key_admin" as checked
    And I should see "role_config_admin" as checked

  Scenario: Cleanup: Delete user
    When I follow the left menu "Users > User List > Active"
    And I follow "MalaysianUser"
    And I follow "Delete User"
    Then I should see a "Confirm User Deletion" text
    And I should see a "This will delete this user permanently." text
    When I click on "Delete User"
    Then I should see a "Active Users" text
    And I should not see a "MalaysianUser" link
