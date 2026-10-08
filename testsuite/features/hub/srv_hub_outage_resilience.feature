# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Prerequisites: srv_hub_minion_on_peripheral.feature must have completed successfully
# (a managed minion on the peripheral must exist to test hub-outage behavior).

@scope_hub
@hub_full_topology
@hub_outage
@peripheral1
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
  #
  # Hub-to-peripheral channel sync here uses the real SLE-Product-SLES15-SP7-Pool vendor
  # channel (synced from SCC earlier in the run set), not a custom/cloned channel: ISS v3
  # sync of a cloned channel is a known-broken path (bugzilla.suse.com/show_bug.cgi?id=1272155),
  # see the commented-out assertions in srv_hub_channel_synchronization.feature. This feature
  # no longer installs a package while the hub is down -- that previously relied on the
  # andromeda-dummy test package, which only exists in the (now unused) Fake-RPM-SUSE-Channel.

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: register peripheral1 as peripheral for outage resilience tests
    When I unregister "peripheral1" from hub if registered
    And I add "peripheral1" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "peripheral1" in peripherals list

  Scenario: Pre-requisite: sync a channel to peripheral1 for outage resilience tests
    When I configure hub to sync channel "SLE-Product-SLES15-SP7-Pool for x86_64" to "peripheral1"
    When I initiate channel sync from peripheral "peripheral1"
    Then I should see a "Successfully scheduled a channels synchronization." text
    And I wait until I see "Synchronization started" text
    And I wait at most 600 seconds until channel "sle-product-sles15-sp7-pool-x86_64" has been synced on "peripheral1"
    Then channel "sle-product-sles15-sp7-pool-x86_64" should exist on "peripheral1"

  Scenario: Log in as admin user on peripheral1 before hub outage
    Given I am authorized for the "Admin" section on "peripheral1"

  Scenario: Stop hub server services to simulate hub outage
    When I stop the uyuni server
    Then I check the uyuni server has stopped

  Scenario: Channel sync from hub fails with clear error while hub is down
    Then I should see a channel sync failure error on "peripheral1"

  Scenario: Restart hub server services to restore normal operation
    When I start the uyuni server
    And I check the uyuni server has started
    Then the Hub XMLRPC API should be running on "hub"

  Scenario: Channel sync from hub recovers after hub restart
    When I initiate channel sync from peripheral "peripheral1"
    Then I should see a "Successfully scheduled a channels synchronization." text
    Then I should see a "Background" text

  Scenario: Cleanup: remove synced channels from peripheral1
    When I remove synced channels from "peripheral1"
    And I wait until I see "Channel configuration updated" text
    Then I should see a "Updated" text

  Scenario: Cleanup: deregister peripheral1 from hub
    When I unregister "peripheral1" from hub
    Then I should not see "peripheral1" hostname

  Scenario: Cleanup: delete sle_minion from peripheral1
    When I delete "sle_minion" system using the api from "peripheral1"
    And I perform a full salt minion cleanup on "sle_minion"
    Then "sle_minion" should not be registered
