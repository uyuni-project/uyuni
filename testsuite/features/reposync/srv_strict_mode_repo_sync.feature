# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Strict Mode for Channel Synchronization
  In order to properly manage channel synchronisation
  As an admin user
  I want to be able to decide if deleted packages from upstream repos are dropped or kept

Scenario: Create Test-strict-channel
  Given I am authorized for the "Admin" section
    And I prepare a channel clone for strict mode testing
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Test-Strict-Channel" as "Channel Name"
    And I enter "test-strict-channel" as "Channel Label"
    And I enter "Test-Strict-Channel for testing" as "Channel Summary"
    And I click on "Create Channel"
    Then I should see a "Channel Test-Strict-Channel created." text

Scenario: Prepare repos for strict test
    Given I am authorized for the "Admin" section
    And I follow the left menu "Software > Manage > Repositories"
    And I follow "Create Repository"
    And I enter "fake-rpm-repo-modified" as "label"
    And I enter "http://localhost/pub/TestRepoRpmUpdates_STRICT_TEST/" as "url"
    And I uncheck "metadataSigned"
    And I click on "Create Repository"
    Then I should see a "Repository created successfully" text

Scenario: Testing strict mode
    Given I am authorized for the "Admin" section
    And I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Strict-Channel"
    And I follow "Repositories" in the "content area"
    And I check "fake-rpm-repo" in the list
    And I click on "Save Repositories"
    And I follow "Sync" in the "content area"
    And I click on "Sync Now"	
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    And I store the amount of packages in channel "test-strict-channel"
    And I follow "Add / Remove"
    And I uncheck "fake-rpm-repo" in the list
    And I check "fake-rpm-repo-modified" in the list
    And I click on "Save Repositories"
    And I follow "Sync" in the "content area"
    And I check "no-strict"
    And I click on "Sync Now"	
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    Then The amount of packages in channel "Test-Strict-Channel" should be the same as before
    And I uncheck "no-strict"
    And I click on "Sync Now"	
    And I wait at most 60 seconds until I do not see "Repository sync is running." text, refreshing the page
    Then The amount of packages in channel "Test-Strict-Channel" should be fewer than before
