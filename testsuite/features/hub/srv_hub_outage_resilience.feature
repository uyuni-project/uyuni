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
  #
  # Hub-to-peripheral channel sync here uses the real SLE-Product-SLES15-SP7-Pool vendor
  # channel (synced from SCC earlier in the run set), not a custom/cloned channel: ISS v3
  # sync of a cloned channel is a known-broken path (bugzilla.suse.com/show_bug.cgi?id=1272155),
  # see the commented-out assertions in srv_hub_channel_synchronization.feature. This feature
  # no longer installs a package while the hub is down -- that previously relied on the
  # andromeda-dummy test package, which only exists in the (now unused) Fake-RPM-SUSE-Channel.

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Prerequisite - register server2 as peripheral for outage resilience tests (B-05)
    When I add "server2" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "server2" in peripherals list

  Scenario: Prerequisite - sync a channel to server2 for outage resilience tests (B-05)
    When I configure hub to sync channel "SLE-Product-SLES15-SP7-Pool for x86_64" to "server2"
    When I initiate channel sync from peripheral "server2"
    Then I should see a "Successfully scheduled a channels synchronization." text
    And I wait until I see "Synchronization started" text
    And I wait at most 600 seconds until channel "sle-product-sles15-sp7-pool-x86_64" has been synced on "server2"
    Then channel "sle-product-sles15-sp7-pool-x86_64" should exist on "server2"

  Scenario: Log in as admin user on server2 before hub outage (B-05)
    Given I am authorized for the "Admin" section on "server2"

  Scenario: Stop hub server services to simulate hub outage (B-05)
    When I stop hub server services on "server"

  Scenario: Channel sync from hub fails with clear error while hub is down (B-05)
    Then I should see a channel sync failure error on "server2"

  Scenario: Restart hub server services to restore normal operation (B-05)
    When I start hub server services on "server"
    Then the Hub XMLRPC API should be running on "server"

  Scenario: Channel sync from hub recovers after hub restart (B-05)
    When I initiate channel sync from peripheral "server2"
    Then I should see a "Successfully scheduled a channels synchronization." text
    Then I should see a "Background" text

  Scenario: Cleanup - remove synced channels from server2 (B-05)
    When I remove synced channels from "server2"
    And I wait until I see "Channel configuration updated" text
    Then I should see a "Updated" text

  Scenario: Cleanup - deregister server2 from hub (B-05)
    When I unregister "server2" from hub
    Then I should not see the name of "server2"

  Scenario: Cleanup - delete sle_minion from server2 (B-05)
    When I delete "sle_minion" system using the api
    And I perform a full salt minion cleanup on "sle_minion"
    Then "sle_minion" should not be registered
