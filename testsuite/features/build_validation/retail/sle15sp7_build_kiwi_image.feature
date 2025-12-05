# Copyright (c) 2025 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature depends on:
#   features/build_validation/retail/init_sle15sp7_buildhost.feature

@proxy
@sle15sp7_terminal
Feature: Build OS image for SLES 15 SP7

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Prepare activation key for SLES 15 SP6 terminal
    When I create an activation key including custom channels for "sle15sp7_terminal" via API

  @sle15sp7_buildhost
  Scenario: Create an OS image profile for SLES 15 SP7 with activation key
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_os_image_15_sp7" as "label"
    And I select "Kiwi" from "imageType"
    And I select "1-sle15sp7_terminal_key" from "activationKey"
    And I enter the image filename for "sle15sp7_terminal" relative to profiles as "path"
    And I click on "create-btn"
    And I wait until no Salt job is running on "sle15sp7_buildhost"

  @sle15sp7_buildhost
  Scenario: Build Kiwi image for SLES 15 SP7
    When I follow the left menu "Images > Build"
    And I select "suse_os_image_15_sp7" from "profileId"
    And I select the hostname of "sle15sp7_buildhost" from "buildHostId"
    And I click on "submit-btn"

  @sle15sp7_buildhost
  Scenario: Wait for the built SLES 15 SP7 OS image
    Given I am on the Systems overview page of this "sle15sp7_buildhost"
    When I wait until I see "[OS Image Build Host]" text
    And I wait until the image build "suse_os_image_15_sp7" is completed
    And I wait until the image inspection for "sle15sp7_terminal" is completed
    And I wait until no Salt job is running on "sle15sp7_buildhost"
    And I follow the left menu "Images > Image List"
    Then I should see the image for "sle15sp7_terminal" is built

  Scenario: Check the built SLES 15 SP7 OS image
    When I open the details page of the image for "sle15sp7_terminal"
    Then I should see a link to download the image for "sle15sp7_terminal"
