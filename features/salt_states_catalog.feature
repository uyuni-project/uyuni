# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check the Salt package state UI
  In Order to test salt states catalog
  As the testing user

  Scenario: Verify the states catalog UI
    Given I am authorized as "testing" with password "testing"
    Then I follow "Salt"
    Then I follow "States Catalog"
    Then I should see a "States Catalog" text
    Then I should see a "Items 0 - 0 of 0" text

  Scenario: I add a state through the UI
    Given I am authorized as "testing" with password "testing"
    Then I follow "Salt"
    Then I follow "States Catalog"
    Then I should see a "States Catalog" text
    And I follow "Create state"
    Then I should see a "Create State" text
    Then I should see a "Name*:" text
    Then I should see a "Content*:" text
    And I enter "teststate" in the css "input[name='name']"
    And I enter the salt state
      """
      touch /root/foobar:
        cmd.run:
          - creates: /root/foobar
      """
    And I click on the css "button#save-btn"
    Then I should see a "State 'teststate' saved" text

  Scenario: I add a apply a state via the UI
    Given I am on the Systems overview page of this client
    And I follow "States"
    Then I follow "Custom"
    Then I should see a "Custom States" text
    And I click on the css "button#search-states"
    Then I should see a "teststate" text
    And I select the state "teststate"
    Then I should see a "1 Changes" text
    Then I click on the css "button#save-btn"
    And I click on the css "button#apply-btn"
    Then I wait for the file "/root/foobar"
