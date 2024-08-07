# Copyright (c) 2018-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_configuration_channels
Feature: Management of configuration of all types of clients in a single channel

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Create a configuration channel for mixed client types
    When I follow the left menu "Configuration > Channels"
    And I follow "Create Config Channel"
    And I enter "Mixed Channel" as "cofName"
    And I enter "mixedchannel" as "cofLabel"
    And I enter "This is a configuration channel for different system types" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Mixed Channel" text

  Scenario: Add a configuration file to the mixed configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Create Configuration File or Directory"
    And I enter "/etc/s-mgr/config" as "cffPath"
    And I enter "COLOR=white" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/s-mgr/config from channel Mixed Channel" text
    And file "/srv/susemanager/salt/manager_org_1/mixedchannel/init.sls" should exist on server
    And file "/srv/susemanager/salt/manager_org_1/mixedchannel/etc/s-mgr/config" should exist on server

@sle_minion
  Scenario: Subscribe a Salt minion to the configuration channel
    When I am on the Systems overview page of this "sle_minion"
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

@rhlike_minion
  Scenario: Subscribe a Red Hat-like minion to the configuration channel
    When I am on the Systems overview page of this "rhlike_minion"
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

@deblike_minion
  Scenario: Subscribe a Debian-like minion to the configuration channel
    When I am on the Systems overview page of this "deblike_minion"
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

@ssh_minion
  Scenario: Subscribe a SSH minion to the configuration channel
    When I am on the Systems overview page of this "ssh_minion"
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Deploy the file to all systems
    When I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Deploy all configuration files to all subscribed systems"
    Then I should see a "/etc/s-mgr/config" link
    When I click on "Deploy Files to Selected Systems"
    Then I should see a "revision-deploys are being scheduled," text
    And I should see a "0 revision-deploys overridden." text

@sle_minion
  Scenario: Check that file has been created on SLE minion
    When I wait until file "/etc/s-mgr/config" exists on "sle_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sle_minion"

@rhlike_minion
  Scenario: Check that file has been created on Red Hat-like minion
    When I wait until file "/etc/s-mgr/config" exists on "rhlike_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "rhlike_minion"

@deblike_minion
  Scenario: Check that file has been created on Debian-like minion
    When I wait until file "/etc/s-mgr/config" exists on "deblike_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "deblike_minion"

@ssh_minion
  Scenario: Check that file has been created on SSH minion
    When I wait until file "/etc/s-mgr/config" exists on "ssh_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "ssh_minion"

@sle_minion
  Scenario: Apply highstate to override changed content on SLE minion
    When I store "COLOR=blue" into file "/etc/s-mgr/config" on "sle_minion"
    And I apply highstate on "sle_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sle_minion"

@rhlike_minion
  Scenario: Apply highstate to override changed content on Red Hat-like minion
    When I store "COLOR=blue" into file "/etc/s-mgr/config" on "rhlike_minion"
    And I apply highstate on "rhlike_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "rhlike_minion"

@deblike_minion
  Scenario: Apply highstate to override changed content on Debian-like minion
    When I store "COLOR=blue" into file "/etc/s-mgr/config" on "deblike_minion"
    And I apply highstate on "deblike_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "deblike_minion"

@ssh_minion
  Scenario: Apply highstate to override changed content on SSH minion
    When I store "COLOR=blue" into file "/etc/s-mgr/config" on "ssh_minion"
    And I apply highstate on "ssh_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "ssh_minion"

@rhlike_minion
  Scenario: Unsubscribe Red Hat-like minion and delete configuration files
    When I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Systems" in the content area
    And I check the "rhlike_minion" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text
    And I destroy "/etc/s-mgr" directory on "rhlike_minion"

@deblike_minion
  Scenario: Unsubscribe Debian-like minion and delete configuration files
    When I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Systems" in the content area
    And I check the "deblike_minion" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text
    And I destroy "/etc/s-mgr" directory on "deblike_minion"

@ssh_minion
  Scenario: Unsubscribe SSH minion and delete configuration files
    When I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Systems" in the content area
    And I check the "ssh_minion" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text
    And I destroy "/etc/s-mgr" directory on "ssh_minion"

@sle_minion
  Scenario: Change file on Salt minion and compare
    When I am on the Systems overview page of this "sle_minion"
    And I store "COLOR=red" into file "/etc/s-mgr/config" on "sle_minion"
    And I follow "Configuration" in the content area
    And I follow "Compare Files" in the content area
    And I check "/etc/s-mgr/config" in the list
    And I click on "Compare Files"
    And I click on "Schedule Compare"
    Then I should see a "1 files scheduled for comparison." text
    When I wait until event "Show differences between profiled config files and deployed config files scheduled" is completed
    Then I should see a "Differences exist" link
    When I follow "Differences exist"
    Then I should see a "+COLOR=white" text
    And I should see a "-COLOR=red" text

@sle_minion
  Scenario: Check configuration channel and files via API for Salt minion
    Then channel "mixedchannel" should exist
    And channel "mixedchannel" should contain file "/etc/s-mgr/config"
    And "sle_minion" should be subscribed to channel "mixedchannel"

@sle_minion
  Scenario: Extend configuration channel and deploy files via API for Salt minion
    When I store "COLOR=green" into file "/etc/s-mgr/config" on "sle_minion"
    And I add file "/etc/s-mgr/other" containing "NAME=Dante" to channel "mixedchannel"
    And I deploy all systems registered to channel "mixedchannel"
    And I wait until file "/etc/s-mgr/other" exists on "sle_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sle_minion"
    And file "/etc/s-mgr/other" should contain "NAME=Dante" on "sle_minion"

@sle_minion
  Scenario: Unsubscribe systems via API for Salt minion
    When I unsubscribe "sle_minion" from configuration channel "mixedchannel"
    Then "sle_minion" should not be subscribed to channel "mixedchannel"

@sle_minion
  Scenario: Re-add SLE Minion via SSM
    When I follow the left menu "Systems > System List > All"
    And I click on the clear SSM button
    And I check the "sle_minion" client
    And I follow the left menu "Systems > System Set Manager > Overview"
    And I follow "config channel subscriptions" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Apply Subscriptions"
    And I click on "Confirm"
    Then I should see a "Configuration channel subscriptions changed for 1 system successfully." text

@sle_minion
  Scenario: Cleanup: remove remaining Salt minion from configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Systems" in the content area
    And I check the "sle_minion" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text

  Scenario: Cleanup: remove the mixed configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"
    Then file "/srv/susemanager/salt/manager_org_1/mixedchannel/init.sls" should not exist on server

@sle_minion
  Scenario: Cleanup: delete configuration files on remaining Salt minion
    When I destroy "/etc/s-mgr" directory on "sle_minion"

  Scenario: Cleanup: remove remaining systems from SSM after tests of configuration channel on all clients
    When I click on the clear SSM button
