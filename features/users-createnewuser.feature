# feature/users.feature
@javascript
Feature: Create a new user

  Scenario: Create a new user
    Given I am authorized as "admin" with password "admin"
    When I go to the users page
    And I follow "create new user"
     And I enter "user1" as "login"
     And I enter "user1" as "desiredpassword"
     And I enter "user1" as "desiredpasswordConfirm"
     And I select "Mr." from "prefix"
     And I enter "Test" as "firstNames"
     And I enter "User" as "lastName"
     And I enter "galaxy-devel@suse.de" as "email"
     And I click on "Create Login"
    Then I should see a "Account user1 created, login information sent to galaxy-devel@suse.de" text
     And I should see a "user1" link
     And I should see a "normal user" text

