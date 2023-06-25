# Copyright (c) 2018-2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature relies on having properly configured
#   /etc/rhn/rhn.conf
# file on your Uyuni server.
#
# For the scope of these tests, we configure it as follows:
#   java.kiwi_os_image_building_enabled = true
# which means "Enable Kiwi OS Image building"
#
# This feature can cause failures in the following features:
# - features/secondary/proxy_retail_pxeboot_and_mass_import.feature:
# This feature leaves a JeOS image built that is used in the "PXE boot a Retail terminal" feature.

@skip_if_github_validation
@skip_if_cloud
@buildhost
@scope_retail
@scope_building_container_images
Feature: Build OS images

  Scenario: Create an OS image profile with activation key
    Given I am authorized for the "Admin" section
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_os_image" as "label"
    And I select "Kiwi" from "imageType"
    And I select "1-SUSE-KEY-x86_64" from "activationKey"
    And I enter the image filename for "pxeboot_minion" relative to profiles as "path"
    And I click on "create-btn"

  # WORKAROUND
  # Remove as soon as the issue is fixed
  Scenario: Work around issue https://github.com/SUSE/spacewalk/issues/10360
    When I let Kiwi build from external repositories

  Scenario: Login as Kiwi image administrator and build an image
    Given I am authorized for the "Images" section
    When I follow the left menu "Images > Build"
    And I select "suse_os_image" from "profileId"
    And I select the hostname of "build_host" from "buildHostId"
    And I click on "submit-btn"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Check the built OS image
    Given I am on the Systems overview page of this "build_host"
    Then I should see a "[OS Image Build Host]" text
    When I wait until the image build "suse_os_image" is completed
    And I wait until the image inspection for "pxeboot_minion" is completed
    And I am on the image store of the Kiwi image for organization "1"
    Then I should see the name of the image for "pxeboot_minion"

  Scenario: Cleanup: remove remaining systems from SSM after OS image tests
    When I go to the home page
    And I click on the clear SSM button

  Scenario: Cleanup: remove OS image profile
    When I follow the left menu "Images > Profiles"
    And I check "suse_os_image" in the list
    And I click on "Delete"
    And I should see a "Are you sure you want to delete the selected profile?" text
    And I click on the red confirmation button
    And I wait until I see "Image profile has been deleted" text

  Scenario: Cleanup: Make sure no job is left running on buildhost
    When I wait until no Salt job is running on "build_host"
