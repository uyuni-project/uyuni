# Copyright (c) 2018-2020 SUSE LLC
# Licensed under the terms of the MIT license.

@buildhost
Feature: Build image with authenticated registry

@auth_registry
  Scenario: Create an authenticated image store as Docker admin
    Given I am authorized as "docker" with password "docker"
    When I follow the left menu "Images > Stores"
    And I follow "Create"
    And I enter "portus" as "label"
    And I check "useCredentials"
    And I enter URI, username and password for portus
    And I click on "create-btn"

@auth_registry
  Scenario: Create a profile for the authenticated image store as Docker admin
    Given I am authorized as "docker" with password "docker"
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "portus_profile" as "label"
    And I select "portus" from "imageStore"
    And I select "1-DOCKER-TEST" from "activationKey"
    And I enter "Docker/authprofile" relative to profiles as "path"
    And I click on "create-btn"

@auth_registry
  Scenario: Build an image in the authenticated image store
    Given I am authorized as "docker" with password "docker"
    When I follow the left menu "Images > Build"
    And I select "portus_profile" from "profileId"
    And I enter "latest" as "version"
    And I select the hostname of "build_host" from "buildHostId"
    And I click on "submit-btn"
    Then I wait until I see "portus_profile" text
    # Verify the status of images in the authenticated image store
    When I wait at most 500 seconds until container "portus_profile" is built successfully

@auth_registry
  Scenario: Cleanup: remove Docker profile for the authenticated image store
    Given I am authorized as "docker" with password "docker"
    When I follow the left menu "Images > Profiles"
    And I check the row with the "portus_profile" text
    And I click on "Delete"
    And I click on the red confirmation button
    And I should see a "Image profile has been deleted." text

@auth_registry
  Scenario: Cleanup: remove authenticated image store
    Given I am authorized as "docker" with password "docker"
    When I follow the left menu "Images > Stores"
    And I check the row with the "portus" text
    And I click on "Delete"
    And I click on the red confirmation button
    And I should see a "Image store has been deleted." text

@auth_registry
  Scenario: Cleanup: delete portus image
    When I delete the image "portus_profile" with version "latest" via XML-RPC calls

@auth_registry
  Scenario: Cleanup: kill stale portus image build jobs
    When I kill remaining Salt jobs on "build_host"
