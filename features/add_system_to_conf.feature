# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Subscribe system to configuration channel

  Scenario: Subscribe system to configuration channel
    Given I am testing configuration
    When I follow "Configuration Channels" in the left menu
     And I follow "Test Channel"
     And I follow "Systems" in class "content-nav"
     And I follow "Target Systems"
     And I check this client
     And I click on "Subscribe systems"
    Then I should see a "Successfully subscribed 1 system(s)." text
