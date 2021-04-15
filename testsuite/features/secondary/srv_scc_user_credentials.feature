# Copyright 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@no_mirror
Feature: SCC user credentials in the Setup Wizard
  As a systems administrator
  In order to manage organization's access to SUSE Customer Service
  I want to create, edit, and delete its credentials

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Enter some invalid organization credentials
    When I follow the left menu "Admin > Setup Wizard > Organization Credentials"
    And I ask to add new credentials
    And I enter "SCC user" as "edit-user"
    And I enter "SCC password" as "edit-password"
    And I click on "Save"
    Then I should see a "SCC user" text
    And the credentials for "SCC user" should be invalid

  Scenario: Make the credentials primary
    When I follow the left menu "Admin > Setup Wizard > Organization Credentials"
    And I make the credentials for "SCC user" primary
    Then the credentials for "SCC user" should be primary

  Scenario: Check the associated subscription list
    When I follow the left menu "Admin > Setup Wizard > Organization Credentials"
    And I view the subscription list for "SCC user"
    And I wait until I see "No subscriptions available" text
    And I click on "Close"

# TODO
# A test to edit the credentials is missing

  Scenario: Cleanup: delete the new organization credentials
    When I follow the left menu "Admin > Setup Wizard > Organization Credentials"
    And I wait for the trash icon to appear for "SCC user"
    And I ask to delete the credentials for "SCC user"
    And I click on "Delete"
    Then I should not see a "SCC user" text
