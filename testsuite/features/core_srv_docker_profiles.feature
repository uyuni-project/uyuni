# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Prepare server for using Docker

  Scenario: Create a Docker user with image administrators rights
    Given I am on the active Users page
    When I follow "Create User"
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

  Scenario: Create Docker activation key
    Given I am on the Systems page
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    When I enter "Docker testing" as "description"
    And I enter "DOCKER-TEST" as "key"
    And I enter "20" as "usageLimit"
    And I select "Test-Channel-x86_64" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    And I follow "Packages"
    And I enter "orion-dummy perseus-dummy" as "packages"
    And I click on "Update Activation Key"
    Then I should see a "Activation key Docker testing has been modified" text

  Scenario: Create an image store without credentials
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Images > Stores"
    And I follow "Create"
    And I enter "galaxy-registry" as "label"
    And I enter "registry.mgr.suse.de" as "uri"
    And I click on "create-btn"
    Then I wait until table row contains a "galaxy-registry" text
    And I should see a "Items 1 - 1 of 1" text

  Scenario: Create a simple image profile without activation key
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_simple" as "label"
    And I select "galaxy-registry" from "imageStore"
    And I enter "Docker" relative to profiles as "path"
    And I click on "create-btn"

  Scenario: Create a simple real image profile without activation key
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_real_simple" as "label"
    And I select "galaxy-registry" from "imageStore"
    And I enter "Docker/serverhost" relative to profiles as "path"
    And I click on "create-btn"

  Scenario: Create an image profile with activation key
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_key" as "label"
    And I select "galaxy-registry" from "imageStore"
    And I select "1-DOCKER-TEST" from "activationKey"
    And I enter "Docker" relative to profiles as "path"
    And I click on "create-btn"

  Scenario: Create a simple real image profile with activation key
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_real_key" as "label"
    And I select "galaxy-registry" from "imageStore"
    And I select "1-DOCKER-TEST" from "activationKey"
    And I enter "Docker/serverhost" relative to profiles as "path"
    And I click on "create-btn"
