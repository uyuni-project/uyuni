# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@server2
Feature: Hub ISSv2 CLI channel export and import
  In order to distribute content offline between hub and peripheral servers
  As an authorized user
  I want to export a channel from the hub and import it on a peripheral (plan A-09)

  Background:
    Given I am authorized for the "Admin" section

  Scenario: Prerequisite - org names match on hub and server2 for ISS v2 import (A-09)
    Given the default organization name on hub and "server2" match

  Scenario: Prerequisite - inter-server-sync is installed on hub and server2 (A-09)
    Given "inter-server-sync" is installed on both hub and "server2"
    And hub and "server2" have the same MLM version

  Scenario: Prerequisite - create a custom channel on hub for ISS v2 export (A-09)
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "ISS v2 Test Channel" as "Channel Name"
    And I enter "iss-v2-test-channel" as "Channel Label"
    And I select "x86_64" from "Architecture"
    And I click on "Create Channel"
    Then I should see a "Channel ISS v2 Test Channel created" text

  Scenario: Export the test channel from hub using ISS v2 (A-09)
    When I export channel "iss-v2-test-channel" with ISS v2 to "/var/spacewalk/iss-export-hub-test" on hub
    Then "/var/spacewalk/iss-export-hub-test" folder on server is ISS v2 export directory

  Scenario: Transfer ISS v2 export from hub to server2 (A-09)
    When I transfer ISS v2 export from hub to "server2"
    Then I should not see a "Transfer failed" text

  Scenario: Import ISS v2 data on server2 (A-09)
    When I import ISS v2 data from "/var/spacewalk/iss-export-hub-test" on "server2"
    Then channel "iss-v2-test-channel" should be listed in API on "server2"

  Scenario: Cleanup - delete ISS v2 test channel from hub
    When I follow the left menu "Software > Manage > Channels"
    And I follow "ISS v2 Test Channel"
    And I follow "Delete Channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "ISS v2 Test Channel" text

  Scenario: Cleanup - remove ISS v2 export directory from hub
    When I run "rm -rf /var/spacewalk/iss-export-hub-test" on "server"
    Then file "/var/spacewalk/iss-export-hub-test" should not exist on server
