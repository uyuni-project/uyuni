# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Very first settings
  In order to use the product
  As the admin user
  I want to create the organisation, the first users and set the HTTP proxy

  Scenario: Create admin user and first organization
    Given I access the host the first time
    And I run "rm -Rf /srv/salt/*" on "server"
    When I go to the home page
    And I enter "SUSE Test" as "orgName"
    And I enter "admin" as "login"
    And I enter "admin" as "desiredpassword"
    And I enter "admin" as "desiredpasswordConfirm"
    And I select "Mr." from "prefix"
    And I enter "Admin" as "firstNames"
    And I enter "Admin" as "lastName"
    And I enter "galaxy-noise@localhost" as "email"
    And I click on "Create Organization"
    Then I am logged in

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create testing username
    When I create a user with name "testing" and password "testing"
