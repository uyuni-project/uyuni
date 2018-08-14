# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature relies on having properly configured
#   /etc/rhn/rhn.conf
# file on your SUSE Manager server.
#
# For the scope of these tests, we configure it as follows:
#   java.kiwi_os_image_building_enabled = true
# which means "Enable Kiwi OS Image building"

Feature: Build OS images

  Scenario: Create a Kiwi user with image administrators rights
    Given I am on the active Users page
    When I follow "Create User"
    And I enter "kiwikiwi" as "login"
    And I enter "kiwikiwi" as "desiredpassword"
    And I enter "kiwikiwi" as "desiredpasswordConfirm"
    And I select "Mr." from "prefix"
    And I enter "TestKiwi" as "firstNames"
    And I enter "TestKiwi" as "lastName"
    And I enter "kiwi@susy.de" as "email"
    And I click on "Create Login"
    Then I should see a "Account kiwikiwi created" text
    And I should see a "kiwikiwi" link
    And I should see a "normal user" text
    And I follow "kiwikiwi"
    And I check "role_image_admin"
    And I click on "Update"

  Scenario: Create Kiwi activation key
    Given I am on the Systems page
    When I follow "Activation Keys" in the left menu
    And I follow "Create Key"
    And I enter "Kiwi testing" as "description"
    And I enter "KIWI-TEST" as "key"
    And I enter "20" as "usageLimit"
    And I select "Test-Channel-x86_64" from "selectedChannel"
    And I click on "Create Activation Key"
    Then I should see a "Activation key Kiwi testing has been created" text

  Scenario: Turn the SLES minion into a OS Image build host
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "osimage_build_host"
    And I click on "Update Properties"
    Then I should see a "OS Image Build Host type has been applied." text
    And I should see a "Note: This action will not result in state application" text
    And I should see a "To apply the state, either use the states page or run state.highstate from the command line." text
    And I should see a "System properties changed" text

  Scenario: Check that the minion is now a OS image build host
    Given I am on the Systems overview page of this "sle-minion"
    Then I should see a "[OS Image Build Host]" text
    When I wait until no Salt job is running on "sle-minion"

  Scenario: Create an OS image profile with activation key
    Given I am authorized as "admin" with password "admin"
    When I follow "Images" in the left menu
    And I follow "Profiles" in the left menu
    And I follow "Create"
    And I enter "suse_osimage_real_simple" as "label"
    And I select "Kiwi" from "imageType"
    And I select "1-KIWI-TEST" from "activationKey"
    And I enter "https://github.com/SUSE/manager-build-profiles#master:OSImage/POS_Image-JeOS6" as "path"
    And I click on "create-btn"

  Scenario: Build OS image via GUI
    Given I am authorized as "admin" with password "admin"
    When I navigate to images build webpage
    And I select "suse_osimage_real_simple" from "profileId"
    And I select sle-minion hostname in Build Host
    And I click on "submit-btn"

  Scenario: Check the OS image built via GUI
    Given I am on the Systems overview page of this "sle-minion"
    When I wait until event "Image Build suse_osimage_real_simple scheduled by admin" is completed
    And I navigate to "os-images/1/" page
    Then I should see a "POS_Image-JeOS6" text

  Scenario: Login as Kiwi image administrator and build an image
    Given I am authorized as "kiwikiwi" with password "kiwikiwi"
    When I navigate to images build webpage
    And I select "suse_osimage_real_simple" from "profileId"
    And I select sle-minion hostname in Build Host
    And I click on "submit-btn"

  Scenario: Check the OS image built as Kiwi image administrator
    Given I am on the Systems overview page of this "sle-minion"
    When I wait until event "Image Build suse_osimage_real_simple scheduled by kiwikiwi" is completed
    And I navigate to "os-images/1/" page
    Then I should see a "POS_Image-JeOS6" text
