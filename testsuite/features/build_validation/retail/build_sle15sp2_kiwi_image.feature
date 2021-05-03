# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Build OS image for SLES 15 SP2

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create an JeOS7 profile with activation key
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "jeos7" as "label"
    And I select "Kiwi" from "imageType"
    And I select "1-sle15sp2_minion_key" from "activationKey"
    And I enter "https://github.com/SUSE/manager-build-profiles#master:OSImage/POS_Image-JeOS7" as "path"
    And I click on "create-btn"

  Scenario: Set record of proxy at SLES 15 SP2 buildhost hosts file if avahi is used
    When I add proxy record into hosts file on "sle15sp2_buildhost" if avahi is used

  Scenario: Login as Kiwi image administrator and build an JeOS7 image
    When I follow the left menu "Images > Build"
    And I select "jeos7" from "profileId"
    And I select the hostname of "sle15sp2_buildhost" from "buildHostId"
    And I click on "submit-btn"

  Scenario: Check the JeOS7 image built as Kiwi image administrator
    Given I am on the Systems overview page of this "sle15sp2_buildhost"
    Then I should see a "[OS Image Build Host]" text
    When I wait until the image build "jeos7" is completed
    And I am on the image store of the kiwi image for organization "1"

  Scenario: Move JeOS7 image to the branch server
    When I manually install the "image-sync" formula on the server
    And I enable repositories before installing branch server
    And I synchronize all Salt dynamic modules on "proxy"
    And I apply state "image-sync" to "proxy"
