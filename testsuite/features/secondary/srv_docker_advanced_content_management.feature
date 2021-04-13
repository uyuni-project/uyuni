# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_building_container_images
Feature: Advanced content management

@no_auth_registry
  Scenario: Create an image store as Docker admin
    Given I am authorized as "docker" with password "docker"
    When I follow the left menu "Images > Stores"
    And I follow "Create"
    And I enter "docker_admin" as "label"
    And I enter the URI of the registry as "uri"
    And I click on "create-btn"

@no_auth_registry
  Scenario: Create a profile as Docker admin
    Given I am authorized as "docker" with password "docker"
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_docker_admin" as "label"
    And I select "galaxy-registry" from "imageStore"
    And I select "1-DOCKER-TEST" from "activationKey"
    And I enter "Docker/serverhost" relative to profiles as "path"
    And I click on "create-btn"

@no_auth_registry
  Scenario: Create a user without rights nor roles
    When I follow the left menu "Users > User List > Active"
    And I follow "Create User"
    And I enter "norole" as "login"
    And I enter "norole" as "desiredpassword"
    And I enter "norole" as "desiredpasswordConfirm"
    And I select "Mr." from "prefix"
    And I enter "norole" as "firstNames"
    And I enter "norole" as "lastName"
    And I enter "norole-ginae@susy.de" as "email"
    And I click on "Create Login"
    Then I should see a "Account norole created" text
    And I should see a "norole" link
    And I should see a "normal user" text

@no_auth_registry
  Scenario: Cleanup: remove Docker profile
    Given I am authorized as "docker" with password "docker"
    When I follow the left menu "Images > Profiles"
    And I check the row with the "suse_docker_admin" text
    And I click on "Delete"
    And I click on the red confirmation button
    And I should see a "Image profile has been deleted." text

@no_auth_registry
  Scenario: Cleanup: remove image store
    Given I am authorized as "docker" with password "docker"
    When I follow the left menu "Images > Stores"
    And I check the row with the "docker_admin" text
    And I click on "Delete"
    And I click on the red confirmation button
    And I should see a "Image store has been deleted." text

@no_auth_registry
  Scenario: Cleanup: delete no role user
    When I follow the left menu "Users > User List > Active"
    And I follow "norole"
    And I follow "Delete User"
    Then I should see a "Confirm User Deletion" text
    And I should see a "This will delete this user permanently." text
    When I click on "Delete User"
    Then I should see a "Active Users" text
    And I should not see a "norole" link
