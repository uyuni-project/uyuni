# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
@scope_visualization
@scope_salt
Feature: Create organizations, users, groups, and activation keys using Salt states

  Scenario: Log in as org admin user
    Given I am authorized

@skip_if_containerized_server
  Scenario: Apply configuration salt state to server
    When I manually install the "uyuni-config" formula on the server

  Scenario: Apply setup_users_configuration state to server
    When I apply "setup_users_configuration" local salt state on "server"

  Scenario: Organization my_org was correctly created
    Given I am authorized as "my_org_user" with password "my_org_user"
    When I follow the left menu "Home > My Organization"
    Then I should see a "my_org" text in the content area
    When I follow the left menu "Home > My Organization > Organization Trusts"
    Then I should see a "my_org2" text in the content area

  Scenario: Organization my_org2 was correctly created
    Given I am authorized as "my_org_user2" with password "my_org_user2"
    When I follow the left menu "Home > My Organization"
    Then I should see a "my_org2" text in the content area
    When I follow the left menu "Home > My Organization > Organization Trusts"
    Then I should see a "my_org" text in the content area

  Scenario: Group was correctly created
    Given I am authorized for the "Admin" section
    When I follow the left menu "Systems > System Groups"
    When I follow "minions_group"
    Then I should see a "minions_group" text
    And I should see a "System Group Status" text

  Scenario: User was correctly created
    Given I am authorized as "user2" with password "user2"
    When I follow the left menu "Systems > System Groups"
    And I follow "minions_group"
    Then I should see a "minions_group" text
    And I should see a "System Group Status" text

  Scenario: User channels permissions were assigned
    Given I am authorized as "user2" with password "user2"
    When I follow the left menu "Software > Channel List > All"
    And I follow "Show All Child Channels"
    And I follow "Fake-RPM-SUSE-Channel"
    And I follow "Managers"

  Scenario: User Roles were assigned
    Given I am authorized for the "Admin" section
    When I follow the left menu "Users > User List > Active"
    And I follow "user2"
    Then I should see a "User Details" text
    And I should see "role_activation_key_admin" as checked
    And I should see "role_image_admin" as unchecked
    And I should see "role_config_admin" as checked
    And I should see "role_channel_admin" as unchecked
    And I should see "role_system_group_admin" as unchecked

  Scenario: Activation Key was correctly created
    When I follow the left menu "Systems > Activation Keys"
    And I follow "My Activation Key created via Salt"
    Then I should see "10" in field identified by "usageLimit"
    And I should see "virtualization_host" as checked
    And I should see a "Push via SSH" text
    And I should see "enable-config-auto-deploy" as checked

  Scenario: Cleanup: apply configuration teardown salt state to server
    When I apply "teardown_users_configuration" local salt state on "server"

@skip_if_containerized_server
  Scenario: Cleanup: uninstall the uyuni-config formula from the server
    And I manually uninstall the "uyuni-config" formula from the server

  Scenario: Cleanup: all organizations were successfully removed
    When I follow the left menu "Admin > Organizations"
    Then I should not see a "my_org" text
    And I should not see a "my_org2" text

  Scenario: Cleanup: user was successfully removed
    When I follow the left menu "Users > User List > Active"
    Then I should not see a "user2" text

  Scenario: Cleanup: activation key was successfully removed
    When I follow the left menu "Systems > Activation Keys"
    Then I should not see a "My Activation Key created via Salt" text
