# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

#@sle_minion
Feature: Correct timezone display 
#  1) create a user and assign him a timezone different than the server's timezone
#  2) test that the popups in some scheduling actions appear in users prefered timezone 
#  3) some scheduler tests based on previous bugs are unavoidable

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create a new user in a timezone different than server's timezone
    When I follow the left menu "Users > User List > Active"
    And I follow "Create User"
    And I enter "user1" as "login"
    And I enter "user1" as "desiredpassword"
    And I enter "user1" as "desiredpasswordConfirm"
    And I select "Mr." from "prefix"
    And I enter "Test" as "firstNames"
    And I enter "User" as "lastName"
    And I enter "galaxy-noise@suse.de" as "email"
    And I select "(GMT+0800) Malaysia" from "timezone"
    And I click on "Create Login"

  Scenario: Add roles
    When I follow the left menu "Users > User List > Active"
    And I follow "user1"
    And the "role_satellite_admin" checkbox should be disabled
    And I check "role_org_admin"
    And I check "role_system_group_admin"
    And I check "role_channel_admin"
    And I check "role_activation_key_admin"
    And I check "role_config_admin"
    And I click on "Update"

  Scenario: Login as the new user
    Given I am authorized as "user1" with password "user1"
    Then I should see a "user1" link

# bsc 1195455, a P3 bug not fixed yet, 
  Scenario: Schedule a remote script in the future and see the correct timezone as a pop up
    Given I am on the Systems overview page of this "onalmpantis-min-sles15sp3-1.tf.local"
    When I follow "Remote Command" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      ls
      """
    And I enter "00:00" as "date_timepicker_widget_input"
    And I click on "Schedule"
    Then I should see a "12:00:00 AM MYT" text
    #WORKAROUND If the above line gets red, they probably fixed the bug, then remove AM from the text
    
  Scenario: Login as the new user if the previous scenario failed
    Given I am authorized as "user1" with password "user1"
    Then I should see a "user1" link

#WORKAROUND
# bsc 1195190, a P3 bug not fixed yet, this scenario is disabled as it is a minor bug, if they fix it we will re-enable
#  Scenario: Cancel the event and see the correct timezone 
#    Given I am on the Systems overview page of this "onalmpantis-min-sles15sp3-1.tf.local"
#    When I follow "Events" in the content area
#    Then I should see a "00:00:00 MYT" text
#    And I check "list_1697531469_sel"
#    And I click on "Cancel Selected Events"
#    Then I should see a "MYT" text  

#  Scenario: Login as the new user if the previous scenario failed
#    Given I am authorized as "user1" with password "user1"
#    Then I should see a "user1" link

# bsc 1195191, a P3 bug not fixed yet 
  Scenario: Schuedule a remote script to run now  and see the correct timezone details in history
    Given I am on the Systems overview page of this "onalmpantis-min-sles15sp3-1.tf.local"
    When I follow "Remote Command" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      ls
      """
    And I click on "Schedule"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    When I follow first "Remote Command on onalmpantis-min-sles15sp3-1.tf.local. scheduled by user1"
    Then I should see a "MYT" text
    #And I should not see a "PM" text
    #WORKAROUND remove the comment from the above line if its fixed
    
  Scenario: Login as the new user if the previous scenario failed
    Given I am authorized as "user1" with password "user1"
    Then I should see a "user1" link

#WORKAROUND
# bsc 1195189, a P3 bug not fixed yet
#  Scenario: Schedule a minion reboot and check the pop up timezone 
#    Given I am on the Systems overview page of this "onalmpantis-min-sles15sp3-1.tf.local"
#    When I follow first "Schedule System Reboot"
#    And I enter "00:00" as "date_timepicker_widget_input"
#    When I click on "Reboot system"
#    Then I should see a "MYT" text
#    And I should not see a "CET" text

#  Scenario: Login as the new user if the previous scenario failed
#    Given I am authorized as "user1" with password "user1"
#    Then I should see a "user1" link

#bsc 1195452, a scheduler P3 bug not fixed yet
  Scenario: Clean up - Cancel minion reboot 
    Given I am on the Systems overview page of this "onalmpantis-min-sles15sp3-1.tf.local"
    When I follow "Events" in the content area
    And I check "list_1697531469_sel"
    And I click on "Cancel Selected Events"
    And I click on "Cancel Selected Events"

  Scenario: Clean up - Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Clean up - Remove role
    When I follow the left menu "Users > User List > Active"
    And I follow "user1"
    And I uncheck "role_org_admin"
    And I click on "Update"
    Then I should see "role_org_admin" as unchecked
    And I should see "role_system_group_admin" as checked
    And I should see "role_channel_admin" as checked
    And I should see "role_activation_key_admin" as checked
    And I should see "role_config_admin" as checked

  Scenario: Clean up - Delete user
    When I follow the left menu "Users > User List > Active"
    And I follow "user1"
    When I follow "Delete User"
    Then I should see a "Confirm User Deletion" text
    And I should see a "This will delete this user permanently." text
    When I click on "Delete User"
    Then I should see a "Active Users" text
    And I should not see a "user1" link
