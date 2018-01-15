# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Salt minions configuration management

  Scenario: Create a configuration channel for both Salt minion and traditional client
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    And I follow "Create Config Channel"
    And I enter "Mixed Channel" as "cofName"
    And I enter "mixedchannel" as "cofLabel"
    And I enter "This is a configuration channel for both Salt minion and traditional client" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Mixed Channel" text

  Scenario: Add a configuration file to the mixed configuration channel
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    And I follow "Mixed Channel"
    And I follow "Create configuration file or directory"
    And I enter "/etc/s-mgr/config" as "cffPath"
    And I enter "COLOR=white" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/s-mgr/config from channel Mixed Channel" text
    And file "/srv/susemanager/salt/mgr_cfg_org_1/mixedchannel/init.sls" should exist on server
    And file "/srv/susemanager/salt/mgr_cfg_org_1/mixedchannel/etc/s-mgr/config" should exist on server

  Scenario: Subscribe a traditional client to the configuration channel
    When I am on the Systems overview page of this "sle-client"
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Subscribe a salt minion to the configuration channel
    When I am on the Systems overview page of this "sle-minion"
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Deploy the file to both systems
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I run "rhn-actions-control --enable-all" on "sle-client"
    And I follow "Configuration Channels" in the left menu
    And I follow "Mixed Channel"
    And I follow "Deploy all configuration files to all subscribed systems"
    Then I should see a "/etc/s-mgr/config" link
    And I should see "sle-client" as link
    When I click on "Deploy Files to Selected Systems"
    Then I should see a "2 revision-deploys are being scheduled," text
    And I should see a "0 revision-deploys overridden." text

  Scenario: Check that file has been created on both systems
    When I run "rhn_check -vvv" on "sle-client"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sle-client"
    And file "/etc/s-mgr/config" should contain "COLOR=white" on "sle-minion"

  Scenario: Apply highstate to override changed content
    When I store "COLOR=blue" into file "/etc/s-mgr/config" on "sle-minion"
    And I apply highstate on "sle-minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sle-minion"

  Scenario: Cleanup: remove both systems from configuration channel
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    And I follow "Mixed Channel"
    And I follow "Systems" in the content area
    And I check the "sle-client" client
    And I check the "sle-minion" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 2 system(s)." text

  Scenario: Cleanup: remove the mixed configuration channel
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    And I follow "Mixed Channel"
    And I follow "delete channel"
    And I click on "Delete Config Channel"
    Then file "/srv/susemanager/salt/mgr_cfg_org_1/mixedchannel/init.sls" should not exist on server

  Scenario: Cleanup: delete configuration files on both systems
    When I destroy "/etc/s-mgr" directory on "sle-client"
    And I destroy "/etc/s-mgr" directory on "sle-minion"
