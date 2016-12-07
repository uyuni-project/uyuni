# Copyright (c) 2015-16 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Change the user's password
  In Order to change my password
  As an authorized user
  I want enter a new password and test some wrong cases

  Scenario: Change the password to a New password
    Given I am authorized as "admin" with password "admin"
    And I follow "Your Account"
    And I enter "GoodPass" as "desiredpassword"
    And I enter "GoodPass" as "desiredpasswordConfirm"
    And I click on "Update"
    Then I should see a "User information updated" text
    Given I sign out
    And I enter "admin" as "username"
    And I enter "GoodPass" as "password"
    And I click on "Sign In"
    Then I should be logged in

  Scenario: Revert the new password to a valid standard password
    Given I am authorized as "admin" with password "GoodPass"
    And I follow "Your Account"
    And I enter "admin" as "desiredpassword"
    And I enter "admin" as "desiredpasswordConfirm"
    And I click on "Update"
    Then I should see a "User information updated" text
    Given I sign out
    And I enter "admin" as "username"
    And I enter "admin" as "password"
    And I click on "Sign In"
    Then I should be logged in

  Scenario: Test invalid password
    Given I am authorized as "admin" with password "admin"
    And I follow "Your Account"
    And I enter "A" as "desiredpassword"
    And I enter "A" as "desiredpasswordConfirm"
    And I click on "Update"
    Then I should see a "Passwords must be at least 5 characters." text
    Given I sign out
    And I enter "admin" as "username"
    And I enter "A" as "password"
    And I click on "Sign In"
    Then I should not be authorized
