# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Adding channels
  In Order distribute software to the clients
  As an authorized user
  I want to add channels

  Background:
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu

  Scenario: Add a base channel
    When I follow "Software"
    And I follow "Manage" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Create Channel"
    And I enter "Test Base Channel" as "Channel Name"
    And I enter "test_base_channel" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Base channel for testing" as "Channel Summary"
    And I enter "No more desdcription for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Test Base Channel created." text

  Scenario: Add a child channel
    When I follow "Software"
    And I follow "Manage" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Create Channel"
    When I enter "Test Child Channel" as "Channel Name"
    And I enter "test_child_channel" as "Channel Label"
    And I select "Test Base Channel" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Child channel for testing" as "Channel Summary"
    And I enter "Description for Test Child Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Test Child Channel created." text

  Scenario: Add a base test channel for i586
    When I follow "Software"
    And I follow "Manage" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Create Channel"
    And I enter "Test-Channel-i586" as "Channel Name"
    And I enter "test-channel-i586" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "IA-32" from "Architecture:"
    And I enter "Test-Channel-i586 channel for testing" as "Channel Summary"
    And I enter "No more desdcription for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Test-Channel-i586 created." text

  Scenario: Add a child channel to the i586 test channel
    When I follow "Software"
    And I follow "Manage" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Create Channel"
    And I enter "Test-Channel-i586 Child Channel" as "Channel Name"
    And I enter "test-channel-i586-child-channel" as "Channel Label"
    And I select "Test-Channel-i586" from "Parent Channel"
    And I select "IA-32" from "Architecture:"
    And I enter "Test-Channel-i586 child channel for testing" as "Channel Summary"
    And I enter "Description for Test-Channel-i586 Child Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Test-Channel-i586 Child Channel created." text

  Scenario: Add a test base channel for x86_64
    When I follow "Software"
    And I follow "Manage" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Create Channel"
    And I enter "Test-Channel-x86_64" as "Channel Name"
    And I enter "test-channel-x86_64" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Test-Channel-x86_64 channel for testing" as "Channel Summary"
    And I enter "No more desdcription for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Test-Channel-x86_64 created." text

  Scenario: Add a child channel to the x86_64 test channel
    When I follow "Software"
    And I follow "Manage" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Create Channel"
    And I enter "Test-Channel-x86_64 Child Channel" as "Channel Name"
    And I enter "test-channel-x86_64-child-channel" as "Channel Label"
    And I select "Test-Channel-x86_64" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Test-Channel-x86_64 child channel for testing" as "Channel Summary"
    And I enter "Description for Test-Channel-x86_64 Child Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Test-Channel-x86_64 Child Channel created." text

  Scenario: Add Fedora x86_64 base channel
    When I follow "Software"
    And I follow "Manage" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Create Channel"
    And I enter "Fedora x86_64 Channel" as "Channel Name"
    And I enter "fedora-x86_64-channel" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Fedora x86_64 channel for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fedora x86_64 Channel created." text

  Scenario: Fail when trying to add a duplicate channel
     When I follow "Software"
     And I follow "Manage" in the left menu
     And I follow "Channels" in the left menu
     And I follow "Create Channel"
     And I enter "Test Base Channel" as "Channel Name"
     And I enter "test_base_channel" as "Channel Label"
     And I select "None" from "Parent Channel"
     And I select "x86_64" from "Architecture:"
     And I enter "Base channel for testing" as "Channel Summary"
     And I enter "No more desdcription for base channel." as "Channel Description"
     And I click on "Create Channel"
     Then I should see a "The channel name 'Test Base Channel' is already in use, please enter a different name" text

  Scenario: Fail when trying to use invalid characters in the channel label
      When I follow "Software"
      And I follow "Manage" in the left menu
      And I follow "Channels" in the left menu
      And I follow "Create Channel"
      And I enter "test123" as "Channel Name"
      And I enter "tesT123" as "Channel Label"
      And I enter "test123" as "Channel Summary"
      And I click on "Create Channel"
      Then I should see a "Invalid channel label, please see the format described below" text

  Scenario: Fail when trying to use invalid characters in the channel name
      When I follow "Software"
      And I follow "Manage" in the left menu
      And I follow "Channels" in the left menu
      And I follow "Create Channel"
      And I enter "!test123" as "Channel Name"
      And I enter "test123" as "Channel Label"
      And I enter "test123" as "Channel Summary"
      And I click on "Create Channel"
      Then I should see a "Invalid channel name, please see the format described below" text

  Scenario: Fail when trying to use reserved names for channels
    When I follow "Software"
     And I follow "Manage" in the left menu
     And I follow "Channels" in the left menu
     And I follow "Create Channel"
     And I enter "SLE-12-Cloud-Compute5-Pool for x86_64" as "Channel Name"
     And I enter "test123" as "Channel Label"
     And I enter "test123" as "Channel Summary"
     And I click on "Create Channel"
    Then I should see a "The channel name 'SLE-12-Cloud-Compute5-Pool for x86_64' is reserved, please enter a different name" text

  Scenario: Fail when trying to use reserved labels for channels
    When I follow "Software"
     And I follow "Manage" in the left menu
     And I follow "Channels" in the left menu
     And I follow "Create Channel"
     And I enter "test123" as "Channel Name"
     And I enter "sle-we12-pool-x86_64-sap" as "Channel Label"
     And I enter "test123" as "Channel Summary"
     And I click on "Create Channel"
    Then I should see a "The channel label 'sle-we12-pool-x86_64-sap' is reserved, please enter a different name" text

  Scenario: Create a channel that will be changed
    When I follow "Software"
     And I follow "Manage" in the left menu
     And I follow "Channels" in the left menu
     And I follow "Create Channel"
     And I enter "aaaSLE-12-Cloud-Compute5-Pool for x86_64" as "Channel Name"
     And I enter "sle-we12aaa-pool-x86_64-sap" as "Channel Label"
     And I enter "test123" as "Channel Summary"
     And I click on "Create Channel"
    Then I should see a "Channel aaaSLE-12-Cloud-Compute5-Pool for x86_64 created." text

  Scenario: Fail when trying to change the channel name to a reserved name
    When I follow "Software"
     And I follow "Manage" in the left menu
     And I follow "Channels" in the left menu
     And I follow "aaaSLE-12-Cloud-Compute5-Pool for x86_64"
     And I enter "SLE-12-Cloud-Compute5-Pool for x86_64" as "Channel Name"
     And I click on "Update Channel"
    Then I should see a "The channel name 'SLE-12-Cloud-Compute5-Pool for x86_64' is reserved, please enter a different name" text
