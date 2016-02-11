# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check client registration
  In Order to test salt package states.
  As the testing

  Scenario: Subscribe to base channel
    Given I am on the Systems overview page of this client
    When I follow "Software" in the content area
    Then I follow "Software Channels" in the content area
    Then I select "SLES11-SP3-Updates x86_64 Channel" from "new_base_channel_id"
    Then I click on "Confirm"
    Then I click on "Modify Base Software Channel"
    Then I should see a "System's Base Channel has been updated." text
    
  Scenario: Test package states through the UI
    Given I am on the Systems overview page of this client
    Then I follow "States" in the content area
    Then I should see a "Package States" text
    And I list packages with "dummy" 
    Then I should see a "milkyway-dummy" text
    And I change the state of "milkyway-dummy" to "Installed" and "Any"
    Then I should see a "1 Changes" text
    Then I click undo for "milkyway-dummy"    
    Then I should see a "No Changes" text
