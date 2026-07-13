# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@server2
Feature: Hub setup and peripheral registration via administrator credentials for server2
  In order to connect peripherals to a hub server
  As an authorized user
  I want to register a peripheral using administrator username and password

  Scenario: Generate hub-signed SSL certificates for the peripheral
    When I generate hub-signed SSL certificates for "server2" on "server"

  Scenario: Transfer the hub-signed SSL certificates to the peripheral
    When I copy the hub-signed SSL certificates for "server2" from "server"
    And I trust the hub CA certificate on "server2"

  Scenario: Install the peripheral server with hub-signed certificates
    When I install the peripheral server on "server2" using the hub-signed certificates
    And I wait until the server on "server2" is ready

  Scenario: Apply testsuite configuration to the peripheral server
    When I apply the testsuite configuration on the peripheral server "server2"
    And I wait until the server on "server2" is ready

  Scenario: Log in as admin user on hub
    Given I am authorized for the "Admin" section

  Scenario: Verify Hub XMLRPC API is accessible on hub
    When I wait until hub.conf exists in the hub xmlrpc container on "server"
    Then the Hub XMLRPC API should be running on "server"

  Scenario: Navigate to Peripherals Configuration on hub
    When I follow the left menu "Admin > Hub Configuration > Peripherals Configuration"
    Then I should see a "Add Peripheral" text

  Scenario: Extend hub deployment check - verify container and config (A-01)
    Then the uyuni-hub-xmlrpc-0 container should be running on "server"
    And the hub.conf on "server" should contain the required configuration keys

  Scenario: Register server2 as a peripheral with administrator credentials
    When I add "server2" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "server2" in peripherals list

  Scenario: Verify peripheral appears in system list as a Foreign system
    When I follow the left menu "Systems > System List > All"
    Then I should see "server2" in the system list with "Foreign" system type

  Scenario: Verify access token was created on hub after registration
    When I follow the left menu "Admin > Hub Configuration > Access Tokens"
    Then I should see a "Consumed" text
    And I should see the name of "server2"

  Scenario: Verify Hub Details are populated on the peripheral
    Given I am authorized for the "Admin" section on "server2"
    When I follow the left menu "Admin > Hub Configuration > Hub Details"
    Then the Hub Details page on "server2" should show the hub FQDN

  Scenario: Verify Setup Wizard on peripheral shows managed-by-hub notice
    Given I am authorized for the "Admin" section on "server2"
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I see "This server is configured as a Peripheral server in a Hub configuration" text, refreshing the page
    Then I should see a "This server is configured as a Peripheral server in a Hub configuration" text

  Scenario: Change page size to 100 per page in admin user
    When I follow the left menu "Home > My Preferences"
    And I select "100" from "pagesize"
    And I click on "Save Preferences"
    Then I should see a "Preferences modified" text