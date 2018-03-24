# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test image building with authenticated registry

  Scenario: Create an authenticated Image Store as docker admin
    Given I am authorized as "docker" with password "docker"
    And I follow "Images" in the left menu
    And I follow "Stores" in the left menu
    And I follow "Create"
    And I enter "portus" as "label"
    And I enter "portus.mgr.suse.de:5000/cucutest" as "uri"
    And I check "useCredentials"
    And I enter "cucutest" as "username"
    And I enter "cucusecret" as "password"
    And I click on "create-btn"

  Scenario: Create a profile as docker admin
    Given I am authorized as "docker" with password "docker"
    And I follow "Images" in the left menu
    And I follow "Profiles" in the left menu
    And I follow "Create"
    And I enter "portus_profile" as "label"
    And I select "portus" from "imageStore"
    And I select "1-DOCKER-TEST" from "activationKey"
    And I enter "https://gitlab.suse.de/galaxy/suse-manager-containers.git#:test-profile/authprofile" as "path"
    And I click on "create-btn"

  Scenario: Build some images via GUI
    Given I am authorized as "docker" with password "docker"
    And I navigate to images build webpage
    When I enter "latest" as "version"
    And I select "portus_profile" from "profileId"
    And I select sle-minion hostname in Build Host
    And I click on "submit-btn"
    And I wait for "5" seconds
    Then I should see a "portus_profile" text

  Scenario: Verify the status of images
    Given I am authorized as "admin" with password "admin"
    Then container "portus_profile" built successfully

  Scenario: Cleanup: remove docker profile
    Given I am authorized as "docker" with password "docker"
    When I follow "Images" in the left menu
    And I follow "Profiles" in the left menu
    And I check the row with the "portus_profile" text
    And I click on "Delete"
    And I click on the css "button.btn-danger"
    And I should see a "Image profile has been deleted." text

  Scenario: Cleanup: remove Image Store
    Given I am authorized as "docker" with password "docker"
    When I follow "Images" in the left menu
    And I follow "Stores" in the left menu
    And I check the row with the "portus" text
    And I click on "Delete"
    And I click on the css "button.btn-danger"
    And I should see a "Image store has been deleted." text
