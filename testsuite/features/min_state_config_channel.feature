# Copyright (c) 2018 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: State Configuration channels
  In order to configure systems through Salt
  I want to be able to use channels from the state tab

  Scenario: Create a state channel
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
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

  Scenario: Create a state channel with same name
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
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

  Scenario: Subscribe a minion to the state channel
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

  Scenario: Apply the new state
    When I am on the Systems overview page of this "sle-minion"
    And I follow "States" in the content area
    And I follow "Configuration Channels" in the content area
    Then I should see a "Apply" button
    When I click on "Apply"
    Then I should see a "Applying the config channels has been scheduled" text
    When I wait until event "Apply states [custom] scheduled by admin" is completed
    And I wait until file "/root/statechannel" exists on "sle-minion"
    And I wait until file "/root/statechannel2" exists on "sle-minion"

  Scenario: Cleanup: remove the state channel and the file
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    And I follow first "My State Channel"
    And I follow "Delete Channel"
    Then I should see a "Are you sure you want to delete this config channel?" text
    When I click on "Delete Config Channel"
    Then I should see a "Channel 'My State Channel' has been deleted." text
    When I follow first "My State Channel"
    And I follow "Delete Channel"
    Then I should see a "Are you sure you want to delete this config channel?" text
    When I click on "Delete Config Channel"
    Then I should see a "Channel 'My State Channel' has been deleted." text
    And I remove "/root/statechannel" from "sle-minion"
    And I remove "/root/statechannel2" from "sle-minion"
