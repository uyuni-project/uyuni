# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Build image with authenticated registry

  Scenario: Create an authenticated image store as Docker admin
    Given I am authorized as "docker" with password "docker"
    And I follow "Images" in the left menu
    And I follow "Stores" in the left menu
    And I follow "Create"
    And I enter "portus" as "label"
    And I check "useCredentials"
    And I enter uri, username and password for portus
    And I click on "create-btn"

  Scenario: Create a profile for the authenticated image store as Docker admin
    Given I am authorized as "docker" with password "docker"
    And I follow "Images" in the left menu
    And I follow "Profiles" in the left menu
    And I follow "Create"
    And I enter "portus_profile" as "label"
    And I select "portus" from "imageStore"
    And I select "1-DOCKER-TEST" from "activationKey"
    And I enter "Docker/authprofile" relative to profiles as "path"
    And I click on "create-btn"

  Scenario: Build an image in the authenticated image store
    Given I am authorized as "docker" with password "docker"
    And I navigate to images build webpage
    And I select "portus_profile" from "profileId"
    When I enter "latest" as "version"
    And I select sle-minion hostname in Build Host
    And I click on "submit-btn"
    Then I wait until I see "portus_profile" text

  Scenario: Verify the status of images in the authenticated image store
    Given I am authorized as "admin" with password "admin"
    Then container "portus_profile" built successfully

  Scenario: Cleanup: remove Docker profile for the authenticated image store
    Given I am authorized as "docker" with password "docker"
    When I follow "Images" in the left menu
    And I follow "Profiles" in the left menu
    And I check the row with the "portus_profile" text
    And I click on "Delete"
    And I click on the css "button.btn-danger"
    And I should see a "Image profile has been deleted." text

  Scenario: Cleanup: remove authenticated image store
    Given I am authorized as "docker" with password "docker"
    When I follow "Images" in the left menu
    And I follow "Stores" in the left menu
    And I check the row with the "portus" text
    And I click on "Delete"
    And I click on the css "button.btn-danger"
    And I should see a "Image store has been deleted." text

  Scenario: Cleanup: delete portus image
    Given I am authorized as "admin" with password "admin"
    When I delete the image "portus_profile" with version "latest" via XML-RPC calls
