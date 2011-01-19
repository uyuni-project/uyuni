# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Delete a configuration channel
  In Configuration
  As the admin user
  I want to delete a configuration channel

  Scenario: Delete configuration channel labeled "testchannel"
    Given I am testing configuration
    When I follow "Configuration Channels"
     And I follow "Test Channel"
     And I follow "delete channel"
     And I click on "Delete Config Channel"
    Then I should see a "Centrally Managed Configuration Channels" text
     And I should see a "create new config channel" link

