# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@server2
Feature: Hub peripheral registration using access tokens
  In order to register peripherals securely without sharing administrator credentials
  As an authorized user
  I want to register a peripheral using a pre-issued access token (plan A-03/A-04)


  Scenario: Issue a new access token on server2 for the hub (A-03)
    Given I am authorized for the "Admin" section on "server2"
    When I issue a new access token for hub on "server2"
    Then I should see a "New token successfully issued" text

  Scenario: Register server2 as peripheral using its access token - same CA (A-03)
    Given I am authorized for the "Admin" section
    When I add "server2" as peripheral using its access token
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "server2" in peripherals list

  Scenario: Verify token shows as consumed on hub after registration (A-03)
    When I follow the left menu "Admin > Hub Configuration > Access Tokens"
    Then the access token for "server2" should be listed as "Consumed"

  Scenario: Negative - reusing an already consumed token is rejected (A-03)
    When I add "server2" as peripheral using its access token
    ## Disable because of https://bugzilla.suse.com/show_bug.cgi?id=1271430
    #Then I should see a "token already consumed" text
    Then I should see a registration failure error

  Scenario: Deregister server2 to restore clean state
    When I unregister "server2" from hub
    Then I should not see the name of "server2"

  Scenario: Negative - token issued for wrong FQDN is rejected (A-03)
    When I issue a new access token for wrong FQDN on "server2"
    And I add "server2" as peripheral using its wrong-FQDN token
    Then I should see a token rejection error
    And I should not see "server2" in peripherals list

  Scenario: Issue a fresh token on server2 for the invalidation test (A-03)
    Given I am authorized for the "Admin" section on "server2"
    When I issue a new access token for hub on "server2"
    Then I should see a "New token successfully issued" text

  Scenario: Negative - invalidated token is rejected (A-03)
    When I invalidate the token I just issued on "server2"
    And I add "server2" as peripheral using its invalidated token
    Then I should see a token rejection error
    And I should not see "server2" in peripherals list

  @server3
  Scenario: Log in as admin user on server3 for cross-CA registration tests (A-04)
    Given I am authorized for the "Admin" section on "server3"

  @server3
  Scenario: Negative - cross-CA registration without root CA is rejected (A-04)
    When I issue a new access token for hub on "server3"
    And I add "server3" as peripheral using its access token without root CA
    Then I should see a token rejection error
    And I should not see "server3" in peripherals list
    When I invalidate the token I just issued on "server3"

  @server3
  Scenario: Fetch root CA from server3 (different CA) for cross-CA registration (A-04)
    Given I am authorized for the "Admin" section on "server3"
    When I fetch root CA certificate from "server3"
    And I issue a new access token for hub on "server3"
    Then I should see a "New token successfully issued" text

  @server3
  Scenario: Register server3 as peripheral using token and pasted root CA (A-04)
    When I add "server3" as peripheral using its access token and pasted root CA
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "server3" in peripherals list

  @server3
  Scenario: Verify cross-CA peripheral connection status is active (A-04)
    When I follow the left menu "Admin > Hub Configuration > Peripherals Configuration"
    Then I should see "server3" in peripherals list

  @server3
  Scenario: Deregister server3 to reset state before upload CA test (A-04)
    When I unregister "server3" from hub
    Then I should not see the name of "server3"

  @server3
  Scenario: Issue a second access token on server3 for upload CA test (A-04)
    Given I am authorized for the "Admin" section on "server3"
    When I issue a new access token for hub on "server3"
    Then I should see a "New token successfully issued" text

  @server3
  Scenario: Register server3 as peripheral using token and uploaded CA file (A-04)
    When I add "server3" as peripheral using its access token and uploaded CA file
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "server3" in peripherals list

  @server3
  Scenario: Verify connection is active after file-upload CA registration (A-04)
    When I follow the left menu "Admin > Hub Configuration > Peripherals Configuration"
    Then I should see "server3" in peripherals list

  @server3
  Scenario: Cleanup - deregister server3 from hub
    When I unregister "server3" from hub
    Then I should not see the name of "server3"
