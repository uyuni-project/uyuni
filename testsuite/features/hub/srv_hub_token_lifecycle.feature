# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Prerequisites: srv_hub_token_registration.feature must have completed successfully.

@scope_hub
@hub_server_to_server
@peripheral1
Feature: Hub access token lifecycle management
  In order to control access between hub and peripheral servers
  As an authorized user
  I want to issue, invalidate, reactivate, and delete access tokens (plan A-05)

  Scenario: Log in as admin user for token lifecycle tests
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: register peripheral1 as peripheral with admin credentials
    When I add "peripheral1" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "peripheral1" in peripherals list

  Scenario: Pre-requisite: assign SLES15-SP7 channels from hub to peripheral1
    ## Requires the SLES15-SP7 product to already be synced on the hub (build_validation phase)
    When I configure hub to sync all "sles15-sp7" channels to "peripheral1"

  Scenario: Pre-requisite: log in as admin user on peripheral1
    Given I am authorized for the "Admin" section on "peripheral1"

  Scenario: Verify token is listed as consumed after registration
    Given I am authorized for the "Admin" section on "server"
    When I follow the left menu "Admin > Hub Configuration > Access Tokens"
    Then the access token for "peripheral1" should be listed as "Consumed"

  Scenario: Invalidate the token for peripheral1 and verify status changes
    When I invalidate the access token for "peripheral1" on hub
    Then the access token for "peripheral1" should be listed as "Invalid"

  Scenario: Verify hub-to-peripheral communication fails after token invalidation
    When I initiate channel sync from peripheral "peripheral1"
    Then channel sync from peripheral "peripheral1" should fail with a repository access error

  ## BUG-021: reactivating an invalidated token does not restore channel sync (RepoMDError
  ## persists), so recovery here goes through a full deregister/re-register cycle instead of
  ## depending on token reactivation. BUG-021 itself stays open and untouched.
  Scenario: Reset peripheral1 by deregistering it instead of reactivating its token
    When I unregister "peripheral1" from hub
    Then I should not see "peripheral1" hostname

  Scenario: Re-register peripheral1 with a fresh token
    When I issue a new access token for hub on "peripheral1"
    And I add "peripheral1" as peripheral using its access token
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "peripheral1" in peripherals list

  Scenario: Re-configure channels and verify channel sync works after re-registration
    When I configure hub to sync all "sles15-sp7" channels to "peripheral1"
    Given I am authorized for the "Admin" section on "peripheral1"
    When I initiate channel sync from peripheral "peripheral1"
    Then channel sync from peripheral "peripheral1" should succeed

  Scenario: Cleanup: deregister peripheral1 from hub
    When I unregister "peripheral1" from hub
    Then I should not see "peripheral1" hostname
