# feature/users-deleteuser1.feature
@users-createnewuser
Feature: Delete user user1
  Validate users page accessibility

  Scenario: Delete user1
    Given I am on the Details page
      When I follow "delete user"
      Then I should see a "Confirm User Deletion" text
        And I should see a "This will delete this user permanently." text
      When I click on "Delete User"
      Then I should see a "Active Users" text
        And I should not see a "user1" link
