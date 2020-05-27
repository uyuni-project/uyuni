# Copyright (c) 2018-2020 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature relies on having properly configured
#   /etc/rhn/rhn.conf
# file on your SUSE Manager server.
#
# For the scope of these tests, we configure it as follows:
#   java.kiwi_os_image_building_enabled = true
# which means "Enable Kiwi OS Image building"

@buildhost
Feature: Build OS images

  Scenario: Create an OS image profile with activation key
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_os_image" as "label"
    And I select "Kiwi" from "imageType"
    And I select "1-KIWI-TEST" from "activationKey"
    And I enter the image filename relative to profiles as "path"
    And I click on "create-btn"

  # WORKAROUND
  # Remove as soon as the issue is fixed
  Scenario: Work around issue https://github.com/SUSE/spacewalk/issues/10360
    When I let Kiwi build from external repositories

  Scenario: Login as Kiwi image administrator and build an image
    Given I am authorized as "kiwikiwi" with password "kiwikiwi"
    When I navigate to images build webpage
    And I select "suse_os_image" from "profileId"
    And I select the hostname of "build_host" from "buildHostId"
    And I click on "submit-btn"

  Scenario: Check the OS image built as Kiwi image administrator
    Given I am on the Systems overview page of this "build_host"
    Then I should see a "[OS Image Build Host]" text
    When I wait at most 3300 seconds until event "Image Build suse_os_image scheduled by kiwikiwi" is completed
    And I wait at most 300 seconds until event "Image Inspect 1//suse_os_image:latest scheduled by kiwikiwi" is completed
    And I navigate to "os-images/1/" page
    Then I should see the name of the image

@proxy
@private_net
  Scenario: Move the image to the branch server
    When I manually install the "image-sync" formula on the server
    And I enable repositories before installing branch server
    And I synchronize all Salt dynamic modules on "proxy"
    And I apply state "image-sync" to "proxy"
    Then the image should exist on "proxy"

  Scenario: Cleanup: remove the image from SUSE Manager server
    Given I am authorized as "admin" with password "admin"
    When I navigate to images webpage
    And I wait until I do not see "There are no entries to show." text
    And I check the first image
    And I click on "Delete"
    And I click on "Delete" in "Delete Selected Image(s)" modal
    And I wait until I see "Deleted successfully." text

  Scenario: Cleanup: Disable the repositories on branch server
    When I disable repositories after installing branch server

  Scenario: Cleanup: remove remaining systems from SSM after OS image tests
    When I am authorized as "admin" with password "admin"
    And I follow "Clear"

  Scenario: Cleanup: remove OS image profile
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Images > Profiles"
    And I check "suse_os_image" in the list
    And I click on "Delete"
    And I should see a "Are you sure you want to delete the selected profile?" text
    And I click on the red confirmation button
    And I wait until I see "Image profile has been deleted" text
