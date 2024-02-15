# Copyright (c) 2021-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@sle15sp4_buildhost
Feature: Prepare buildhost and build OS image for SLES 15 SP4

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create the bootstrap repository for a SLES 15 SP4 build host
     When I create the bootstrap repository for "sle15sp4_buildhost" on the server

  Scenario: Clean up sumaform leftovers on a SLES 15 SP4 build host
    When I perform a full salt minion cleanup on "sle15sp4_buildhost"

  Scenario: Prepare activation key for SLES 15 SP4 build host
    When I create an activation key including custom channels for "sle15sp4_buildhost" via API

  Scenario: Bootstrap the SLES 15 SP4 build host
    When I bootstrap "sle15sp4_buildhost" using bootstrap script with activation key "1-sle15sp4_buildhost_key" from the proxy
    And I wait at most 10 seconds until Salt master sees "sle15sp4_buildhost" as "unaccepted"
    And I accept "sle15sp4_buildhost" key in the Salt master
    And I wait until onboarding is completed for "sle15sp4_buildhost"

  Scenario: Apply the highstate to the SLES 15 SP4 build host
    Given I am on the Systems overview page of this "sle15sp4_buildhost"
    When I wait until no Salt job is running on "sle15sp4_buildhost"
    And I apply highstate on "sle15sp4_buildhost"
    And I wait until file "/var/lib/Kiwi/repo/rhn-org-trusted-ssl-cert-osimage-1.0-1.noarch.rpm" exists on "sle15sp4_buildhost"

  Scenario: Create an OS image profile for SLES 15 SP4 with activation key
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_os_image_15" as "label"
    And I select "Kiwi" from "imageType"
    And I select "1-sle15sp4_minion_key" from "activationKey"
    And I enter the image filename for "sle15sp4_terminal" relative to profiles as "path"
    And I click on "create-btn"

  # WORKAROUND
  # Remove as soon as the issue is fixed
  Scenario: Work around issue https://github.com/SUSE/spacewalk/issues/10360
    When I let Kiwi build from external repositories

  Scenario: Build Kiwi image for SLES 15 SP4
    When I follow the left menu "Images > Build"
    And I select "suse_os_image_15" from "profileId"
    And I select the hostname of "sle15sp4_buildhost" from "buildHostId"
    And I click on "submit-btn"

  Scenario: Check the built SLES 15 SP4 OS image
    Given I am on the Systems overview page of this "sle15sp4_buildhost"
    When I wait until I see "[OS Image Build Host]" text
    And I wait until the image build "suse_os_image_15" is completed
    And I wait until the image inspection for "sle15sp4_terminal" is completed
    And I am on the image store of the Kiwi image for organization "1"
    Then I should see the name of the image for "sle15sp4_terminal"

@proxy
  Scenario: Move the SLES 15 SP4 image to the branch server
    When I apply state "image-sync" to "proxy"
    Then the image for "sle15sp4_terminal" should exist on the branch server
