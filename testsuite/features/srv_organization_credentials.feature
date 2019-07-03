# Copyright 2017-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Organization credentials in the Setup Wizard

@no_mirror
  Scenario: Create some organization credentials
    Given I am on the Admin page
    When I follow "Organization Credentials" in the content area
    And I want to add a new credential
    And I enter "SCC user" as "edit-user"
    And I enter "SCC password" as "edit-password"
    And I click on "Save"
    Then I should see a "SCC user" text

@no_mirror
  Scenario: Make the credentials primary
    Given I am on the Admin page
    When I follow "Organization Credentials" in the content area
    And I make the credentials primary

@no_mirror
  Scenario: Check the associated subscription list
    Given I am on the Admin page
    When I follow "Organization Credentials" in the content area
    And I view the primary subscription list for SCC user
    Then I should see a "No subscriptions available" text
    When I click on "Close"

# Missing: edit the credentials

@no_mirror
  Scenario: Cleanup: delete the new organization credentials
    Given I am on the Admin page
    When I follow "Organization Credentials" in the content area
    And I delete the primary credentials
    And I view the primary subscription list
    And I click on "Close"
    Then I should not see a "SCC user" text
    And I see verification succeeded
