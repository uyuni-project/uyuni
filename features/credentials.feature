# Copyright (c) 2010-2012 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Create external system or API credentials
  In order to access external systems or APIs
  As the testing user
  I want to create credentials

  Scenario: Fail to create credentials (empty)
    Given I am on the Credentials page
    When I click on "Update Credentials"
    Then I should see a "Your credentials are incomplete" text

  Scenario: Fail to create credentials (API key is missing)
    Given I am on the Credentials page
    When I enter "foobar-user" as "studio_user"
    And I click on "Update Credentials"
    Then I should see a "Your credentials are incomplete" text

  Scenario: Fail to create credentials (API user is missing)
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
    And I should see a "http://susestudio.com" text
    When I click on "Delete Credentials"
    Then I should see a "Your credentials were successfully deleted." text

