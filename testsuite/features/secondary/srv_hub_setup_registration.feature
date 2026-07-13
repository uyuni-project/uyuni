# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@server2
Feature: Hub setup and peripheral registration via administrator credentials
  In order to connect peripherals to a hub server
  As an authorized user
  I want to register a peripheral using administrator username and password (plan A-01/A-02)

  Scenario: Log in as admin user on hub
    Given I am authorized for the "Admin" section

  Scenario: Verify Hub XMLRPC API is accessible on hub (A-01)
    When I wait until hub.conf exists in the hub xmlrpc container on "server"
    Then the Hub XMLRPC API should be running on "server"

  Scenario: Navigate to Peripherals Configuration on hub (A-02)
    When I follow the left menu "Admin > Hub Configuration > Peripherals Configuration"
    Then I should see a "Add Peripheral" text

  Scenario: Extend hub deployment check - verify container and config (A-01)
    Then the uyuni-hub-xmlrpc-0 container should be running on "server"
    And the hub.conf on "server" should contain the required configuration keys

  Scenario: Negative - registration with wrong password is rejected (A-02)
    When I attempt to register "server2" as peripheral with wrong password
    Then I should see a registration failure error
    And I should not see "server2" in peripherals list

  Scenario: Negative - registration with non-admin credentials is rejected (A-02)
    When I create a non-admin user "hub-test-readonly" with password "TestPass123!" on "server2"
    And I attempt to register "server2" as peripheral with username "hub-test-readonly" and password "TestPass123!"
    Then I should see a registration failure error
    And I should not see "server2" in peripherals list

  Scenario: Cleanup - delete non-admin test user from server2 (A-02)
    When I delete non-admin user "hub-test-readonly" from "server2"

  Scenario: Register server2 as a peripheral with administrator credentials (A-02)
    When I add "server2" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "server2" in peripherals list

  Scenario: Verify peripheral appears in system list as a Foreign system (A-02)
    When I follow the left menu "Systems > System List > All"
    Then I should see "server2" in the system list with "Foreign" system type

  Scenario: Verify access token was created on hub after registration (A-02)
    When I follow the left menu "Admin > Hub Configuration > Access Tokens"
    Then I should see a "Consumed" text
    And I should see the name of "server2"

  Scenario: Verify Hub Details are populated on the peripheral (A-02)
    Given I am authorized for the "Admin" section on "server2"
    When I follow the left menu "Admin > Hub Configuration > Hub Details"
    Then the Hub Details page on "server2" should show the hub FQDN

  Scenario: Verify Setup Wizard on peripheral shows managed-by-hub notice (A-02)
    Given I am authorized for the "Admin" section on "server2"
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I see "This server is configured as a Peripheral server in a Hub configuration" text, refreshing the page
    Then I should see a "This server is configured as a Peripheral server in a Hub configuration" text

  Scenario: Negative - re-registering an already-registered peripheral is rejected (A-02)
    When I add "server2" as peripheral using administrator credentials
    Then I should see a duplicate peripheral registration error
    And I should see "server2" in peripherals list

  Scenario: Cleanup - deregister server2 from hub
    When I unregister "server2" from hub
    Then I should not see the name of "server2"
