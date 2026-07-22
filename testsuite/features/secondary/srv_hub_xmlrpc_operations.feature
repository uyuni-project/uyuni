# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@server2
Feature: Hub XMLRPC API operations
  In order to manage multiple peripheral servers from a hub
  As an authorized user
  I want to use the hub XMLRPC API namespaces (plan A-07/A-08)

  # The hub.multicast.system.list_systems scenarios for A-08 live in
  # srv_hub_minion_on_peripheral.feature instead of here: verifying multicast returns
  # real system data needs a minion bootstrapped under server2, which only exists in
  # that feature's @hub_full_topology window. This feature stays in @hub_server_to_server
  # and only covers the API calls that just need server2 registered (no minion).

  # This runs first, before any hub XMLRPC session is established: it restarts
  # uyuni-hub-xmlrpc-0 to pick up server2's certificate in its trust store, and that
  # container appears to hold sessions in-memory -- restarting it after a session is
  # already established invalidates that session for subsequent calls (unicast.system.list_systems
  # hits server2's own /rpc/api directly from this container, unlike multicast).
  Scenario: Trust server2's certificate in the hub xmlrpc container before unicast test (A-08)
    When I trust the hub CA in the hub xmlrpc container on "server"

  Scenario: Connect to Hub XMLRPC API with auto-connect mode (A-07)
    Given I am connected to the hub XMLRPC API

  Scenario: List peripheral server IDs via Hub API (A-08)
    When I call hub.listServerIds via XMLRPC
    Then I should see "server2" in the server IDs list

  Scenario: Login to Hub API with manual mode returns a session key (A-07)
    When I login to hub XMLRPC API with standard mode
    Then the hub standard session key should be non-empty

  Scenario: Login to Hub API with auth relay mode returns a session key (A-07)
    When I login to hub XMLRPC API with auth relay mode
    Then the hub relay session key should be non-empty

  Scenario: Reconnect to Hub API with auto-connect mode for namespace tests (A-08)
    Given I am connected to the hub XMLRPC API

  Scenario: List server IDs before unicast and pass-through tests (A-08)
    When I call hub.listServerIds via XMLRPC
    Then I should see "server2" in the server IDs list

  Scenario: Execute unicast system list for one peripheral via Hub API (A-08)
    When I call unicast.system.list_systems for "server2" via XMLRPC
    Then unicast response should contain systems from "server2"

  Scenario: Call system.list_systems on hub's own XMLRPC endpoint (pass-through) (A-08)
    When I call system.list_systems on hub's own XMLRPC endpoint
    Then hub's own system list should not be empty

  Scenario: Logout from Hub XMLRPC API
    When I logout from hub XMLRPC API
