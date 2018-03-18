# Copyright (c) 2015-18 SUSE LLC
# Licensed under the terms of the MIT license.

Feature:  SUSE Studio credentials page

  Scenario: Fail to create empty SUSE Studio credentials
    Given I am on the Credentials page
    When I click on "Update Credentials"
    Then I should see a "Your credentials are incomplete" text

  Scenario: Fail to create SUSE Studio credentials wthout API key
    Given I am on the Credentials page
    When I enter "foobar-user" as "studio_user"
    And I click on "Update Credentials"
    Then I should see a "Your credentials are incomplete" text

  Scenario: Fail to create SUSE Studio credentials without API user
    Given I am on the Credentials page
    When I enter "foobar-key" as "studio_key"
    And I click on "Update Credentials"
    Then I should see a "Your credentials are incomplete" text

  Scenario: Create SUSE Studio credentials succesfully
    Given I am on the Credentials page
    When I enter "foobar-user" as "studio_user"
    And I enter "foobar-key" as "studio_key"
    And I click on "Update Credentials"
    Then I should see a "Your credentials were successfully updated." text

  Scenario: Delete SUSE Studio credentials
    Given I am on the Credentials page
    When I follow "delete credentials"
    Then I should see a "foobar-user" text
    And I should see a "foobar-key" text
    And I should see a "https://susestudio.com" text
    When I click on "Delete Credentials"
    Then I should see a "Your credentials were successfully deleted." text
