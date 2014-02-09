# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

# hooks in support/password_hooks.rb
Feature: Change the user's password
  In Order to change my password
  As an authorized user
  I want to input a new password

  @revertgoodpass
  Scenario: Change the password to a valid password
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

  @revertshortpass
  Scenario: Change the password to an invalid password
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
