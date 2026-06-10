# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Hub setup and peripheral registration
  Register a peripheral server to the hub using auto-connect mode
  and verify the connection is established

  Scenario: Log in as admin user
    Given I am authorized

  Scenario: Verify Hub XMLRPC API is accessible
    When I wait until file "/etc/hub/hub.conf" exists on "server"
    Then the Hub XMLRPC API should be running on "server"

  Scenario: Add peripheral server via UI with auto-connect
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Peripherals Configuration"
    And I click on "Add peripheral"
    And I wait until I see "Peripheral FQDN" text
    Then I should see a "Registration Mode" text

  Scenario: Register peripheral with admin credentials
    When I add peripheral "peripheral_server" with credentials to hub via UI
    And I wait until I see "Peripheral registered successfully" text
    Then "peripheral_server" should appear in peripherals list

  Scenario: Verify peripheral connection status
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Peripherals Configuration"
    Then connection status for "peripheral_server" should be "Connected"

  Scenario: Verify access token was created
    When I follow the left menu "Admin > Hub Configuration"
    And I follow "Access Tokens"
    Then I should see a "Consumed" text
    And I should see the name of "peripheral_server"
