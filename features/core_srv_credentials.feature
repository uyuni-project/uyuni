# Copyright (c) 2015-17 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Create external system or API credentials
  In order to access external systems or APIs
  As the testing user
  I want to create credentials

  Scenario: Fail to create empty credentials
    Given I am on the Credentials page
    When I click on "Update Credentials"
    Then I should see a "Your credentials are incomplete" text

  Scenario: Fail to create credentials wthout API key
    Given I am on the Credentials page
    When I enter "foobar-user" as "studio_user"
    And I click on "Update Credentials"
    Then I should see a "Your credentials are incomplete" text

  Scenario: Fail to create credentials without API user
    Given I am on the Credentials page
    When I enter "foobar-key" as "studio_key"
    And I click on "Update Credentials"
    Then I should see a "Your credentials are incomplete" text

  Scenario: Create credentials succesfully
    Given I am on the Credentials page
    When I enter "foobar-user" as "studio_user"
    And I enter "foobar-key" as "studio_key"
    And I click on "Update Credentials"
    Then I should see a "Your credentials were successfully updated." text

  Scenario: Delete credentials
    Given I am on the Credentials page
    When I follow "delete credentials"
    Then I should see a "foobar-user" text
    And I should see a "foobar-key" text
    And I should see a "https://susestudio.com" text
    When I click on "Delete Credentials"
    Then I should see a "Your credentials were successfully deleted." text
