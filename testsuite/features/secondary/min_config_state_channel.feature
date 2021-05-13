# Copyright (c) 2018-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

@scope_configuration_channels
Feature: Configuration state channels
  In order to configure systems through Salt
  I want to be able to use the state channels

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create a state channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Create State Channel"
    Then I should see a "New Config State Channel" text
    When I enter "My State Channel" as "cofName"
    And I enter "statechannel" as "cofLabel"
    And I enter "This is a state channel" as "cofDescription"
    And I enter "touch /root/foobar:\n  cmd.run:\n    - creates: /root/foobar" in the editor
    And I click on "Create Config State Channel"
    Then I should see a "State Channel" text
    And I should see a "Channel Properties" text
    And I should see a "Channel Information" text
    And I should see a "Configuration Actions" text

  Scenario: Subscribe a minion to the state channel
    When I am on the Systems overview page of this "sle_minion"
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "My State Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Salt state details
    When I follow the left menu "Configuration > Channels"
    And I follow "My State Channel"
    Then I should see a "1 system subscribed" text
    When I follow "View/Edit 'init.sls' File"
    Then I should see a "Revision 1 of /init.sls from channel My State Channel" text
    And I should see a "File Contents" text
    And I should see a "touch /root/foobar:" text

  Scenario: Apply the new state
    When I am on the Systems overview page of this "sle_minion"
    And I follow "States" in the content area
    And I follow "Configuration Channels" in the content area
    Then I should see a "Apply" button
    When I click on "Apply"
    Then I should see a "Applying the config channels has been scheduled" text
    When I wait until event "Apply states [custom] scheduled by admin" is completed
    And I wait until file "/root/foobar" exists on "sle_minion"

  Scenario: Try to remove init.sls file
    When I follow the left menu "Configuration > Channels"
    And I follow "My State Channel"
    And I follow "View/Edit 'init.sls' File"
    When I follow "Delete This File Revision"
    And I click on "Delete Configuration Revision"
    Then I should see a "Cannot delete the only revision for the init.sls file" text
    And I should see a "Revision 1 of /init.sls from channel My State Channel" text

  Scenario: Cleanup: remove the state channel and the file
    When I follow the left menu "Configuration > Channels"
    And I follow "My State Channel"
    And I follow "Delete Channel"
    Then I should see a "Are you sure you want to delete this config channel?" text
    When I click on "Delete Config Channel"
    Then I should see a "Channel 'My State Channel' has been deleted." text
    And I remove "/root/foobar" from "sle_minion"
