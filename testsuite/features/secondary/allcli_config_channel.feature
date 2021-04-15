# Copyright (c) 2018-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_configuration_channels
Feature: Management of configuration of all types of clients in a single channel

  Scenario: Create a configuration channel for mixed client types
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Channels"
    And I follow "Create Config Channel"
    And I enter "Mixed Channel" as "cofName"
    And I enter "mixedchannel" as "cofLabel"
    And I enter "This is a configuration channel for different system types" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Mixed Channel" text

  Scenario: Add a configuration file to the mixed configuration channel
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Create Configuration File or Directory"
    And I enter "/etc/s-mgr/config" as "cffPath"
    And I enter "COLOR=white" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/s-mgr/config from channel Mixed Channel" text
    And file "/srv/susemanager/salt/manager_org_1/mixedchannel/init.sls" should exist on server
    And file "/srv/susemanager/salt/manager_org_1/mixedchannel/etc/s-mgr/config" should exist on server

@sle_client
  Scenario: Subscribe a traditional client to the configuration channel
    When I am on the Systems overview page of this "sle_client"
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

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

@centos_minion
  Scenario: Subscribe a CentOS minion to the configuration channel
    When I am on the Systems overview page of this "ceos_minion"
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

@ubuntu_minion
  Scenario: Subscribe a Ubuntu minion to the configuration channel
    When I am on the Systems overview page of this "ubuntu_minion"
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
    Given I am authorized as "admin" with password "admin"
    When I run "rhn-actions-control --enable-all" on "sle_client"
    And I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Deploy all configuration files to all subscribed systems"
    Then I should see a "/etc/s-mgr/config" link
    When I click on "Deploy Files to Selected Systems"
    Then I should see a "revision-deploys are being scheduled," text
    And I should see a "0 revision-deploys overridden." text

@sle_client
  Scenario: Check that file has been created on traditional client
    When I run "rhn_check -vvv" on "sle_client"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sle_client"

@sle_minion
  Scenario: Check that file has been created on SLE minion
    When I wait until file "/etc/s-mgr/config" exists on "sle_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sle_minion"

@centos_minion
  Scenario: Check that file has been created on CentOS minion
    When I wait until file "/etc/s-mgr/config" exists on "ceos_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "ceos_minion"

@ubuntu_minion
  Scenario: Check that file has been created on Ubuntu minion
    When I wait until file "/etc/s-mgr/config" exists on "ubuntu_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "ubuntu_minion"

@ssh_minion
  Scenario: Check that file has been created on SSH minion
    When I wait until file "/etc/s-mgr/config" exists on "ssh_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "ssh_minion"

@sle_minion
  Scenario: Apply highstate to override changed content on SLE minion
    When I store "COLOR=blue" into file "/etc/s-mgr/config" on "sle_minion"
    And I apply highstate on "sle_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sle_minion"

@centos_minion
  Scenario: Apply highstate to override changed content on CentOS minion
    When I store "COLOR=blue" into file "/etc/s-mgr/config" on "ceos_minion"
    And I apply highstate on "ceos_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "ceos_minion"

@ubuntu_minion
  Scenario: Apply highstate to override changed content on Ubuntu minion
    When I store "COLOR=blue" into file "/etc/s-mgr/config" on "ubuntu_minion"
    And I apply highstate on "ubuntu_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "ubuntu_minion"

@ssh_minion
  Scenario: Apply highstate to override changed content on SSH minion
    When I store "COLOR=blue" into file "/etc/s-mgr/config" on "ssh_minion"
    And I apply highstate on "ssh_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "ssh_minion"

@centos_minion
  Scenario: Unsubscribe CentOS minion and delete configuration files
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Systems" in the content area
    And I check the "ceos_minion" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text
    And I destroy "/etc/s-mgr" directory on "ceos_minion"

@ubuntu_minion
  Scenario: Unsubscribe Ubuntu minion and delete configuration files
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Systems" in the content area
    And I check the "ubuntu_minion" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text
    And I destroy "/etc/s-mgr" directory on "ubuntu_minion"

@ssh_minion
  Scenario: Unsubscribe SSH minion and delete configuration files
    Given I am authorized as "admin" with password "admin"
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
    When I wait until event "Show differences between profiled config files and deployed config files scheduled by admin" is completed
    Then I should see a "Differences exist" link
    When I follow "Differences exist"
    Then I should see a "+COLOR=white" text
    And I should see a "-COLOR=red" text

@sle_client
  Scenario: Check configuration channel and files via XML-RPC for Traditional Client
    Given I am logged in via XML-RPC configchannel as user "admin" and password "admin"
    Then channel "mixedchannel" should exist
    And channel "mixedchannel" should contain file "/etc/s-mgr/config"
    And "sle_client" should be subscribed to channel "mixedchannel"
    And I logout from XML-RPC configchannel namespace

