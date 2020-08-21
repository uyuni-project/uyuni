
Feature: Test salt sates module for org/user/group creation

  Scenario: deploy and execute salt state to create org, user and group
    When I copy "../upload_files/salt/setup_configuration_states.sls" to "/usr/share/susemanager/salt/setup_configuration_states.sls" on "server"
    And I run "salt-call --local --module-dirs=/usr/share/susemanager/salt --log-level=info --retcode-passthrough --force-color state.apply setup_configuration_states" on "server"

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

  Scenario: Check group creation
    Given I am on the groups page
    When I follow "minions_group"
    Then I should see a "minions_group" text
    And I should see a "System Group Status" text

  Scenario: Check user creation and group access
    Given I am authorized as "user2" with password "user2"
    When I follow the left menu "Systems > System Groups"
    And I follow "minions_group"
    Then I should see a "minions_group" text
    And I should see a "System Group Status" text

  Scenario: Check user channels permissions
    Given I am authorized as "user2" with password "user2"
    When I follow the left menu "Software > Channel List > All"
    And  I follow "Test-Channel-x86_64"
    And I follow "Managers"

  Scenario: Check user Roles
    Given I am on the active Users page
    And I follow "user2"
    Then I should see a "User Details" text
    And I should see "role_activation_key_admin" as checked
    And I should see "role_image_admin" as unchecked
    And I should see "role_config_admin" as checked
    And I should see "role_cluster_admin" as unchecked
    And I should see "role_channel_admin" as unchecked
    And I should see "role_system_group_admin" as unchecked

  Scenario: tear down salt state to create org, user and group
    When I copy "../upload_files/salt/teardown_configuration_states.sls" to "/usr/share/susemanager/salt/teardown_configuration_states.sls" on "server"
    And I run "salt-call --local --module-dirs=/usr/share/susemanager/salt --log-level=info --retcode-passthrough --force-color state.apply teardown_configuration_states" on "server"

  Scenario: Verify if organizations were removed
    Given I am on the Organizations page
    Then I should not see a "my_org" text
    And I should not see a "my_org2" text

  Scenario: Verify if user were removed
    Given I am on the active Users page
    Then I should not see a "user2" text
