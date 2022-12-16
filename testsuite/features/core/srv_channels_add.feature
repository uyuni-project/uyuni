# Copyright (c) 2015-2022 SUSE LLC
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
    And I enter "Fake Base Channel" as "Channel Name"
    And I enter "fake_base_channel" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Base channel for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake Base Channel created." text

  Scenario: Add a child channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake Child Channel" as "Channel Name"
    And I enter "fake_child_channel" as "Channel Label"
    And I select "Fake Base Channel" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Child channel for testing" as "Channel Summary"
    And I enter "Description for Fake Child Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake Child Channel created." text

  Scenario: Add a base test channel for i586
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-i586-Channel" as "Channel Name"
    And I enter "fake-i586-channel" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "IA-32" from "Architecture:"
    And I enter "Fake-i586-Channel channel for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake-i586-Channel created." text

  Scenario: Add a child channel to the i586 test channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-i586-Channel Child Channel" as "Channel Name"
    And I enter "fake-i586-channel-child-channel" as "Channel Label"
    And I select "Fake-i586-Channel" from "Parent Channel"
    And I select "IA-32" from "Architecture:"
    And I enter "Fake-i586-Channel child channel for testing" as "Channel Summary"
    And I enter "Description for Fake-i586-Channel Child Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake-i586-Channel Child Channel created." text

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

  Scenario: Add Debian-like AMD64 base channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-Deb-AMD64-Channel" as "Channel Name"
    And I enter "fake-deb-amd64-channel" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "AMD64 Debian" from "Architecture:"
    And I enter "Fake-Deb-AMD64-Channel for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    # WORKAROUND
    # GPG verification of Debian-like repos is possible with an own GPG key.
    # This is not yet part of the testsuite and we run with disabled checkes for Ubuntu/Debian
    And I uncheck "gpg_check"
    # End of WORKAROUND
    And I click on "Create Channel"
    Then I should see a "Channel Fake-Deb-AMD64-Channel created." text

  Scenario: Add a RedHat-like base channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-RH-Like-Channel" as "Channel Name"
    And I enter "fake-rh-like-channel" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Fake-RH-Like-Channel for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake-RH-Like-Channel created." text

  Scenario: Wait for Channels generated initial metadata
    When I wait until the channel "test-channel-x86_64" has been synced
    And I wait until the channel "fake-i586-channel" has been synced
