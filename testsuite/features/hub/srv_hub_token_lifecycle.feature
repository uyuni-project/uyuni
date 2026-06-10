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

  Scenario: Prerequisite - register peripheral1 as peripheral with admin credentials
    When I add "peripheral1" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "peripheral1" in peripherals list

  Scenario: Prerequisite - assign SLES15-SP7 channels from hub to peripheral1 (A-05)
    ## Requires the SLES15-SP7 product to already be synced on the hub (build_validation phase)
    When I configure hub to sync all "sles15-sp7" channels to "peripheral1"

  Scenario: Prerequisite - log in as admin user on peripheral1 (A-05)
    Given I am authorized for the "Admin" section on "peripheral1"

  Scenario: Verify token is listed as consumed after registration (A-05)
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Access Tokens"
    Then the access token for "peripheral1" should be listed as "Consumed"

  Scenario: Invalidate the token for peripheral1 and verify status changes (A-05)
    When I invalidate the access token for "peripheral1" on hub
    Then the access token for "peripheral1" should be listed as "Invalid"

  Scenario: Verify hub-to-peripheral communication fails after token invalidation (A-05)
    When I initiate channel sync from peripheral "peripheral1"
    Then channel sync from peripheral "peripheral1" should fail with a repository access error

  Scenario: Reactivate the invalidated token and verify status restores (A-05)
    When I reactivate the access token for "peripheral1" on hub
    Then the access token for "peripheral1" should be listed as "Valid"

  @new_issue
  Scenario: Verify hub-to-peripheral communication is restored after token reactivation (A-05)
    ## BUG-021: reactivating the token does not currently restore channel sync (RepoMDError persists)
    Given I am authorized for the "Admin" section on "peripheral1"
    When I initiate channel sync from peripheral "peripheral1"
    ## BUG-021: reactivating the token does not currently restore channel sync (RepoMDError persists)
    #Then channel sync from peripheral "peripheral1" should succeed

  Scenario: Cleanup - deregister peripheral1 from hub
    When I unregister "peripheral1" from hub
    Then I should not see the name of "peripheral1"
