# Copyright (c) 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_visualization
@scope_onboarding
Feature: Custom system info key-value pairs

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create a new key
    When I follow the left menu "Systems > Custom System Info"
    And I follow "Create Key"
    And I should see a "Create Custom Info Key" text
    And I enter "key-label" as "label"
    And I enter "key-desc" as "description"
    And I click on "Create Key"
    Then I should see a "Successfully added 1 custom key." text

  Scenario: Add a value to a system
    When I am on the System Overview page
    And I follow this "sle_client" link
    And I follow "Custom Info"
    And I follow "Create Value"
    And I follow "key-label"
    And I enter "key-value" as "value"
    And I click on "Update Key"
    Then I should see a "key-label" text
    And I should see a "key-value" link

  Scenario: Edit the value
    When I am on the System Overview page
    And I follow this "sle_client" link
    And I follow "Custom Info"
    And I follow "key-value"
    And I should see a "Edit Custom Value" text
    And I enter "key-value-edited" as "value"
    And I click on "Update Key"
    Then I should see a "key-label" text
    And I should see a "key-value-edited" link

  Scenario: Edit the key description
    When I follow the left menu "Systems > Custom System Info"
    And I follow "key-label"
    And I enter "key-desc-edited" as "description"
    And I click on "Update Key"
    Then I should see a "key-label" link
    And I should see a "key-desc-edited" text

  Scenario: Delete the value
    When I follow the left menu "Systems > Custom System Info"
    And I follow "key-label"
    And I follow this "sle_client" link
    And I follow "Custom Info"
    And I follow "key-value-edited"
    And I follow "Delete Value"
    And I click on "Remove Value"
    Then I should see a "No custom information defined for this system." text

  Scenario: Delete the key
    When I follow the left menu "Systems > Custom System Info"
    And I follow "key-label"
    And I follow "Delete Key"
    And I click on "Delete Key"
    Then I should not see a "key-label" text
