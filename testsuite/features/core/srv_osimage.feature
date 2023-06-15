# Copyright (c) 2018-2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in:
# If the "kiwikiwi" user fails to be created:
# - features/secondary/buildhost_osimage_build_image.feature


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
