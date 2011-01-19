# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

# features/system_configuration.feature
Feature: System configuration
  In Order to subscribe a system to a configuration channel
  As an admin user
  I want to go to the systems configuration page

  @failed
  Scenario: Accessing system configuration
    Given I am authorized
    When I follow "Systems"
    Then I should see a "System Overview" text
     And I should see this client as link

