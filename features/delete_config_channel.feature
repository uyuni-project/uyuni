# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Delete a configuration channel
  In Configuration
  As the admin user
  I want to delete a configuration channel

  Scenario: Delete configuration channel labeled "testchannel"
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels"
    And I follow "Test Channel"
    And I follow "delete channel"
    And I click on "Delete Config Channel"
    Then I should see a "Centrally Managed Configuration Channels" text
    And I should see a "Create Config Channel" link
