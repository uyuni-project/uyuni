# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Management of configuration of all types of clients in a single channel

  Scenario: Create a configuration channel for mixed client types
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Create Config Channel"
    And I enter "Mixed Channel" as "cofName"
    And I enter "mixedchannel" as "cofLabel"
    And I enter "This is a configuration channel for different system types" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Mixed Channel" text

  Scenario: Add a configuration file to the mixed configuration channel
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Mixed Channel"
    And I follow "Create Configuration File or Directory"
    And I enter "/etc/s-mgr/config" as "cffPath"
    And I enter "COLOR=white" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/s-mgr/config from channel Mixed Channel" text
    And file "/srv/susemanager/salt/manager_org_1/mixedchannel/init.sls" should exist on server
    And file "/srv/susemanager/salt/manager_org_1/mixedchannel/etc/s-mgr/config" should exist on server

  Scenario: Subscribe a traditional client to the configuration channel
    When I am on the Systems overview page of this "sle-client"
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Subscribe a Salt minion to the configuration channel
    When I am on the Systems overview page of this "sle-minion"
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

@centos_minion
  Scenario: Subscribe a CentOS minion to the configuration channel
    When I am on the Systems overview page of this "ceos-minion"
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

@ssh_minion
  Scenario: Subscribe a SSH minion to the configuration channel
    When I am on the Systems overview page of this "ssh-minion"
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Deploy the file to all systems
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I run "rhn-actions-control --enable-all" on "sle-client"
    And I follow "Channels" in the left menu
    And I follow "Mixed Channel"
    And I follow "Deploy all configuration files to all subscribed systems"
    Then I should see a "/etc/s-mgr/config" link
    And I should see "sle-client" as link
    And I should see "sle-minion" as link
    When I click on "Deploy Files to Selected Systems"
    Then I should see a " revision-deploys are being scheduled," text
    And I should see a "0 revision-deploys overridden." text

  Scenario: Check that file has been created on traditional client
    When I run "rhn_check -vvv" on "sle-client"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sle-client"

  Scenario: Check that file has been created on SLE minion
    When I wait until file "/etc/s-mgr/config" exists on "sle-minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sle-minion"

@centos_minion
  Scenario: Check that file has been created on CentOS minion
    When I wait until file "/etc/s-mgr/config" exists on "ceos-minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "ceos-minion"

@ssh_minion
  Scenario: Check that file has been created on SSH minion
    When I wait until file "/etc/s-mgr/config" exists on "ssh-minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "ssh-minion"

  Scenario: Apply highstate to override changed content on SLE minion
    When I store "COLOR=blue" into file "/etc/s-mgr/config" on "sle-minion"
    And I apply highstate on "sle-minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sle-minion"

@centos_minion
  Scenario: Apply highstate to override changed content on CentOS minion
    When I store "COLOR=blue" into file "/etc/s-mgr/config" on "ceos-minion"
    And I apply highstate on "ceos-minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "ceos-minion"

@ssh_minion
  Scenario: Apply highstate to override changed content on SSH minion
    When I store "COLOR=blue" into file "/etc/s-mgr/config" on "ssh-minion"
    And I apply highstate on "ssh-minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "ssh-minion"

@centos_minion
  Scenario: Unsubscribe CentOS minion and delete configuration files
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Mixed Channel"
    And I follow "Systems" in the content area
    And I check the "ceos-minion" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text
    And I destroy "/etc/s-mgr" directory on "ceos-minion"

@ssh_minion
  Scenario: Unsubscribe SSH minion and delete configuration files
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
    And I follow "Mixed Channel"
    And I follow "Systems" in the content area
    And I check the "ssh-minion" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text
    And I destroy "/etc/s-mgr" directory on "ssh-minion"

  Scenario: Change file on Salt minion and compare
    When I am on the Systems overview page of this "sle-minion"
    And I store "COLOR=red" into file "/etc/s-mgr/config" on "sle-minion"
    And I follow "Configuration" in the content area
    And I follow "Compare Files" in the content area
    And I check "/etc/s-mgr/config" in the list
    And I click on "Compare Files"
    And I click on "Schedule Compare"
    Then I should see a "1 files scheduled for comparison." text
    When I wait until event "Show differences between profiled config files and deployed config files scheduled by admin" is completed
    Then I should see a "Differences exist" link
    When I follow "Differences exist"
    ### WORKAROUND - please uncomment when issue is fixed
    # bsc#1078764 - CFG-MGMT-SALT: inconsistent UI between traditional clients and Salt minions when comparing files
    # Then I should see a "-COLOR=white" text
    # And I should see a "+COLOR=red" text
    Then I should see a "-COLOR=white+COLOR=red" text

  Scenario: Check configuration channel and files via XML-RPC
    Given I am logged in via XML-RPC configchannel as user "admin" and password "admin"
    Then channel "mixedchannel" should exist
    And channel "mixedchannel" should contain file "/etc/s-mgr/config"
    And "sle-client" should be subscribed to channel "mixedchannel"
    And "sle-minion" should be subscribed to channel "mixedchannel"
    And I logout from XML-RPC configchannel namespace

  Scenario: Extend configuration channel and deploy files via XML-RPC
    Given I am logged in via XML-RPC configchannel as user "admin" and password "admin"
    When I store "COLOR=green" into file "/etc/s-mgr/config" on "sle-minion"
    And I store "COLOR=yellow" into file "/etc/s-mgr/config" on "sle-client"
    And I add file "/etc/s-mgr/other" containing "NAME=Dante" to channel "mixedchannel"
    And I deploy all systems registered to channel "mixedchannel"
    And I run "rhn_check -vvv" on "sle-client"
    And I wait until file "/etc/s-mgr/other" exists on "sle-minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sle-minion"
    And file "/etc/s-mgr/other" should contain "NAME=Dante" on "sle-minion"
    And file "/etc/s-mgr/config" should contain "COLOR=white" on "sle-client"
    And file "/etc/s-mgr/other" should contain "NAME=Dante" on "sle-client"
    And I logout from XML-RPC configchannel namespace

  Scenario: Unsubscribe systems via XML-RPC
    Given I am logged in via XML-RPC system as user "admin" and password "admin"
    When I unsubscribe "sle-client" and "sle-minion" from configuration channel "mixedchannel"
    And I logout from XML-RPC system namespace
    And I am logged in via XML-RPC configchannel as user "admin" and password "admin"
    Then "sle-client" should not be subscribed to channel "mixedchannel"
    And "sle-minion" should not be subscribed to channel "mixedchannel"
    And I logout from XML-RPC configchannel namespace

  Scenario: Re-add systems via SSM
    Given I am authorized as "admin" with password "admin"
    When I am on the System Overview page
    And I check the "sle-client" client
    And I check the "sle-minion" client
    And I am on System Set Manager Overview
    And I follow "config channel subscriptions" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Apply Subscriptions"
    And I click on "Confirm"
    Then I should see a "Configuration channel subscriptions changed for 2 systems successfully." text

  Scenario: Cleanup: remove remaining systems from configuration channel
    Given I am authorized as "admin" with password "admin"
    When I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Channels" in the left menu
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
    And I follow "Channels" in the left menu
    And I follow "Mixed Channel"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"
    Then file "/srv/susemanager/salt/manager_org_1/mixedchannel/init.sls" should not exist on server

  Scenario: Cleanup: delete configuration files on remaining systems
    When I destroy "/etc/s-mgr" directory on "sle-client"
    And I destroy "/etc/s-mgr" directory on "sle-minion"
