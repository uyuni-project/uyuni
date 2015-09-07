# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

#@wip
Feature: Adding channels
  In Order distribute software to the clients
  As an authorized user
  I want to add channels

  Background:
    Given I am testing channels

  Scenario: Adding a base channel
    And I follow "Channels"
    And I follow "Manage Software Channels" in the left menu
    And I follow "Create Channel"
    When I enter "Test Base Channel" as "Channel Name"
    And I enter "test_base_channel" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Base channel for testing" as "Channel Summary"
    And I enter "No more desdcription for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Test Base Channel created." text

  Scenario: Adding a child channel
    And I follow "Channels"
    And I follow "Manage Software Channels" in the left menu
    And I follow "Create Channel"
    When I enter "Test Child Channel" as "Channel Name"
    And I enter "test_child_channel" as "Channel Label"
    And I select "Test Base Channel" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Child channel for testing" as "Channel Summary"
    And I enter "Description for Test Child Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Test Child Channel created." text

  Scenario: Adding SLES11-SP3-Updates i586 base channel
    And I follow "Channels"
    And I follow "Manage Software Channels" in the left menu
    And I follow "Create Channel"
    When I enter "SLES11-SP3-Updates i586 Channel" as "Channel Name"
    And I enter "sles11-sp3-updates-i586-channel" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "IA-32" from "Architecture:"
    And I enter "SLES11-SP3-Updates i586 channel for testing" as "Channel Summary"
    And I enter "No more desdcription for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel SLES11-SP3-Updates i586 Channel created." text

  Scenario: Adding a child channel to SLES11-SP3-Updates i586
    And I follow "Channels"
    And I follow "Manage Software Channels" in the left menu
    And I follow "Create Channel"
    When I enter "SLES11-SP3-Updates i586 Child Channel" as "Channel Name"
    And I enter "sles11-sp3-updates-i586-child-channel" as "Channel Label"
    And I select "SLES11-SP3-Updates i586 Channel" from "Parent Channel"
    And I select "IA-32" from "Architecture:"
    And I enter "SLES11-SP3-Updates i586 child channel for testing" as "Channel Summary"
    And I enter "Description for SLES11-SP3-Updates i586 Child Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel SLES11-SP3-Updates i586 Child Channel created." text

  Scenario: Adding SLES11-SP3-Updates x86_64 base channel
    And I follow "Channels"
    And I follow "Manage Software Channels" in the left menu
    And I follow "Create Channel"
    When I enter "SLES11-SP3-Updates x86_64 Channel" as "Channel Name"
    And I enter "sles11-sp3-updates-x86_64-channel" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "SLES11-SP3-Updates x86_64 channel for testing" as "Channel Summary"
    And I enter "No more desdcription for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel SLES11-SP3-Updates x86_64 Channel created." text

  Scenario: Adding a child channel to SLES11-SP3-Updates x86_64
    And I follow "Channels"
    And I follow "Manage Software Channels" in the left menu
    And I follow "Create Channel"
    When I enter "SLES11-SP3-Updates x86_64 Child Channel" as "Channel Name"
    And I enter "sles11-sp3-updates-x86_64-child-channel" as "Channel Label"
    And I select "SLES11-SP3-Updates x86_64 Channel" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "SLES11-SP3-Updates x86_64 child channel for testing" as "Channel Summary"
    And I enter "Description for SLES11-SP3-Updates x86_64 Child Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel SLES11-SP3-Updates x86_64 Child Channel created." text

  Scenario: Adding Fedora x86_64 base channel
    And I follow "Channels"
    And I follow "Manage Software Channels" in the left menu
    And I follow "Create Channel"
    When I enter "Fedora x86_64 Channel" as "Channel Name"
    And I enter "fedora-x86_64-channel" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Fedora x86_64 channel for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fedora x86_64 Channel created." text

   Scenario: Adding a duplicate channel fails
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     And I follow "Create Channel"
     When I enter "Test Base Channel" as "Channel Name"
     And I enter "test_base_channel" as "Channel Label"
     And I select "None" from "Parent Channel"
     And I select "x86_64" from "Architecture:"
     And I enter "Base channel for testing" as "Channel Summary"
     And I enter "No more desdcription for base channel." as "Channel Description"
     And I click on "Create Channel"
     Then I should see a "The channel name 'Test Base Channel' is already in use, please enter a different name" text

    Scenario: I am not allowed to use invalid characters in the channel label
      And I follow "Channels"
      And I follow "Manage Software Channels" in the left menu
      And I follow "Create Channel"
      When I enter "test123" as "Channel Name"
      And I enter "tesT123" as "Channel Label"
      And I enter "test123" as "Channel Summary"
      And I click on "Create Channel"
      Then I should see a "Invalid channel label, please see the format described below" text

    Scenario: I am not allowed to invalid characters in the channel name
      And I follow "Channels"
      And I follow "Manage Software Channels" in the left menu
      And I follow "Create Channel"
      When I enter "!test123" as "Channel Name"
      And I enter "test123" as "Channel Label"
      And I enter "test123" as "Channel Summary"
      And I click on "Create Channel"
      Then I should see a "Invalid channel name, please see the format described below" text

    Scenario: I am not allowed to use a reserved name
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     And I follow "Create Channel"
    When I enter "SLE-12-Cloud-Compute5-Pool for x86_64" as "Channel Name"
     And I enter "test123" as "Channel Label"
     And I enter "test123" as "Channel Summary"
     And I click on "Create Channel"
    Then I should see a "The channel name 'SLE-12-Cloud-Compute5-Pool for x86_64' is reserved, please enter a different name" text

   Scenario: I am not allowed to use a reserved label
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     And I follow "Create Channel"
    When I enter "test123" as "Channel Name"
     And I enter "sle-we12-pool-x86_64-sap" as "Channel Label"
     And I enter "test123" as "Channel Summary"
     And I click on "Create Channel"
    Then I should see a "The channel label 'sle-we12-pool-x86_64-sap' is reserved, please enter a different name" text

   Scenario: I create a channel to change
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     And I follow "Create Channel"
    When I enter "aaaSLE-12-Cloud-Compute5-Pool for x86_64" as "Channel Name"
     And I enter "sle-we12aaa-pool-x86_64-sap" as "Channel Label"
     And I enter "test123" as "Channel Summary"
     And I click on "Create Channel"
    Then I should see a "Channel aaaSLE-12-Cloud-Compute5-Pool for x86_64 created." text

  Scenario: I try to change the channel name to a reserved name
     And I follow "Channels"
     And I follow "Manage Software Channels" in the left menu
     When I follow "aaaSLE-12-Cloud-Compute5-Pool for x86_64"
     And I enter "SLE-12-Cloud-Compute5-Pool for x86_64" as "Channel Name"
     And I click on "Update Channel"
    Then I should see a "The channel name 'SLE-12-Cloud-Compute5-Pool for x86_64' is reserved, please enter a different name" text
