# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Remove system from configuration channel

  Scenario: Remove system from configuration channel
    Given I am testing configuration
    When I follow "Configuration Channels" in the left menu
    And I follow "Test Channel"
    And I follow "Systems" in the content area
    And I check this client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text
