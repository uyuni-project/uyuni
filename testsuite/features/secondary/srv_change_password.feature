# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_visualization
Feature: Change the user's password
  In order to change my password
  As an authorized user
  I want enter a new password

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Change the password to a new password
    When I follow the left menu "Home > User Account > My Account"
    And I enter "GoodPass" as "desiredpassword"
    And I enter "GoodPass" as "desiredpasswordConfirm"
    And I click on "Update"
    Then I should see a "User information updated" text
    When I sign out
    And I enter "admin" as "username"
    And I enter "GoodPass" as "password"
    And I click on "Sign In"
    Then I should be logged in

  Scenario: Revert the new password to a valid standard password
    When I follow the left menu "Home > User Account > My Account"
    And I enter "admin" as "desiredpassword"
    And I enter "admin" as "desiredpasswordConfirm"
    And I click on "Update"
    Then I should see a "User information updated" text
    When I sign out
    And I enter "admin" as "username"
    And I enter "admin" as "password"
    And I click on "Sign In"
    Then I should be logged in

  Scenario: Try an invalid password
    When I follow the left menu "Home > User Account > My Account"
    And I enter "A" as "desiredpassword"
    And I enter "A" as "desiredpasswordConfirm"
    And I click on "Update"
    Then I should see a "Passwords must be at least 5 characters." text
    When I sign out
    And I enter "admin" as "username"
    And I enter "A" as "password"
    And I click on "Sign In"
    Then I should not be authorized
