# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_visualization
Feature: Patches page
  In order to use the Patches page
  As a authorized user
  I want to see all the texts and links

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Patches left menu
    Given I am on the patches page
    Then I should see a "Patches Relevant to Your Systems" text
    And I should see a "Relevant" link in the left menu
    And I should see a "All" link in the left menu
    And I should see a "Advanced Search" link in the left menu
    And I should see a "Manage Patches" link in the left menu
    And I should see a "Clone Patches" link in the left menu
    And I should see a "Bugfix Patches" link
    And I should see a "Enhancement Patches" link
    And I should see a "Security Patches" link
    And I should see a Sign Out link

  Scenario: Create new bugfix patch with bnc URL
    When I follow the left menu "Patches > Manage Patches"
    And I follow "Create Patch"
    When I enter "Test Patch" as "synopsis"
    And I enter "Test Advisory" as "advisoryName"
    And I enter "Test Product" as "product"
    And I enter "test@test.org" as "errataFrom"
    And I enter "Test Topic" as "topic"
    And I enter "Test Description" as "description"
    And I enter "Test Solution" as "solution"
    And I enter "620212" as "buglistId"
    And I enter "Test Summary" as "buglistSummary"
    And I enter "https://bugzilla.opensuse.org/show_bug.cgi?id=620212" as "buglistUrl"
    And I enter "test,keywords" as "keywords"
    And I enter "Test Reference" as "refersTo"
    And I enter "Test Note" as "notes"
    And I check test channel
    And I click on "Create Patch"
    Then I should see a "Patch: Test Advisory-1" text

  Scenario: Create new enhancement patch with no bnc URL
    When I follow the left menu "Patches > Manage Patches"
    And I follow "Create Patch"
    When I enter "Enhancement Patch" as "synopsis"
    And I enter "Enhancement Advisory" as "advisoryName"
    And I select "Product Enhancement Advisory" from "advisoryType"
    And I enter "Enhancement Product" as "product"
    And I enter "Enhancement Topic" as "topic"
    And I enter "Enhancement Description" as "description"
    And I enter "Enhancement Solution" as "solution"
    And I enter "1234" as "buglistId"
    And I enter "Enhancement Summary" as "buglistSummary"
    And I enter "Enhancement,keywords" as "keywords"
    And I enter "Enhancement Reference" as "refersTo"
    And I enter "Enhancement Note" as "notes"
    And I check test channel
    And I click on "Create Patch"
    Then I should see a "Patch: Enhancement Advisory-1" text

  Scenario: Delete enhancement patch
    When I follow the left menu "Patches > Manage Patches"
    And I check "Enhancement Advisory" patch
    And I click on "Delete Patches"
    And I click on "Confirm"
    Then I should see a "Successfully deleted 1 patches." text

  Scenario: Verify patch presence in web UI
    When I follow the left menu "Patches > Patch List > All"
    And I follow "Bugfix Patches" in the content area
    And I enter "Test Patch" as the filtered synopsis
    And I click on the filter button
    And I follow "Test Advisory"
    Then I should see a "Test Patch" text
    And I should see a "test@test.org" text
    And I should see a "Test Topic" text
    And I should see a "Test Description" text
    And I should see a "Test Solution" text
    And I should see a "Test Base Channel" link
    And I should see a "Test Summary" link
    And I should see a "keywords, test" text
    And I should see a "Test Reference" text
    And I should see a "Test Note" text

  Scenario: Assert that patch is now in test base channel
    When I follow the left menu "Software > Channel List > All"
    And I follow "Test Base Channel"
    And I follow "Patches" in the content area
    Then I should see a "Test Patch" text

  Scenario: Delete patch
    When I follow the left menu "Patches > Manage Patches"
    And I check "Test Advisory" patch
    And I click on "Delete Patches"
    And I click on "Confirm"
    Then I should see a "Successfully deleted 1 patches." text
