# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Prepare server for using Kiwi

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
    And I enter "suse_os_image" as "label"
    And I select "Kiwi" from "imageType"
    And I select "1-KIWI-TEST" from "activationKey"
    # TODO: use final URL
    And I enter "https://github.com/Bischoff/manager-build-profiles#testsuite-profile:Testsuite/OSImage/POS_Image-JeOS6" as "path"
    And I click on "create-btn"
