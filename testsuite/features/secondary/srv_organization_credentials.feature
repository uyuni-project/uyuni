# Copyright 2017-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Organization credentials in the Setup Wizard

@scc_credentials
  Scenario: Enter valid SCC credentials
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Organization Credentials"
    And I enter the SCC credentials

@no_mirror
  Scenario: Create some organization credentials
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Organization Credentials"
    And I want to add a new credential
    And I enter "SCC user" as "edit-user"
    And I enter "SCC password" as "edit-password"
    And I click on "Save"
    Then I should see a "SCC user" text

@no_mirror
  Scenario: Make the credentials primary
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Organization Credentials"
    And I make the credentials primary

@no_mirror
  Scenario: Check the associated subscription list
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Organization Credentials"
    And I view the primary subscription list for SCC user
    And I wait until I see "No subscriptions available" text
    And I click on "Close"

# Missing: edit the credentials

@no_mirror
  Scenario: Cleanup: delete the new organization credentials
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Organization Credentials"
    And I delete the primary credentials
    And I view the primary subscription list
    And I click on "Close"
    Then I should not see a "SCC user" text
    And I see verification succeeded
