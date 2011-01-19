# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Log in and out of the host
  In Order to log into the host
  As a non-authorized user
  I want to log in
  Then I should be an authorized user
  Scenario: Log into the host
    Given I am not authorized
    When I go to the home page
    And I enter "testing" as "username"
    And I enter "testing" as "password"
    And I click on "Sign In"
    Then I should be logged in
  Scenario: Log out of the host
    Given I am authorized
    When I sign out
    Then I should not be authorized
