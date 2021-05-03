# Copyright (c) 2015-2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Adding channels
  In Order to distribute software to the clients
  As an authorized user
  I want to add channels

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add a base channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Test Base Channel" as "Channel Name"
    And I enter "test_base_channel" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Base channel for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Test Base Channel created." text

  Scenario: Add a child channel
    When I follow the left menu "Software > Manage > Channels"
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
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Test-Channel-i586" as "Channel Name"
    And I enter "test-channel-i586" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "IA-32" from "Architecture:"
    And I enter "Test-Channel-i586 channel for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Test-Channel-i586 created." text

  Scenario: Add a child channel to the i586 test channel
    When I follow the left menu "Software > Manage > Channels"
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
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Test-Channel-x86_64" as "Channel Name"
    And I enter "test-channel-x86_64" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Test-Channel-x86_64 channel for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Test-Channel-x86_64 created." text

  Scenario: Add a child channel to the x86_64 test channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Test-Channel-x86_64 Child Channel" as "Channel Name"
    And I enter "test-channel-x86_64-child-channel" as "Channel Label"
    And I select "Test-Channel-x86_64" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Test-Channel-x86_64 child channel for testing" as "Channel Summary"
    And I enter "Description for Test-Channel-x86_64 Child Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Test-Channel-x86_64 Child Channel created." text

  Scenario: Add Ubuntu AMD64 base channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Test-Channel-Deb-AMD64" as "Channel Name"
    And I enter "test-channel-deb-amd64" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "AMD64 Debian" from "Architecture:"
    And I enter "Test-Channel-Deb-AMD64 for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    # WORKAROUND
    # GPG verification of Debian-like repos was added and the TestRepoDebUpdates repo
    # is signed by a GPG key that is not in the keyring. This workaround temporarily
    # disables GPG check, before this is properly handled at sumaform/terraform level.
    And I uncheck "gpg_check"
    # End of WORKAROUND
    And I click on "Create Channel"
    Then I should see a "Channel Test-Channel-Deb-AMD64 created." text
