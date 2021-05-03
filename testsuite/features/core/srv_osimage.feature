# Copyright (c) 2018-2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Prepare server for using Kiwi

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create a Kiwi user with image administrators rights
    When I follow the left menu "Users > User List > Active"
    And I follow "Create User"
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
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "Kiwi testing" as "description"
    And I enter "KIWI-TEST" as "key"
    And I enter "20" as "usageLimit"
    And I select "Test-Channel-x86_64" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    Then I should see a "Activation key Kiwi testing has been created" text
