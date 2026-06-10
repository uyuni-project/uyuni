# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Hub verification and cleanup
  Verify end-to-end functionality through peripheral
  Clean up all test data

  Scenario: Verify minion can use synced channel on peripheral
    Given I am authorized
    When I follow the left menu "Systems > System List > All"
    And I follow this "sle_minion" link
    And I follow "Software > Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "clone-fake-rpm-suse-channel"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text

  Scenario: Subscribe minion to synced channel
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

  Scenario: Install package from synced channel
    When I follow "Software > Packages > Install"
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait until event "Package Install/Upgrade scheduled by admin" is completed

  Scenario: Verify package installation succeeded
    When I follow "Software > Packages > List / Remove"
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    Then I should see a "andromeda-dummy" link

  Scenario: Remove package from minion
    When I check "andromeda-dummy" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    And I wait until event "Package Removal scheduled by admin" is completed

  Scenario: Unsubscribe minion from channel
    When I follow "Software > Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    And I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

  Scenario: Initiate channel sync from peripheral
    When I initiate channel sync from peripheral "peripheral_server"
    And I wait until I see "Synchronization started" text
    Then I should see a "Background" text

  Scenario: Navigate to peripherals configuration on hub
    Given I am authorized
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Peripherals Configuration"
    Then I should see the name of "peripheral_server"

  Scenario: Remove synced channels from peripheral
    When I remove synced channels from "peripheral_server"
    And I wait until I see "Channel configuration updated" text
    Then I should see a "Updated" text

  Scenario: Unregister peripheral from hub
    When I unregister "peripheral_server" from hub
    Then I should not see the name of "peripheral_server"

  Scenario: Delete cloned channel from hub
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone of Fake-RPM-SUSE-Channel"
    And I follow "Delete Channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Fake-RPM-SUSE-Channel" text

  Scenario: Delete custom channel from hub
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test Hub Custom Channel"
    And I follow "Delete Channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Test Hub Custom Channel" text

  Scenario: Verify cleanup completed
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Peripherals Configuration"
    Then I should not see the name of "peripheral_server"
