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
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "Kiwi testing" as "description"
    And I enter "KIWI-TEST" as "key"
    And I enter "20" as "usageLimit"
    And I select "Test-Channel-x86_64" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    Then I should see a "Activation key Kiwi testing has been created" text

  Scenario: Create an OS image profile with activation key
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Images > Profiles"
    And I follow "Create"
    And I enter "suse_os_image" as "label"
    And I select "Kiwi" from "imageType"
    And I select "1-KIWI-TEST" from "activationKey"
    And I enter "Kiwi/POS_Image-JeOS6" relative to profiles as "path"
    And I click on "create-btn"
