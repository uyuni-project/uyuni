# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

# feature/init_user_create.feature
@javascript @init_once
Feature: Create initial users
  In Order to run the tests
  As a testing users
  I need to create the admin and a testing users

  @first
  Scenario: Create Admin users
    Given I access the host the first time
    When I go to the home page
    And I enter "admin" as "login"
    And I enter "admin" as "desiredpassword"
    And I enter "admin" as "desiredpasswordConfirm"
    And I select "Mr." from "prefix"
    And I enter "Admin" as "firstNames"
    And I enter "Admin" as "lastName"
    And I enter "galaxy-noise@suse.de" as "email"
    And I click on "Create Login"
    Then I am logged-in

  @second @monitoring
  Scenario: Enable Monitoring
    Given I am authorized as "admin" with password "admin"
    When I go to the admin configuration page
     And I check "Monitoring"
     And I click on "Update"
    Then I should see a "The SUSE Manager must be restarted to reflect these changes" text
     And I should see "is_monitoring_backend" as checked

    # Then I should see a "The Spacewalk must be restarted to reflect these changes" text

  @monitoring
  Scenario: restart spacewalk service
    Given I am root
    When I restart the spacewalk service
    Then I should be able to login

  @third
  Scenario: Create Testing username
    Given I am authorized as "admin" with password "admin"
    When I go to the users page
     And I follow "create new user"
     And I enter "testing" as "login"
     And I enter "testing" as "desiredpassword"
     And I enter "testing" as "desiredpasswordConfirm"
     And I enter "Test" as "firstNames"
     And I enter "User" as "lastName"
     And I enter "galaxy-noise@suse.de" as "email"
     And I click on "Create Login"
    Then I should see a "Account testing created, login information sent to galaxy-noise@suse.de" text
     And I should see a "testing" link

  @fourth
  Scenario: Grant Testing user admin priviledges
    Given I am authorized as "admin" with password "admin"
      When I go to the users page
       And I follow "testing"
       And I check "role_org_admin"
       And I check "role_system_group_admin"
       And I check "role_channel_admin"
       And I check "role_activation_key_admin"
       And I check "role_config_admin"
       And I check "role_monitoring_admin"
       And I click on "Submit"
    Then I should see a "User information updated" text
     And I should see a "testing" text

  @monitoring
  Scenario: Activate monitoring scout
    Given I am on the Admin page
     When I follow "SUSE Manager Configuration"
       And I follow "Monitoring" in class "content-nav"
       And I check "Enable Monitoring Scout"
       And I click on "Update Config"
    Then I should see a "Configuration updated, Monitoring services restarted." text

