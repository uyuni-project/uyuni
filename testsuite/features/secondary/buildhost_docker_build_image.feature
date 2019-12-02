# Copyright (c) 2017-2020 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Basic images do not contain zypper nor the name of the server,
# so the inspect functionality is not tested here.

@buildhost
Feature: Build container images

@no_auth_registry
  Scenario: Create a simple image profile without activation key
    Given I am authorized with the feature's user
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_simple" as "label"
    And I select "galaxy-registry" from "imageStore"
    And I enter "Docker" relative to profiles as "path"
    And I click on "create-btn"

@no_auth_registry
  Scenario: Create a simple real image profile without activation key
    Given I am authorized with the feature's user
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_real_simple" as "label"
    And I select "galaxy-registry" from "imageStore"
    And I enter "Docker/serverhost" relative to profiles as "path"
    And I click on "create-btn"

@no_auth_registry
  Scenario: Create an image profile with activation key
    Given I am authorized with the feature's user
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_key" as "label"
    And I select "galaxy-registry" from "imageStore"
    And I select "1-DOCKER-TEST" from "activationKey"
    And I enter "Docker" relative to profiles as "path"
    And I click on "create-btn"

@no_auth_registry
  Scenario: Create a simple real image profile with activation key
    Given I am authorized with the feature's user
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_real_key" as "label"
    And I select "galaxy-registry" from "imageStore"
    And I select "1-DOCKER-TEST" from "activationKey"
    And I enter "Docker/serverhost" relative to profiles as "path"
    And I click on "create-btn"

@no_auth_registry
  Scenario: Build the images with and without activation key
    Given I am on the Systems overview page of this "build_host"
    When I schedule the build of image "suse_key" via XML-RPC calls
    And I wait at most 600 seconds until event "Image Build suse_key scheduled by admin" is completed
    And I schedule the build of image "suse_simple" via XML-RPC calls
    And I wait at most 600 seconds until event "Image Build suse_simple scheduled by admin" is completed
    And I schedule the build of image "suse_real_key" via XML-RPC calls
    And I wait at most 600 seconds until event "Image Build suse_real_key scheduled by admin" is completed

@no_auth_registry
  Scenario: Build same images with different versions
    Given I am authorized with the feature's user
    When I schedule the build of image "suse_key" with version "Latest_key-activation1" via XML-RPC calls
    And I schedule the build of image "suse_simple" with version "Latest_simple" via XML-RPC calls
    And I wait at most 1000 seconds until all "5" container images are built correctly in the GUI

@no_auth_registry
  Scenario: Delete image via XML-RPC calls
    Given I am authorized with the feature's user
    When I delete the image "suse_key" with version "Latest_key-activation1" via XML-RPC calls
    And I delete the image "suse_simple" with version "Latest_simple" via XML-RPC calls
    Then the image "suse_simple" with version "Latest_key-activation1" doesn't exist via XML-RPC calls
    And the image "suse_simple" with version "Latest_simple" doesn't exist via XML-RPC calls

@no_auth_registry
  Scenario: Rebuild the images
    Given I am authorized with the feature's user
    When I schedule the build of image "suse_simple" with version "Latest_simple" via XML-RPC calls
    And I schedule the build of image "suse_key" with version "Latest_key-activation1" via XML-RPC calls
    And I wait at most 1000 seconds until all "5" container images are built correctly in the GUI

@no_auth_registry
  Scenario: Build an image via the GUI
    Given I am authorized with the feature's user
    When I follow the left menu "Images > Build"
    And I select "suse_real_key" from "profileId"
    And I enter "GUI_BUILT_IMAGE" as "version"
    And I select the hostname of "build_host" from "buildHostId"
    And I click on "submit-btn"
    Then I wait until I see "GUI_BUILT_IMAGE" text

@no_auth_registry
  Scenario: Login as Docker image administrator and build an image
    Given I am authorized as "docker" with password "docker"
    When I follow the left menu "Images > Build"
    And I select "suse_real_key" from "profileId"
    And I enter "GUI_DOCKERADMIN" as "version"
    And I select the hostname of "build_host" from "buildHostId"
    And I click on "submit-btn"
    Then I wait until I see "GUI_DOCKERADMIN" text

@no_auth_registry
  Scenario: Cleanup: delete all images
    Given I am authorized with the feature's user
    When I delete the image "suse_key" with version "Latest" via XML-RPC calls
    And I delete the image "suse_simple" with version "Latest_simple" via XML-RPC calls
    And I delete the image "suse_key" with version "Latest_key-activation1" via XML-RPC calls
    And I delete the image "suse_real_key" with version "GUI_BUILT_IMAGE" via XML-RPC calls
    And I delete the image "suse_real_key" with version "GUI_DOCKERADMIN" via XML-RPC calls

@no_auth_registry
  Scenario: Cleanup: kill stale image build jobs
    When I kill remaining Salt jobs on "build_host"

@no_auth_registry
  Scenario: Cleanup: delete all profiles
    Given I am authorized with the feature's user
    When I follow the left menu "Images > Profiles"
    And I check "suse_simple" in the list
    And I check "suse_real_simple" in the list
    And I check "suse_key" in the list
    And I check "suse_real_key" in the list
    And I click on "Delete"
    And I should see a "Are you sure you want to delete selected profiles?" text
    And I click on the red confirmation button
    And I wait until I see "Image profiles have been deleted" text
