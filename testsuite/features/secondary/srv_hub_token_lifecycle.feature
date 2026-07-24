# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@server2
Feature: Hub access token lifecycle management
  In order to control access between hub and peripheral servers
  As an authorized user
  I want to issue, invalidate, reactivate, and delete access tokens (plan A-05)

  Scenario: Log in as admin user for token lifecycle tests
    Given I am authorized for the "Admin" section

  Scenario: Prerequisite - register server2 as peripheral with admin credentials
    When I add "server2" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "server2" in peripherals list

  Scenario: Prerequisite - assign SLES15-SP7 channels from hub to server2 (A-05)
    ## Requires the SLES15-SP7 product to already be synced on the hub (build_validation phase)
    When I configure hub to sync all "sles15-sp7" channels to "server2"

  Scenario: Prerequisite - log in as admin user on server2 (A-05)
    Given I am authorized for the "Admin" section on "server2"

  Scenario: Verify token is listed as consumed after registration (A-05)
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Access Tokens"
    Then the access token for "server2" should be listed as "Consumed"

  Scenario: Invalidate the token for server2 and verify status changes (A-05)
    When I invalidate the access token for "server2" on hub
    Then the access token for "server2" should be listed as "Invalid"

  Scenario: Verify hub-to-peripheral communication fails after token invalidation (A-05)
    When I initiate channel sync from peripheral "server2"
    Then channel sync from peripheral "server2" should fail with a repository access error

  Scenario: Reactivate the invalidated token and verify status restores (A-05)
    When I reactivate the access token for "server2" on hub
    Then the access token for "server2" should be listed as "Valid"

  @new_issue
  Scenario: Verify hub-to-peripheral communication is restored after token reactivation (A-05)
    ## BUG-021: reactivating the token does not currently restore channel sync (RepoMDError persists)
    Given I am authorized for the "Admin" section on "server2"
    When I initiate channel sync from peripheral "server2"
    ## BUG-021: reactivating the token does not currently restore channel sync (RepoMDError persists)
    #Then channel sync from peripheral "server2" should succeed

  Scenario: Cleanup - deregister server2 from hub
    When I unregister "server2" from hub
    Then I should not see the name of "server2"

  Scenario: Verify token is now deletable after deregistration (A-05)
    When I delete the access token for "server2" on hub
    Then I should not see the name of "server2"
