# Copyright (c) 2017-2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Basic images do not contain zypper nor the name of the server,
# so the inspect functionality is not tested here.
#
# This feature is a dependency for:
# - features/secondary/srv_docker_cve_audit.feature 
#
# This feature depends on:
# - features/secondary/min_docker_api.feature
#
# This feature can cause failures in the following features:
# - features/secondary/min_salt_install_with_staging.feature
# Due to the images listed in the CVE Audit images

@buildhost
@scope_building_container_images
@no_auth_registry
@skip_if_github_validation
Feature: Build container images

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create a simple image profile without activation key
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_simple" as "label"
    And I select "galaxy-registry" from "imageStore"
    And I enter "Docker" relative to profiles as "path"
    And I click on "create-btn"

  Scenario: Create a simple real image profile without activation key
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_real_simple" as "label"
    And I select "galaxy-registry" from "imageStore"
    And I enter "Docker/serverhost" relative to profiles as "path"
    And I click on "create-btn"

@scc_credentials
  Scenario: Create an image profile with activation key
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_key" as "label"
    And I select "galaxy-registry" from "imageStore"
    And I select "1-SUSE-KEY-x86_64" from "activationKey"
    And I enter "Docker" relative to profiles as "path"
    And I click on "create-btn"

@scc_credentials
  Scenario: Create a simple real image profile with activation key
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_real_key" as "label"
    And I select "galaxy-registry" from "imageStore"
    And I select "1-SUSE-KEY-x86_64" from "activationKey"
    And I enter "Docker/serverhost" relative to profiles as "path"
    And I click on "create-btn"

@scc_credentials
  Scenario: Build the suse_key image with and without activation key
    Given I am on the Systems overview page of this "build_host"
    When I schedule the build of image "suse_key" via API calls
    And I wait at most 660 seconds until event "Image Build suse_key scheduled by admin" is completed
    # We should see the same result via API.
    # Also, check that all inspect actions are finished:
    And I wait at most 600 seconds until image "suse_key" with version "latest" is built successfully via API
    And I wait at most 300 seconds until image "suse_key" with version "latest" is inspected successfully via API
    Then the list of packages of image "suse_key" with version "latest" is not empty


  Scenario: Build the suse_simple image with and without activation key
    Given I am on the Systems overview page of this "build_host"
    When I schedule the build of image "suse_simple" via API calls
    And I wait at most 660 seconds until event "Image Build suse_simple scheduled by admin" is completed
    And I wait at most 600 seconds until image "suse_simple" with version "latest" is built successfully via API
    And I wait at most 300 seconds until image "suse_simple" with version "latest" is inspected successfully via API
    Then the list of packages of image "suse_simple" with version "latest" is not empty

@scc_credentials
  Scenario: Build the suse_real_key image with and without activation key
    Given I am on the Systems overview page of this "build_host"  
    When I schedule the build of image "suse_real_key" via API calls
    And I wait at most 660 seconds until event "Image Build suse_real_key scheduled by admin" is completed
    And I wait at most 60 seconds until all "3" container images are built correctly on the Image List page
    And I wait at most 600 seconds until image "suse_real_key" with version "latest" is built successfully via API
    And I wait at most 300 seconds until image "suse_real_key" with version "latest" is inspected successfully via API
    Then the list of packages of image "suse_real_key" with version "latest" is not empty
    When I wait until no Salt job is running on "build_host"

@scc_credentials
  Scenario: Build suse_key images with different versions
    When I schedule the build of image "suse_key" with version "Latest_key-activation1" via API calls
    And I wait at most 600 seconds until image "suse_key" with version "Latest_key-activation1" is built successfully via API
    And I wait at most 300 seconds until image "suse_key" with version "Latest_key-activation1" is inspected successfully via API
    Then the list of packages of image "suse_key" with version "Latest_key-activation1" is not empty
    When I wait until no Salt job is running on "build_host"

  Scenario: Build suse_simple image with different versions
    When I schedule the build of image "suse_simple" with version "Latest_simple" via API calls
    And I wait at most 600 seconds until image "suse_simple" with version "Latest_simple" is built successfully via API
    And I wait at most 300 seconds until image "suse_simple" with version "Latest_simple" is inspected successfully via API
    Then the list of packages of image "suse_simple" with version "Latest_simple" is not empty
    When I wait until no Salt job is running on "build_host"

