# Copyright (c) 2015-17 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Create a configuration channel
  In Configuration
  As the admin user
  I want to create a configuration channel

  Background:
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu

  Scenario: Successfully create configuration channel
    When I follow "Create a New Configuration Channel"
    And I enter "Test Channel" as "cofName"
    And I enter "testchannel" as "cofLabel"
    And I enter "This is a test channel" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Test Channel" text
    And I should see a "Add Files" link
    And I should see a "Systems" link in the content area
    And I should see a "Edit Properties" link
    And I should see a "Configuration Actions" text
    And I should see a "Add/Create Files" text
    And I should see a "Create configuration file or directory" link
    And I should see a "Upload configuration files" link
    And I should see a "Import a file from another channel or system" link
    And I should see a "delete channel" link

  Scenario: Try to create same channel again; this should fail
    When I follow "Create a New Configuration Channel"
    And I enter "Test Channel" as "cofName"
    And I enter "testchannel" as "cofLabel"
    And I enter "This is a test channel" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Label 'testchannel' already exists. Please choose a different label for the new channel." text
    And I should see a "Update Channel" button

  Scenario: Try to create a channel with an invalid label
    When I follow "Create a New Configuration Channel"
    And I enter "Test Channel2" as "cofName"
    And I enter "!testchannel" as "cofLabel"
    And I enter "This is a test channel 2" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Configuration channel label contains invalid characters. In addition to alphanumeric characters, '-', '_', and '.' are allowed." text
    And I should see a "Update Channel" button
