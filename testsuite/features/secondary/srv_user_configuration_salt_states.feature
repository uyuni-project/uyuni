# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Create organization, user and group using salt states

  Scenario: Apply setup configure salt state to server
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

  Scenario: Group was correct created
    Given I am on the groups page
    When I follow "minions_group"
    Then I should see a "minions_group" text
    And I should see a "System Group Status" text

  Scenario: user was correctly created
    Given I am authorized as "user2" with password "user2"
    When I follow the left menu "Systems > System Groups"
    And I follow "minions_group"
    Then I should see a "minions_group" text
    And I should see a "System Group Status" text

  Scenario: User channels permissions were assigned
    Given I am authorized as "user2" with password "user2"
    When I follow the left menu "Software > Channel List > All"
    And  I follow "Test-Channel-x86_64"
    And I follow "Managers"

  Scenario: User Roles were assigned
    Given I am on the active Users page
    And I follow "user2"
    Then I should see a "User Details" text
    And I should see "role_activation_key_admin" as checked
    And I should see "role_image_admin" as unchecked
    And I should see "role_config_admin" as checked
    And I should see "role_cluster_admin" as unchecked
    And I should see "role_channel_admin" as unchecked
    And I should see "role_system_group_admin" as unchecked

  Scenario: Apply teardown configure salt state to server
#    When I copy "../upload_files/salt/teardown_configuration_states.sls" to "/usr/share/susemanager/salt/teardown_configuration_states.sls" on "server"
#    And I run "salt-call --local --module-dirs=/usr/share/susemanager/salt --log-level=info --retcode-passthrough --force-color state.apply teardown_configuration_states" on "server"
    When I apply "teardown_users_configuration" local salt state on "server"

  Scenario: All organizations were successfully removed
    Given I am on the Organizations page
    Then I should not see a "my_org" text
    And I should not see a "my_org2" text

  Scenario: User was successfully removed
    Given I am on the active Users page
    Then I should not see a "user2" text
