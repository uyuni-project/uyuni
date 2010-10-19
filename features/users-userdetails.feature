# feature/users-userdetails.feature
@users-createnewuser
Feature: Watch/edit user details
  Validate users page accessibility

  Scenario: Access user details
    Given I am on the Details page
      Then I should see a "User Details" text
        And I should see a "delete user" link
        And I should see a "deactivate user" link
        And I should see a "Details" link
        And I should see a "System Groups" link
        And I should see a "Systems" link
        And I should see a "Channel Permissions" link
        And I should see a "Preferences" link
        And I should see a "Addresses" link
        And I should see a "Notification Methods" link
        And I should see a "user1" text
        And Option "Mr." is selected as "prefix"
        And I should see "Test" in field "firstNames"
        And I should see "User" in field "lastName"
        And I should see a "galaxy-devel@suse.de" text
        And I should see a "Change" link
        And I should see a "Administrative Roles" text
        And I should see a "Roles:" text
        And I should see a "Created:" text
        And I should see a "Last Sign In:" text

  Scenario: Change Roles (1)
    Given I am on the Details page
      When the "role_satellite_admin" checkbox should be disabled
        And I check "role_org_admin"
        And I check "role_system_group_admin"
        And I check "role_channel_admin"
        And I check "role_activation_key_admin"
        And I check "role_config_admin"
        And I check "role_monitoring_admin"
        And I click on "Submit"
      Then the "role_satellite_admin" checkbox should be disabled
        And I should see a "SUSE Manager Administrator" text
        And I should see "role_org_admin" as checked
        And I should see a "Organization Administrator" text
        And the "role_system_group_admin" checkbox should be disabled
        And I should see "role_system_group_admin" as checked
        And I should see a "System Group Administrator - [ Admin Access ]" text
        And the "role_channel_admin" checkbox should be disabled
        And I should see "role_channel_admin" as checked
        And I should see a "Channel Administrator - [ Admin Access ]" text
        And the "role_activation_key_admin" checkbox should be disabled
        And I should see "role_activation_key_admin" as checked
        And I should see a "Activation Key Administrator - [ Admin Access ]" text
        And the "role_config_admin" checkbox should be disabled
        And I should see "role_config_admin" as checked
        And I should see a "Configuration Administrator - [ Admin Access ]" text
        And the "role_monitoring_admin" checkbox should be disabled
        And I should see "role_monitoring_admin" as checked
        And I should see a "Monitoring Administrator - [ Admin Access ]" text
        And I should see a "Above roles are granted via the Organization Administrator role." text

  Scenario: Verify User List
    Given I am on the Users page
      Then Table row for "user1" should contain "Organization Administrator"
        And Table row for "user1" should contain "Channel Administrator"
        And Table row for "user1" should contain "Configuration Administrator"
        And Table row for "user1" should contain "Monitoring Administrator"
        And Table row for "user1" should contain "System Group Administrator"
        And Table row for "user1" should contain "Activation Key Administrator"

  Scenario: Deactivate User (Fail)
    Given I am on the Details page
      When I follow "deactivate user"
      Then I should see a "This action will deactivate this user. This user will no longer be able to log in or perform actions unless it is reactivated." text
      When I click on "Deactivate User"
      Then I should see a "You cannot deactivate another organization administrator. Please remove the 'Organization Administrator' role from this user before attempting to deactivate their account." text
      When I follow "Deactivated"
      Then I should see a "No deactivated users." text

  Scenario: Change Role (2)
    Given I am on the Details page
      When I check "role_org_admin"
        And I click on "Submit"
      Then I should see "role_org_admin" as unchecked

  Scenario: Deactivate User (Succeed)
    Given I am on the Details page
      Then I should see "role_org_admin" as unchecked
      When I follow "deactivate user"
      Then I should see a "This action will deactivate this user. This user will no longer be able to log in or perform actions unless it is reactivated." text
      When I click on "Deactivate User"
      Then I should see a "Active Users" text
        And I should not see a "user1" link
      When I follow "Deactivated"
      Then I should see a "Deactivated Users" text
        And I should see a "user1" link
      When I follow "All"
      Then I should see a "user1" link

  Scenario: Reactivate User (Succeed)
    Given I am on the Users page
      When I follow "Deactivated"
        And I follow "user1"
      Then I should see a "reactivate user" link
    When I follow "reactivate user"
    Then I should see a "This action will allow this user to access SUSE Manager. This user will retain all permissions, roles, and data that he or she had before being deactivated." text
    When I click on "Reactivate User"
      Then I should see a "Active Users" text
        And I should see a "user1" link
    When I follow "Deactivated"
    Then I should not see a "user1" link