@scc_credentials
  Scenario: Delete image via API calls with key
    When I delete the image "suse_key" with version "Latest_key-activation1" via API calls
    Then the image "suse_simple" with version "Latest_key-activation1" doesn't exist via API calls
    When I wait until no Salt job is running on "build_host"

  Scenario: Delete image via API calls without key
    When I delete the image "suse_simple" with version "Latest_simple" via API calls
    Then the image "suse_simple" with version "Latest_simple" doesn't exist via API calls
    When I wait until no Salt job is running on "build_host"

  Scenario: Rebuild suse_simple image
    When I schedule the build of image "suse_simple" with version "Latest_simple" via API calls
    And I wait at most 600 seconds until image "suse_simple" with version "Latest_simple" is built successfully via API
    And I wait at most 300 seconds until image "suse_simple" with version "Latest_simple" is inspected successfully via API
    Then the list of packages of image "suse_simple" with version "Latest_simple" is not empty
    When I wait until no Salt job is running on "build_host"

@scc_credentials
  Scenario: Rebuild suse_key image
    When I schedule the build of image "suse_key" with version "Latest_key-activation1" via API calls
    And I wait at most 600 seconds until image "suse_key" with version "Latest_key-activation1" is built successfully via API
    And I wait at most 300 seconds until image "suse_key" with version "Latest_key-activation1" is inspected successfully via API
    Then the list of packages of image "suse_key" with version "Latest_key-activation1" is not empty
    When I wait until no Salt job is running on "build_host"

@scc_credentials
  Scenario: Build an image via the GUI
    When I follow the left menu "Images > Build"
    And I select "suse_real_key" from "profileId"
    And I enter "GUI_BUILT_IMAGE" as "version"
    And I select the hostname of "build_host" from "buildHostId"
    And I click on "submit-btn"
    And I wait until no Salt job is running on "build_host"
    Then I wait until I see "GUI_BUILT_IMAGE" text
    And I wait at most 600 seconds until image "suse_real_key" with version "GUI_BUILT_IMAGE" is built successfully via API
    And I wait at most 300 seconds until image "suse_real_key" with version "GUI_BUILT_IMAGE" is inspected successfully via API

@scc_credentials
  Scenario: Login as Docker image administrator and build an image
    Given I am authorized as "docker" with password "docker"
    When I follow the left menu "Images > Build"
    And I select "suse_real_key" from "profileId"
    And I enter "GUI_DOCKERADMIN" as "version"
    And I select the hostname of "build_host" from "buildHostId"
    And I click on "submit-btn"
    And I wait until no Salt job is running on "build_host"
    Then I wait until I see "GUI_DOCKERADMIN" text
    And I wait at most 600 seconds until image "suse_real_key" with version "GUI_DOCKERADMIN" is built successfully via API
    And I wait at most 300 seconds until image "suse_real_key" with version "GUI_DOCKERADMIN" is inspected successfully via API

@scc_credentials
  Scenario: Cleanup: delete all images with key
    Given I am authorized as "admin" with password "admin"
    When I delete the image "suse_key" with version "latest" via API calls
    And I delete the image "suse_key" with version "Latest_key-activation1" via API calls
    And I delete the image "suse_real_key" with version "latest" via API calls
    And I delete the image "suse_real_key" with version "GUI_BUILT_IMAGE" via API calls
    And I delete the image "suse_real_key" with version "GUI_DOCKERADMIN" via API calls

  Scenario: Cleanup: delete images without key
    Given I am authorized as "admin" with password "admin"
    When I delete the image "suse_simple" with version "latest" via API calls
    And I delete the image "suse_simple" with version "Latest_simple" via API calls
	
@scc_credentials
  Scenario: Cleanup: delete all profiles with key
    When I follow the left menu "Images > Profiles"
    And I check "suse_key" in the list
    And I check "suse_real_key" in the list
    And I click on "Delete"
    And I should see a "Are you sure you want to delete selected profiles?" text
    And I click on the red confirmation button
    And I wait until I see "Image profiles have been deleted" text

  Scenario: Cleanup: delete all profiles without key
    When I follow the left menu "Images > Profiles"
    And I check "suse_simple" in the list
    And I check "suse_real_simple" in the list
    And I click on "Delete"
    And I should see a "Are you sure you want to delete selected profiles?" text
    And I click on the red confirmation button
    And I wait until I see "Image profiles have been deleted" text

  Scenario: Cleanup: Make sure no job is left running on buildhost
    When I wait until no Salt job is running on "build_host"
