# Copyright (c) 2018-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@buildhost
@scope_building_container_images
@auth_registry
Feature: Build image with authenticated registry

  Scenario: Create an authenticated image store as Docker admin
    Given I am authorized as "docker" with password "docker"
    When I follow the left menu "Images > Stores"
    And I follow "Create"
    And I enter "portus" as "label"
    And I check "useCredentials"
    And I enter URI, username and password for portus
    And I click on "create-btn"
    Then I wait until I see "portus" text

  Scenario: Create a profile for the authenticated image store as Docker admin
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "portus_profile" as "label"
    And I select "portus" from "imageStore"
    And I select "1-DOCKER-TEST" from "activationKey"
    And I enter "Docker/authprofile" relative to profiles as "path"
    And I click on "create-btn"
    Then I wait until I see "portus_profile" text

  Scenario: Build an image in the authenticated image store
    When I follow the left menu "Images > Build"
    And I select "portus_profile" from "profileId"
    And I enter "latest" as "version"
    And I select the hostname of "build_host" from "buildHostId"
    And I click on "submit-btn"
    Then I wait until I see "portus_profile" text
    # Verify the status of images in the authenticated image store
    When I wait at most 600 seconds until container "portus_profile" is built successfully
    And I refresh the page
    Then table row for "portus_profile" should contain "1"

  Scenario: Cleanup: remove Docker profile for the authenticated image store
    When I follow the left menu "Images > Profiles"
    And I check the row with the "portus_profile" text
    And I click on "Delete"
    And I click on the red confirmation button
    And I should see a "Image profile has been deleted." text

  Scenario: Cleanup: remove authenticated image store
    When I follow the left menu "Images > Stores"
    And I check the row with the "portus" text
    And I click on "Delete"
    And I click on the red confirmation button
    And I should see a "Image store has been deleted." text

  Scenario: Cleanup: delete portus image
    Given I am authorized as "admin" with password "admin"
    When I delete the image "portus_profile" with version "latest" via XML-RPC calls
