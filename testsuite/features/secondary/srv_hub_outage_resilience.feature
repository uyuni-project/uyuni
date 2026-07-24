# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_full_topology
@hub_outage
@server2
@sle_minion
Feature: Hub outage resilience for peripherals and their minions
  In order to confirm high availability of peripheral operations
  As an authorized user
  I want to verify that peripheral and minion operations continue while the hub is unavailable (plan B-05)

  # This feature must run LAST in hub_full_topology.yml.
  # An After hook restores hub services if the hub was left stopped mid-scenario.
  #
  # sle_minion (referenced below) is bootstrapped by srv_hub_minion_on_peripheral.feature
  # earlier in the run set. That feature deliberately does not delete it, since this
  # feature is the last consumer -- final sle_minion cleanup happens here instead.

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Prerequisite - register server2 as peripheral for outage resilience tests (B-05)
    When I add "server2" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "server2" in peripherals list

  Scenario: Prerequisite - clone a channel on hub for outage sync testing (B-05)
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "Fake-RPM-SUSE-Channel" as the origin channel
    And I choose "current"
    And I click on "Clone Channel"
    And I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    And I click on "Clone Channel"
    Then I should see a "Clone of Fake-RPM-SUSE-Channel" text

  Scenario: Prerequisite - sync the cloned channel to server2 for outage resilience tests (B-05)
    When I configure hub to sync channel "clone-fake-rpm-suse-channel" to "server2"
    And I trigger channel sync from hub to "server2"
    And I wait until I see "Synchronization started" text
    And I wait at most 600 seconds until channel "clone-fake-rpm-suse-channel" has been synced on "server2"
    Then channel "clone-fake-rpm-suse-channel" should exist on "server2"

  Scenario: Log in as admin user on server2 before hub outage (B-05)
    Given I am authorized for the "Admin" section on "server2"

  Scenario: Stop hub server services to simulate hub outage (B-05)
    When I stop hub server services on "server"

  Scenario: Package install on sle_minion from already-mirrored channel succeeds while hub is down (B-05)
    Given I am authorized for the "Admin" section on "server2"
    And I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Install" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click the search button
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    And I wait until event "Package Install/Upgrade scheduled by admin" is completed

  Scenario: Channel sync from hub fails with clear error while hub is down (B-05)
    Then I should see a channel sync failure error on "server2"

  Scenario: Restart hub server services to restore normal operation (B-05)
    When I start hub server services on "server"
    Then the Hub XMLRPC API should be running on "server"

  Scenario: Channel sync from hub recovers after hub restart (B-05)
    When I trigger channel sync from hub to "server2"
    And I wait until I see "Synchronization started" text
    Then I should see a "Background" text

  Scenario: Cleanup - remove andromeda-dummy from sle_minion (B-05)
    Given I am authorized for the "Admin" section on "server2"
    And I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "andromeda-dummy" as the filtered package name
    And I click the search button
    And I check "andromeda-dummy" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    And I wait until event "Package Removal scheduled by admin" is completed

  Scenario: Cleanup - remove synced channels from server2 (B-05)
    When I remove synced channels from "server2"
    And I wait until I see "Channel configuration updated" text
    Then I should see a "Updated" text

  Scenario: Cleanup - delete cloned channel from hub (B-05)
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone of Fake-RPM-SUSE-Channel"
    And I follow "Delete Channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Fake-RPM-SUSE-Channel" text

  Scenario: Cleanup - deregister server2 from hub (B-05)
    When I unregister "server2" from hub
    Then I should not see the name of "server2"

  Scenario: Cleanup - delete sle_minion from server2 (B-05)
    When I delete "sle_minion" system using the api
    And I perform a full salt minion cleanup on "sle_minion"
    Then "sle_minion" should not be registered
