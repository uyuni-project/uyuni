# Copyright (c) 2017 SUSE LLC
# SPDX-License-Identifier: MIT

@scope_building_container_images
@no_auth_registry
Feature: Advanced content management

  Scenario: Log in as docker user
    Given I am authorized as "docker" with password "docker"

  Scenario: Create an image store as Docker admin
    When I follow the left menu "Images > Stores"
    And I follow "Create"
    And I enter "docker_admin" as "label"
    And I enter the URI of the registry as "uri"
    And I click on "create-btn"
    Then I wait until table row contains a "galaxy-registry" text

  @scc_credentials
  Scenario: Create a profile as Docker admin
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_docker_admin" as "label"
    And I select "galaxy-registry" from "imageStore"
    And I select "1-SUSE-KEY-x86_64" from "activationKey"
    And I enter "Docker/serverhost" relative to profiles as "path"
    And I click on "create-btn"

  Scenario: Create a user without rights nor roles
    Given I am authorized for the "Admin" section
    When I create a user with name "norole" and password "norole" with roles ""

  Scenario: Log in as docker user
    Given I am authorized as "docker" with password "docker"

  @scc_credentials
  Scenario: Cleanup: remove Docker profile
    Given I am authorized as "docker" with password "docker"
    When I follow the left menu "Images > Profiles"
    And I check the row with the "suse_docker_admin" text
    And I click on "Delete"
    And I click on the red confirmation button
    And I should see a "Image profile has been deleted." text

  Scenario: Cleanup: remove image store
    When I follow the left menu "Images > Stores"
    And I check the row with the "docker_admin" text
    And I click on "Delete"
    And I click on the red confirmation button
    And I should see a "Image store has been deleted." text

  Scenario: Cleanup: delete no role user
    Given I am authorized for the "Admin" section
    When I delete user "norole"
