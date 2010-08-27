# feature/init_user_create.feature
@javascript
Feature: Create initial users
  In Order to run the tests 
  As a testing users
  I need to create the admin and a testing users
  Scenario: Create Admin users
    Given I am access the host initial
    When I go to the home page
    And I enter "admin" as "login"
    And I enter "admin" as "desiredpassword"
    And I enter "admin" as "desiredpasswordConfirm"
    And I enter "Admin" as "firstNames"
    And I enter "Admin" as "lastName"
    And I enter "galaxy-devel@suse.de" as "email"
    And I click on "Create Login"
    Then I can login
  Scenario: Enable Monitoring
    Given I am authorized as "admin" with password "admin"
    When I go to the admin configuration page
     And I check "Monitoring"
     And I click on "Update"
    Then I should see "The Spacewalk must be restarted to reflect these changes"
     And I should see "is_monitoring_enabled" as checked
#  Scenario: Create Testing username
#    Given I am authorized as admin
#    When