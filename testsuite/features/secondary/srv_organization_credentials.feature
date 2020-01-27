# Copyright 2017-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Organization credentials in the Setup Wizard

@no_mirror
  Scenario: Enter some invalid organization credentials
    Given I am on the Admin page
    When I follow "Organization Credentials" in the content area
    And I ask to add new credentials
    And I enter "SCC user" as "edit-user"
    And I enter "SCC password" as "edit-password"
    And I click on "Save"
    Then I should see a "SCC user" text
    And the credentials for "SCC user" should be invalid

@no_mirror
  Scenario: Make the credentials primary
    Given I am on the Admin page
    When I follow "Organization Credentials" in the content area
    And I make the credentials for "SCC user" primary
    Then the credentials for "SCC user" should be primary

@no_mirror
  Scenario: Check the associated subscription list
    Given I am on the Admin page
    When I follow "Organization Credentials" in the content area
    And I view the subscription list for "SCC user"
    And I wait until I see "No subscriptions available" text
    And I click on "Close"

# TODO
# A test to edit the credentials is missing

@no_mirror
  Scenario: Cleanup: delete the new organization credentials
    Given I am on the Admin page
    When I follow "Organization Credentials" in the content area
    And I wait for the trash icon to appear for "SCC user"
    And I ask to delete the credentials for "SCC user"
    And I click on "Delete"
    Then I should not see a "SCC user" text