@sle_minion
  Scenario: Check configuration channel and files via XML-RPC for Salt Minion
    Given I am logged in via XML-RPC configchannel as user "admin" and password "admin"
    Then channel "mixedchannel" should exist
    And channel "mixedchannel" should contain file "/etc/s-mgr/config"
    And "sle_minion" should be subscribed to channel "mixedchannel"
    And I logout from XML-RPC configchannel namespace

@sle_minion
  Scenario: Extend configuration channel and deploy files via XML-RPC for Salt Minion
    Given I am logged in via XML-RPC configchannel as user "admin" and password "admin"
    When I store "COLOR=green" into file "/etc/s-mgr/config" on "sle_minion"
    And I add file "/etc/s-mgr/other" containing "NAME=Dante" to channel "mixedchannel"
    And I deploy all systems registered to channel "mixedchannel"
    And I wait until file "/etc/s-mgr/other" exists on "sle_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sle_minion"
    And file "/etc/s-mgr/other" should contain "NAME=Dante" on "sle_minion"
    And I logout from XML-RPC configchannel namespace

@sle_client
  Scenario: Extend configuration channel and deploy files via XML-RPC for Traditional Client
    Given I am logged in via XML-RPC configchannel as user "admin" and password "admin"
    And I store "COLOR=yellow" into file "/etc/s-mgr/config" on "sle_client"
    And I add file "/etc/s-mgr/other" containing "NAME=Dante" to channel "mixedchannel"
    And I deploy all systems registered to channel "mixedchannel"
    And I run "rhn_check -vvv" on "sle_client"
    And file "/etc/s-mgr/config" should contain "COLOR=white" on "sle_client"
    And file "/etc/s-mgr/other" should contain "NAME=Dante" on "sle_client"
    And I logout from XML-RPC configchannel namespace

@sle_client
  Scenario: Unsubscribe systems via XML-RPC for Traditional Client
    Given I am logged in via XML-RPC system as user "admin" and password "admin"
    When I unsubscribe "sle_client" from configuration channel "mixedchannel"
    And I logout from XML-RPC system namespace
    And I am logged in via XML-RPC configchannel as user "admin" and password "admin"
    Then "sle_client" should not be subscribed to channel "mixedchannel"
    And I logout from XML-RPC configchannel namespace

@sle_minion
  Scenario: Unsubscribe systems via XML-RPC for Salt Minion
    Given I am logged in via XML-RPC system as user "admin" and password "admin"
    When I unsubscribe "sle_minion" from configuration channel "mixedchannel"
    And I logout from XML-RPC system namespace
    And I am logged in via XML-RPC configchannel as user "admin" and password "admin"
    And "sle_minion" should not be subscribed to channel "mixedchannel"
    And I logout from XML-RPC configchannel namespace

@sle_client
  Scenario: Re-add Salt Minion via SSM
    Given I am authorized as "admin" with password "admin"
    When I am on the System Overview page
    And I follow "Clear"
    And I check the "sle_client" client
    And I am on System Set Manager Overview
    And I follow "config channel subscriptions" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Apply Subscriptions"
    And I click on "Confirm"
    Then I should see a "Configuration channel subscriptions changed for 1 system successfully." text

@sle_minion
  Scenario: Re-add Traditional Client via SSM
    Given I am authorized as "admin" with password "admin"
    When I am on the System Overview page
    And I follow "Clear"
    And I check the "sle_minion" client
    And I am on System Set Manager Overview
    And I follow "config channel subscriptions" in the content area
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Apply Subscriptions"
    And I click on "Confirm"
    Then I should see a "Configuration channel subscriptions changed for 1 system successfully." text

@sle_client
  Scenario: Cleanup: remove remaining Traditional Client from configuration channel
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Systems" in the content area
    And I check the "sle_client" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text

@sle_minion
  Scenario: Cleanup: remove remaining Salt Minion from configuration channel
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Systems" in the content area
    And I check the "sle_minion" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text

  Scenario: Cleanup: remove the mixed configuration channel
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Channels"
    And I follow "Mixed Channel"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"
    Then file "/srv/susemanager/salt/manager_org_1/mixedchannel/init.sls" should not exist on server

@sle_client
  Scenario: Cleanup: delete configuration files on remaining Traditional Client
    When I destroy "/etc/s-mgr" directory on "sle_client"

@sle_minion
  Scenario: Cleanup: delete configuration files on remaining Salt Minion
    When I destroy "/etc/s-mgr" directory on "sle_minion"

  Scenario: Cleanup: remove remaining systems from SSM after tests of configuration channel on all clients
    When I am authorized as "admin" with password "admin"
    And I follow "Clear"
