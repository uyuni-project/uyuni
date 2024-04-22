# Copyright (c) 2021-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
@proxy
@sle12sp5_buildhost
Feature: Prepare buildhost and build OS image for SLES 12 SP5

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create the bootstrap repository for a SLES 12 SP5 build host
     When I create the bootstrap repository for "sle12sp5_buildhost" on the server

  Scenario: Clean up sumaform leftovers on a SLES 12 SP5 build host
    When I perform a full salt minion cleanup on "sle12sp5_buildhost"

  Scenario: Prepare activation key for SLES 12 SP5 build host
    When I create an activation key including custom channels for "sle12sp5_buildhost" via API

  Scenario: Bootstrap the SLES 12 SP5 build host
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle12sp5_buildhost" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-sle12sp5_buildhost_key" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait at most 500 seconds until onboarding is completed for "sle12sp5_buildhost"

  Scenario: Check the new SLES 12 SP5 bootstrapped build host in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    When I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "sle12sp5_buildhost", refreshing the page
    And I wait until onboarding is completed for "sle12sp5_buildhost"
    Then the Salt master can reach "sle12sp5_buildhost"

  Scenario: Create an OS image profile for SLES 12 SP5 with activation key
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_os_image_12" as "label"
    And I select "Kiwi" from "imageType"
    And I select "1-sle12sp5_minion_key" from "activationKey"
    And I enter the image filename for "sle12sp5_terminal" relative to profiles as "path"
    And I click on "create-btn"
    And I wait until no Salt job is running on "sle12sp5_buildhost"

  # WORKAROUND
  # Remove as soon as the issue is fixed
  Scenario: Work around issue https://github.com/SUSE/spacewalk/issues/10360
    When I let Kiwi build from external repositories

  Scenario: Build Kiwi image for SLES 12 SP5
    When I follow the left menu "Images > Build"
    And I select "suse_os_image_12" from "profileId"
    And I select the hostname of "sle12sp5_buildhost" from "buildHostId"
    And I click on "submit-btn"

  Scenario: Check the built SLES 12 SP5 OS image
    Given I am on the Systems overview page of this "sle12sp5_buildhost"
    When I wait until I see "[OS Image Build Host]" text
    And I wait until the image build "suse_os_image_12" is completed
    And I wait until the image inspection for "sle12sp5_terminal" is completed
    And I wait until no Salt job is running on "sle12sp5_buildhost"
    And I am on the image store of the Kiwi image for organization "1"
    Then I should see the name of the image for "sle12sp5_terminal"

@skip_if_containerized_server
  Scenario: Move the SLES 12 SP5 image to the branch server
    When I apply state "image-sync" to "proxy"
    Then the image for "sle12sp5_terminal" should exist on the branch server
