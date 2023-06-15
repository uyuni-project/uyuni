# Copyright (c) 2017-2023 SUSE LLC
# Licensed under the terms of the MIT license.

# This feature is a dependency for:
# - features/secondary/min_docker_api.feature

Feature: Prepare server for using Docker

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create a Docker user with image administrators rights
    When I follow the left menu "Users > User List > Active"
    And I follow "Create User"
    And I enter "docker" as "login"
    And I enter "docker" as "desiredpassword"
    And I enter "docker" as "desiredpasswordConfirm"
    And I select "Mr." from "prefix"
    And I enter "TestDocky" as "firstNames"
    And I enter "TestDocky" as "lastName"
    And I enter "gino-ginae@susy.de" as "email"
    And I click on "Create Login"
    Then I should see a "Account docker created" text
    And I should see a "docker" link
    And I should see a "normal user" text
    And I follow "docker"
    And I check "role_image_admin"
    And I click on "Update"

@no_auth_registry
  Scenario: Create an image store without credentials
    When I follow the left menu "Images > Stores"
    And I follow "Create"
    And I enter "galaxy-registry" as "label"
    And I enter the URI of the registry as "uri"
    And I click on "create-btn"
    Then I wait until table row contains a "galaxy-registry" text
    And I should see a "Items 1 - 1 of 1" text
