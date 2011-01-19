# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Remove system from configuration channel

  Scenario: Remove system from configuration channel
    Given I am testing configuration
    When I follow "Configuration Channels" in the left menu
     And I follow "Test Channel"
     And I follow "Systems" in class "content-nav"
     And I check this client
     And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text
