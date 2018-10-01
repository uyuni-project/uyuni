# Copyright (c) 2018 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: State Configuration channels
  In order to configure systems through Salt
  I want to be able to use channels from the state tab

  Scenario: Create the 1st state channel
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Create State Channel"
    Then I should see a "New Config Channel" text
    When I enter "My State Channel" as "cofName"
    And I enter "statechannel" as "cofLabel"
    And I enter "This is a state channel" as "cofDescription"
    And I enter "touch /root/statechannel:\n  cmd.run:\n    - creates: /root/statechannel" in the editor
    And I click on "Create Config Channel"
    Then I should see a "State Channel" text
    And I should see a "Channel Properties" text
    And I should see a "Channel Information" text
    And I should see a "Configuration Actions" text

  Scenario: Create the 2nd state channel with same name
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Create State Channel"
    Then I should see a "New Config Channel" text
    When I enter "My State Channel" as "cofName"
    And I enter "statechannel2" as "cofLabel"
    And I enter "This is a state channel" as "cofDescription"
    And I enter "touch /root/statechannel2:\n  cmd.run:\n    - creates: /root/statechannel2" in the editor
    And I click on "Create Config Channel"
    Then I should see a "State Channel" text
    And I should see a "Channel Properties" text
    And I should see a "Channel Information" text
    And I should see a "Configuration Actions" text

  Scenario: Create the 3rd state channel with spacecmd
    Given I am authorized as "admin" with password "admin"
    When I create channel "statechannel3" from spacecmd of type "state"
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    Then I should see a "statechannel3" text
    And  I update init.sls from spacecmd with content "touch /root/statechannel3:\n  cmd.run:\n    - creates: /root/statechannel3" for channel "statechannel3"

  Scenario: Subscribe a minion to 1st and 2nd state channels
    When I am on the Systems overview page of this "sle-minion"
    And I follow "States" in the content area
    And I follow "Configuration Channels" in the content area
    And I click on "Search"
    Then I should see a "My State Channel" text
    And I should see a "statechannel" text
    And I should see a "statechannel2" text
    And I check "statechannel-cbox"
    And I check "statechannel2-cbox"
    When I click on "Save Changes"
    Then I should see a "Edit Channel Ranks" text
    And I should see a "My State Channel (statechannel)" link
    And I should see a "My State Channel (statechannel2)" link
    When I click on "Confirm"
    Then I should see a "State assignments have been saved." text

  Scenario: Apply the Configuration channel state
    When I am on the Systems overview page of this "sle-minion"
    And I follow "States" in the content area
    And I follow "Configuration Channels" in the content area
    Then I should see a "Apply" button
    When I click on "Apply"
    Then I should see a "Applying the config channels has been scheduled" text
    When I wait until event "Apply states [custom] scheduled by admin" is completed
    And I wait until file "/root/statechannel" exists on "sle-minion"
    And I wait until file "/root/statechannel2" exists on "sle-minion"

  Scenario: Subscribe a minion to the 3rd state channel
    When I am on the Systems overview page of this "sle-minion"
    And I follow "States" in the content area
    And I follow "Configuration Channels" in the content area
    And I click on "Search"
    Then I should see a "My State Channel" text
    And I should see a "statechannel3" text
    And I check "statechannel3-cbox"
    When I click on "Save Changes"
    Then I should see a "Edit Channel Ranks" text
    And I should see a "My State Channel (statechannel)" link
    And I should see a "My State Channel (statechannel2)" link
    And I should see a "statechannel3 (statechannel3)" link
    When I click on "Confirm"
    Then I should see a "State assignments have been saved." text

  Scenario: Apply the Configuration channel state with spacecmd
    Given I am authorized as "admin" with password "admin"
    When I schedule apply configchannels for "sle-minion"
    And I wait until file "/root/statechannel3" exists on "sle-minion"

  Scenario: Cleanup: remove the 1st state channel and the deployed file
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    And I follow first "My State Channel"
    And I follow "Delete Channel"
    Then I should see a "Are you sure you want to delete this config channel?" text
    When I click on "Delete Config Channel"
    Then I should see a "Channel 'My State Channel' has been deleted." text
    And I remove "/root/statechannel" from "sle-minion"

  Scenario: Cleanup: remove the 2nd state channel and the deployed file
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    And I follow first "My State Channel"
    And I follow "Delete Channel"
    Then I should see a "Are you sure you want to delete this config channel?" text
    When I click on "Delete Config Channel"
    Then I should see a "Channel 'My State Channel' has been deleted." text
    And I remove "/root/statechannel2" from "sle-minion"

  Scenario: Cleanup: remove the 3rd state channel and the deployed file
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    And I follow first "statechannel3"
    And I follow "Delete Channel"
    Then I should see a "Are you sure you want to delete this config channel?" text
    When I click on "Delete Config Channel"
    Then I should see a "Channel 'statechannel3' has been deleted." text
    And I remove "/root/statechannel3" from "sle-minion"
