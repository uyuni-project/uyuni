# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in:
# If the hub-peripheral registration fails:
# - features/hub/srv_hub_channel_synchronization.feature
# - features/hub/srv_hub_token_registration.feature
# - features/hub/srv_hub_token_lifecycle.feature
# - features/hub/srv_hub_xmlrpc_operations.feature
# - features/hub/srv_hub_reporting.feature
# - features/hub/srv_hub_grafana_setup.feature
# - features/hub/srv_hub_issv2_export_import.feature
# - features/hub/srv_hub_peripheral_as_minion.feature
# - features/hub/srv_hub_minion_on_peripheral.feature
# - features/hub/srv_hub_outage_resilience.feature
# - features/hub/srv_hub_verification_cleanup.feature

@scope_hub
@hub_server_to_server
@peripheral1
Feature: Hub setup and peripheral registration via administrator credentials
  In order to connect peripherals to a hub server
  As an authorized user
  I want to register a peripheral using administrator username and password (plan A-01/A-02)

  Scenario: Generate hub-signed SSL certificates for the peripheral (A-01)
    When I generate hub-signed SSL certificates for "peripheral1" on "hub"

  Scenario: Transfer the hub-signed SSL certificates to the peripheral (A-01)
    When I copy the hub-signed SSL certificates for "peripheral1" from "hub"
    And I trust the hub CA certificate on "peripheral1"

  Scenario: Install the peripheral server with hub-signed certificates (A-01)
    When I install the peripheral server on "peripheral1" using the hub-signed certificates
    And I wait until the server on "peripheral1" is ready

  Scenario: Apply testsuite configuration to the peripheral server (A-01)
    When I apply the testsuite configuration on the peripheral server "peripheral1"
    And I wait until the server on "peripheral1" is ready

  Scenario: Log in as admin user on hub
    Given I am authorized for the "Admin" section

  Scenario: Verify Hub XMLRPC API is accessible on hub (A-01)
    When I wait until hub.conf exists in the hub xmlrpc container on "hub"
    Then the Hub XMLRPC API should be running on "hub"

  Scenario: Navigate to Peripherals Configuration on hub (A-02)
    When I follow the left menu "Admin > Hub Configuration > Peripherals Configuration"
    Then I should see a "Add Peripheral" text

  Scenario: Extend hub deployment check - verify container and config (A-01)
    Then the uyuni-hub-xmlrpc-0 container should be running on "hub"
    And the hub.conf on "hub" should contain the required configuration keys

  Scenario: Negative - registration with wrong password is rejected (A-02)
    When I attempt to register "peripheral1" as peripheral with wrong password
    Then I should see a registration failure error
    And I should not see "peripheral1" in peripherals list

  Scenario: Negative - registration with non-admin credentials is rejected (A-02)
    When I create a non-admin user "hub-test-readonly" with password "TestPass123!" on "peripheral1"
    And I attempt to register "peripheral1" as peripheral with username "hub-test-readonly" and password "TestPass123!"
    Then I should see a registration failure error
    And I should not see "peripheral1" in peripherals list

  Scenario: Cleanup - delete non-admin test user from peripheral1 (A-02)
    When I delete non-admin user "hub-test-readonly" from "peripheral1"

  Scenario: Register peripheral1 as a peripheral with administrator credentials (A-02)
    When I add "peripheral1" as peripheral using administrator credentials
    And I wait until I see "is currently registered as peripheral of this hub" text
    Then I should see "peripheral1" in peripherals list

  Scenario: Verify peripheral appears in system list as a Foreign system (A-02)
    When I follow the left menu "Systems > System List > All"
    Then I should see "peripheral1" in the system list with "Foreign" system type

  Scenario: Verify access token was created on hub after registration (A-02)
    When I follow the left menu "Admin > Hub Configuration > Access Tokens"
    Then I should see a "Consumed" text
    And I should see the name of "peripheral1"

  Scenario: Verify Hub Details are populated on the peripheral (A-02)
    Given I am authorized for the "Admin" section on "peripheral1"
    When I follow the left menu "Admin > Hub Configuration > Hub Details"
    Then the Hub Details page on "peripheral1" should show the hub FQDN

  Scenario: Verify Setup Wizard on peripheral shows managed-by-hub notice (A-02)
    Given I am authorized for the "Admin" section on "peripheral1"
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I see "This server is configured as a Peripheral server in a Hub configuration" text, refreshing the page
    Then I should see a "This server is configured as a Peripheral server in a Hub configuration" text

  Scenario: Negative - re-registering an already-registered peripheral is rejected (A-02)
    When I add "peripheral1" as peripheral using administrator credentials
    Then I should see a duplicate peripheral registration error
    And I should see "peripheral1" in peripherals list

  Scenario: Cleanup - deregister peripheral1 from hub
    When I unregister "peripheral1" from hub
    Then I should not see the name of "peripheral1"
