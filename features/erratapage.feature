# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Explore the Errata page
  In Order to validate completeness of the errata page
  As a authorized user
  I want to see all the texts and links

  Scenario: Completeness of the Patches left menu
    Given I am on the errata page
    Then I should see a "Errata Relevant to Your Systems" text
    And I should see a "Relevant" link in the left menu
    And I should see a "All" link in the left menu
    And I should see a "Advanced Search" link in the left menu
    And I should see a "Manage Errata" link in the left menu
    And I should see a "Clone Errata" link in the left menu
    And I should see a "Bugfix Errata" link
    And I should see a "Enhancement Errata" link
    And I should see a "Security Errata" link
    And I should see a Sign Out link

  Scenario: Create new bugfix erratum with bnc URL
    Given I am on the errata page
    And I follow "Manage Patches" in the left menu
    And I follow "Published" in the left menu
    And I follow "Create Patch"
    When I enter "Test Erratum" as "synopsis"
    And I enter "Test Advisory" as "advisoryName"
    And I enter "Test Product" as "product"
    And I enter "test@test.org" as "errataFrom"
    And I enter "Test Topic" as "topic"
    And I enter "Test Description" as "description"
    And I enter "Test Solution" as "solution"
    And I enter "620212" as "buglistId"
    And I enter "Test Summary" as "buglistSummary"
    And I enter "https://bugzilla.novell.com/show_bug.cgi?id=620212" as "buglistUrl"
    And I enter "test,keywords" as "keywords"
    And I enter "Test Reference" as "refersTo"
    And I enter "Test Note" as "notes"
    And I click on "Create Errata"
    Then I should see a "Errata Test Advisory-1 created." text

  Scenario: Create new enhancement erratum with no bnc URL
    Given I am on the errata page
    And I follow "Manage Patches" in the left menu
    And I follow "Published" in the left menu
    And I follow "Create Patch"
    When I enter "Enhancement Erratum" as "synopsis"
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
    And I click on "Create Errata"
    Then I should see a "Errata Enhancement Advisory-1 created." text

  Scenario: Delete enhancement erratum
    Given I am on the errata page
    And I follow "Manage Patches" in the left menu
    And I follow "Unpublished" in the left menu
    And I check "Enhancement Advisory" erratum
    And I click on "Delete Errata"
    And I click on "Confirm"
    Then I should see a "Successfully deleted 1 errata." text

  Scenario: Publish erratum
    Given I am on the errata page
    And I follow "Manage Patches" in the left menu
    And I follow "Unpublished" in the left menu
    And I follow "Test Advisory"
    And I click on "Publish Errata"
    And I check test channel
    And I click on "Publish Errata"
    Then I should see a "All Types" text
    And I should see a "Test Erratum" text

  Scenario: View bugfix errata
    Given I am on the errata page
    And I follow "All" in the left menu
    And I follow "Bugfix Patches" in the content area
    Then I should see a "Test Erratum" text

  Scenario: View erratum
    Given I am on the errata page
    And I follow "All" in the left menu
    And I follow "Bugfix Patches" in the content area
    And I follow "Test Advisory"
    Then I should see a "Test Erratum" text
    And I should see a "test@test.org" text
    And I should see a "Test Topic" text
    And I should see a "Test Description" text
    And I should see a "Test Solution" text
    And I should see a "Test Base Channel" link
    And I should see a "Test Summary" link
    And I should see a "keywords, test" text
    And I should see a "Test Reference" text
    And I should see a "Test Note" text

  Scenario: Check erratum in channel
    Given I am on the errata page
    And I follow "Software" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Channels > All" in the left menu
    And I follow "Test Base Channel"
    And I follow "Errata" in the content area
    Then I should see a "Test Erratum" text

  Scenario: Delete erratum
    Given I am on the errata page
    And I follow "Manage Patches" in the left menu
    And I follow "Published" in the left menu
    And I check "Test Advisory" erratum
    And I click on "Delete Errata"
    And I click on "Confirm"
    Then I should see a "Successfully deleted 1 errata." text
