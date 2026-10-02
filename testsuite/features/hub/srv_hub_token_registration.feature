# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in:
# If token-based peripheral registration fails:
# - features/hub/srv_hub_token_lifecycle.feature

@scope_hub
@hub_server_to_server
@peripheral1
Feature: Hub peripheral registration using access tokens
  In order to register peripherals securely without sharing administrator credentials
  As an authorized user
  I want to register a peripheral using a pre-issued access token (plan A-03/A-04)


  Scenario: Issue a new access token on peripheral1 for the hub (A-03)
    Given I am authorized for the "Admin" section on "peripheral1"
    When I issue a new access token for hub on "peripheral1"
    Then I should see a "New token successfully issued" text

  Scenario: Register peripheral1 as peripheral using its access token - same CA (A-03)
    Given I am authorized for the "Admin" section
    When I add "peripheral1" as peripheral using its access token
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "peripheral1" in peripherals list

  Scenario: Verify token shows as consumed on hub after registration (A-03)
    When I follow the left menu "Admin > Hub Configuration > Access Tokens"
    Then the access token for "peripheral1" should be listed as "Consumed"

  Scenario: Negative - reusing an already consumed token is rejected (A-03)
    When I add "peripheral1" as peripheral using its access token
    ## Disable because of https://bugzilla.suse.com/show_bug.cgi?id=1271430
    #Then I should see a "token already consumed" text
    Then I should see a registration failure error

  Scenario: Deregister peripheral1 to restore clean state
    When I unregister "peripheral1" from hub
    Then I should not see the name of "peripheral1"

  Scenario: Negative - token issued for wrong FQDN is rejected (A-03)
    When I issue a new access token for wrong FQDN on "peripheral1"
    And I add "peripheral1" as peripheral using its wrong-FQDN token
    Then I should see a token rejection error
    And I should not see "peripheral1" in peripherals list

  Scenario: Issue a fresh token on peripheral1 for the invalidation test (A-03)
    Given I am authorized for the "Admin" section on "peripheral1"
    When I issue a new access token for hub on "peripheral1"
    Then I should see a "New token successfully issued" text

  Scenario: Negative - invalidated token is rejected (A-03)
    When I invalidate the token I just issued on "peripheral1"
    And I add "peripheral1" as peripheral using its invalidated token
    Then I should see a token rejection error
    And I should not see "peripheral1" in peripherals list

  @peripheral2
  Scenario: Install the peripheral server on peripheral2 with its own certificate (A-04)
    When I install the peripheral server on "peripheral2" with its own self-signed certificate
    And I wait until the server on "peripheral2" is ready

  @peripheral2
  Scenario: Log in as admin user on peripheral2 for cross-CA registration tests (A-04)
    Given I am authorized for the "Admin" section on "peripheral2"

  @peripheral2
  Scenario: Negative - cross-CA registration without root CA is rejected (A-04)
    When I issue a new access token for hub on "peripheral2"
    And I add "peripheral2" as peripheral using its access token without root CA
    Then I should see a token rejection error
    And I should not see "peripheral2" in peripherals list
    When I invalidate the token I just issued on "peripheral2"

  @peripheral2
  Scenario: Fetch root CA from peripheral2 (different CA) for cross-CA registration (A-04)
    Given I am authorized for the "Admin" section on "peripheral2"
    When I fetch root CA certificate from "peripheral2"
    And I issue a new access token for hub on "peripheral2"
    Then I should see a "New token successfully issued" text

  @peripheral2
  Scenario: Register peripheral2 as peripheral using token and pasted root CA (A-04)
    When I add "peripheral2" as peripheral using its access token and pasted root CA
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "peripheral2" in peripherals list

  @peripheral2
  Scenario: Verify cross-CA peripheral connection status is active (A-04)
    When I follow the left menu "Admin > Hub Configuration > Peripherals Configuration"
    Then I should see "peripheral2" in peripherals list

  @peripheral2
  Scenario: Deregister peripheral2 to reset state before upload CA test (A-04)
    When I unregister "peripheral2" from hub
    Then I should not see the name of "peripheral2"

  @peripheral2
  Scenario: Issue a second access token on peripheral2 for upload CA test (A-04)
    Given I am authorized for the "Admin" section on "peripheral2"
    When I issue a new access token for hub on "peripheral2"
    Then I should see a "New token successfully issued" text

  @peripheral2
  Scenario: Register peripheral2 as peripheral using token and uploaded CA file (A-04)
    When I add "peripheral2" as peripheral using its access token and uploaded CA file
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "peripheral2" in peripherals list

  @peripheral2
  Scenario: Verify connection is active after file-upload CA registration (A-04)
    When I follow the left menu "Admin > Hub Configuration > Peripherals Configuration"
    Then I should see "peripheral2" in peripherals list

  @peripheral2
  Scenario: Cleanup - deregister peripheral2 from hub
    When I unregister "peripheral2" from hub
    Then I should not see the name of "peripheral2"
