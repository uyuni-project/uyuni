# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Basic images do not contain zypper nor the name of the server,
# so the inspect functionality is not tested here.

Feature: Build container images

  Scenario: Build the images with and without activation key
    Given I am on the Systems overview page of this "sle-minion"
    When I schedule the build of image "suse_key" via XML-RPC calls
    And I wait at most 500 seconds until event "Image Build suse_key scheduled by admin" is completed
    And I schedule the build of image "suse_simple" via XML-RPC calls
    And I wait at most 500 seconds until event "Image Build suse_simple scheduled by admin" is completed
    And I schedule the build of image "suse_real_key" via XML-RPC calls
    And I wait at most 500 seconds until event "Image Build suse_real_key scheduled by admin" is completed

  Scenario: Build same images with different versions
    Given I am authorized as "admin" with password "admin"
    When I schedule the build of image "suse_key" with version "Latest_key-activation1" via XML-RPC calls
    And I schedule the build of image "suse_simple" with version "Latest_simple" via XML-RPC calls
    And I wait at most 1000 seconds until all "5" container images are built correctly in the GUI

  Scenario: Delete image via XML-RPC calls
    Given I am authorized as "admin" with password "admin"
    When I delete the image "suse_key" with version "Latest_key-activation1" via XML-RPC calls
    And I delete the image "suse_simple" with version "Latest_simple" via XML-RPC calls
    Then the image "suse_simple" with version "Latest_key-activation1" doesn't exist via XML-RPC calls
    And the image "suse_simple" with version "Latest_simple" doesn't exist via XML-RPC calls

  Scenario: Rebuild the images
    Given I am authorized as "admin" with password "admin"
    When I schedule the build of image "suse_simple" with version "Latest_simple" via XML-RPC calls
    And I schedule the build of image "suse_key" with version "Latest_key-activation1" via XML-RPC calls
    And I wait at most 1000 seconds until all "5" container images are built correctly in the GUI

  Scenario: Build an image via the GUI
    Given I am authorized as "admin" with password "admin"
    When I navigate to images build webpage
    And I select "suse_real_key" from "profileId"
    And I enter "GUI_BUILT_IMAGE" as "version"
    And I select sle-minion hostname in Build Host
    And I click on "submit-btn"
    Then I wait until I see "GUI_BUILT_IMAGE" text

  Scenario: Login as Docker image administrator and build an image
    Given I am authorized as "docker" with password "docker"
    When I navigate to images build webpage
    And I select "suse_real_key" from "profileId"
    And I enter "GUI_DOCKERADMIN" as "version"
    And I select sle-minion hostname in Build Host
    And I click on "submit-btn"
    Then I wait until I see "GUI_DOCKERADMIN" text

  Scenario: Cleanup: delete all images
    Given I am authorized as "admin" with password "admin"
    When I delete the image "suse_key" with version "Latest" via XML-RPC calls
    And I delete the image "suse_simple" with version "Latest_simple" via XML-RPC calls
    And I delete the image "suse_key" with version "Latest_key-activation1" via XML-RPC calls
    And I delete the image "suse_real_key" with version "GUI_BUILT_IMAGE" via XML-RPC calls
    And I delete the image "suse_real_key" with version "GUI_DOCKERADMIN" via XML-RPC calls
