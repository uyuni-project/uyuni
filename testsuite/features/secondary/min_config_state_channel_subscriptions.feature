# Copyright (c) 2018-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

@sle_minion
@scope_configuration_channels
Feature: State Configuration channels
  In order to configure systems through Salt
  I want to be able to use channels from the state tab

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create the 1st state channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Create State Channel"
    Then I should see a "New Config State Channel" text
    When I enter "My State Channel" as "cofName"
    And I enter "statechannel" as "cofLabel"
    And I enter "This is a state channel" as "cofDescription"
    And I enter "touch /root/statechannel:\n  cmd.run:\n    - creates: /root/statechannel" in the editor
    And I click on "Create Config State Channel"
    Then I should see a "State Channel" text
    And I should see a "Channel Properties" text
    And I should see a "Channel Information" text
    And I should see a "Configuration Actions" text

  Scenario: Create the 2nd state channel with same name
    When I follow the left menu "Configuration > Channels"
    And I follow "Create State Channel"
    Then I should see a "New Config State Channel" text
    When I enter "My State Channel" as "cofName"
    And I enter "statechannel2" as "cofLabel"
    And I enter "This is a state channel" as "cofDescription"
    And I enter "touch /root/statechannel2:\n  cmd.run:\n    - creates: /root/statechannel2" in the editor
    And I click on "Create Config State Channel"
    Then I should see a "State Channel" text
    And I should see a "Channel Properties" text
    And I should see a "Channel Information" text
    And I should see a "Configuration Actions" text

  Scenario: Create the 3rd state channel with spacecmd
    When I create channel "statechannel3" from spacecmd of type "state"
    And I follow the left menu "Configuration > Channels"
    Then I should see a "statechannel3" text
    When I update init.sls from spacecmd with content "touch /tmp/statechannel3:\n  cmd.run:\n    - creates: /tmp/statechannel3" for channel "statechannel3"
    And I get "/init.sls" file details for channel "statechannel3" via spacecmd
    Then I should see "Revision: 2" in the output
    When  I update init.sls from spacecmd with content "touch /root/statechannel3:\n  cmd.run:\n    - creates: /root/statechannel3" for channel "statechannel3" and revision "100"
    And I get "/init.sls" file details for channel "statechannel3" via spacecmd
    Then I should see "Revision: 100" in the output

  Scenario: Subscribe a minion to 1st and 2nd state channels
    When I am on the Systems overview page of this "sle_minion"
    And I follow "States" in the content area
    And I follow "Configuration Channels" in the content area
    And I click on "Search" in element "search-row"
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
    And I follow "States" in the content area
    And I follow "Configuration Channels" in the content area
    And I click on "Search" in element "search-row"
    Then I should see a "Execute States" button
    When I click on "Execute States"
    Then I should see a "Applying the config channels has been scheduled" text
    When I wait until event "Apply states [custom] scheduled by admin" is completed
    And I wait until file "/root/statechannel" exists on "sle_minion"
    And I wait until file "/root/statechannel2" exists on "sle_minion"

  Scenario: Subscribe a minion to the 3rd state channel
    And I follow "States" in the content area
    And I follow "Configuration Channels" in the content area
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
    When I schedule apply configchannels for "sle_minion"
    And I wait until file "/root/statechannel3" exists on "sle_minion"

  Scenario: Cleanup: remove the 1st state channel and the deployed file
    When I follow the left menu "Configuration > Channels"
    And I follow first "My State Channel"
    And I follow "Delete Channel"
    Then I should see a "Are you sure you want to delete this config channel?" text
    When I click on "Delete Config Channel"
    Then I should see a "Channel 'My State Channel' has been deleted." text
    And I remove "/root/statechannel" from "sle_minion"

  Scenario: Cleanup: remove the 2nd state channel and the deployed file
    When I follow the left menu "Configuration > Channels"
    And I follow first "My State Channel"
    And I follow "Delete Channel"
    Then I should see a "Are you sure you want to delete this config channel?" text
    When I click on "Delete Config Channel"
    Then I should see a "Channel 'My State Channel' has been deleted." text
    And I remove "/root/statechannel2" from "sle_minion"

  Scenario: Cleanup: remove the 3rd state channel and the deployed file
    When I follow the left menu "Configuration > Channels"
    And I follow first "statechannel3"
    And I follow "Delete Channel"
    Then I should see a "Are you sure you want to delete this config channel?" text
    When I click on "Delete Config Channel"
    Then I should see a "Channel 'statechannel3' has been deleted." text
    And I remove "/root/statechannel3" from "sle_minion"
