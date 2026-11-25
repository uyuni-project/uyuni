# Copyright (c) 2026 SUSE LLC.
# SPDX-License-Identifier: MIT
#
# Role-based access control: editing the permissions and the users of an existing access group
# in the web UI, and the effect of View and Modify permissions on a regular user, for two
# families of namespaces (configuration, activation keys).
#
# The access.* API calls authenticate as the last user logged into the web UI, so every step that
# calls them runs right after logging in as admin. The UI changes are checked through the API.

@scope_rbac
Feature: Role-based access control: permissions and users of an access group

  Scenario: Pre-requisite: clean RBAC permission test data, create the test user and an access group
    Given I am authorized for the "Admin" section
    And the access group "qe_rbac_perm_group" does not exist
    And the user "rbac_perm_tester" does not exist
    When I create a user with name "rbac_perm_tester" and password "rbac_perm_tester" with roles ""
    # Creating the user switches the identity of the access.* calls to the new user
    And I am authorized for the "Admin" section
    And I call access.create_role() with label "qe_rbac_perm_group"
    Then the access group "qe_rbac_perm_group" should be present
    When I call user.list_users()
    Then I should get at least user "rbac_perm_tester"

  Scenario: Predefined access groups are not listed in the web UI
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Access Control"
    And I enter "qe_rbac_perm_group" in the placeholder "Filter by name or description"
    Then I should see a "qe_rbac_perm_group" text
    When I enter "config_admin" in the placeholder "Filter by name or description"
    Then I should see a "No Access Group found." text

  Scenario: Filter the permission tree by name
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Access Control"
    And I enter "qe_rbac_perm_group" in the placeholder "Filter by name or description"
    And I click on "Edit Access Group" in row "qe_rbac_perm_group"
    And I open the "Namespaces & Permissions" tab of the access group
    And I enter "remote_commands" in the placeholder "Filter by name"
    Then I should see a "remote_commands" text

  Scenario: Filter the permission tree by API and Web namespaces
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Access Control"
    And I enter "qe_rbac_perm_group" in the placeholder "Filter by name or description"
    And I click on "Edit Access Group" in row "qe_rbac_perm_group"
    And I open the "Namespaces & Permissions" tab of the access group
    Then I should see the namespace row "config" in the permission tree
    And I should see API namespace rows in the permission tree
    When I check "apiNamespace"
    Then I should not see the namespace row "config" in the permission tree
    And I should see API namespace rows in the permission tree
    When I uncheck "apiNamespace"
    And I check "webNamespace"
    Then I should see the namespace row "config" in the permission tree
    And I should not see API namespace rows in the permission tree

  Scenario: Add the View permission to an existing access group
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Access Control"
    And I enter "qe_rbac_perm_group" in the placeholder "Filter by name or description"
    And I click on "Edit Access Group" in row "qe_rbac_perm_group"
    And I open the "Namespaces & Permissions" tab of the access group
    And I check the "View" permission of the namespace "config"
    And I click on "Update Access Group"
    # The list page appears once the save request is done
    And I wait until I see "Access Group Management" text
    And I call access.list_permissions() on "qe_rbac_perm_group"
    Then I should get permission "config.overview" with mode "R"
    And I should get permission "config.channels" with mode "R"
    And I should not get permission "config.overview" with mode "W"
    And I should not get permission "config.channels" with mode "W"

  Scenario: Add the Modify permission to an existing access group
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Access Control"
    And I enter "qe_rbac_perm_group" in the placeholder "Filter by name or description"
    And I click on "Edit Access Group" in row "qe_rbac_perm_group"
    And I open the "Namespaces & Permissions" tab of the access group
    And I check the "Modify" permission of the namespace "config"
    And I click on "Update Access Group"
    # The list page appears once the save request is done
    And I wait until I see "Access Group Management" text
    And I call access.list_permissions() on "qe_rbac_perm_group"
    Then I should get permission "config.overview" with mode "R"
    And I should get permission "config.channels" with mode "W"

  Scenario: Remove the permissions from an existing access group
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Access Control"
    And I enter "qe_rbac_perm_group" in the placeholder "Filter by name or description"
    And I click on "Edit Access Group" in row "qe_rbac_perm_group"
    And I open the "Namespaces & Permissions" tab of the access group
    And I uncheck the "Modify" permission of the namespace "config"
    And I uncheck the "View" permission of the namespace "config"
    And I click on "Update Access Group"
    # The list page appears once the save request is done
    And I wait until I see "Access Group Management" text
    And I call access.list_permissions() on "qe_rbac_perm_group"
    Then I should not get permission "config.overview" with mode "R"
    And I should not get permission "config.channels" with mode "W"

  Scenario: Add a user to an existing access group in the web UI
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Access Control"
    And I enter "qe_rbac_perm_group" in the placeholder "Filter by name or description"
    And I click on "Edit Access Group" in row "qe_rbac_perm_group"
    And I open the "Users" tab of the access group
    And I select "rbac_perm_tester" from "login"
    And I click on "Update Access Group"
    # The list page appears once the save request is done
    And I wait until I see "Access Group Management" text
    And I call user.list_roles() on user "rbac_perm_tester"
    Then I should get role "qe_rbac_perm_group"

  Scenario: Remove a user from an existing access group in the web UI
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Access Control"
    And I enter "qe_rbac_perm_group" in the placeholder "Filter by name or description"
    And I click on "Edit Access Group" in row "qe_rbac_perm_group"
    And I open the "Users" tab of the access group
    And I remove the user "rbac_perm_tester" from the access group
    And I click on "Update Access Group"
    # The list page appears once the save request is done
    And I wait until I see "Access Group Management" text
    And I call user.list_roles() on user "rbac_perm_tester"
    Then I should not get role "qe_rbac_perm_group"

  Scenario: The View permission alone does not allow a Modify page
    Given I am authorized for the "Admin" section
    When I call access.grant_access() on "qe_rbac_perm_group" with namespace "config.channels" and mode "R"
    And I call user.add_role() on "rbac_perm_tester" with the role "qe_rbac_perm_group"
    And I am authorized as "rbac_perm_tester" with password "rbac_perm_tester"
    And I visit the "/rhn/configuration/GlobalConfigChannelList.do" path
    Then I should see a "Centrally Managed Configuration Channels" text
    When I visit the "/rhn/configuration/ChannelCreate.do" path
    Then I should see a "403: Access Denied" text

  Scenario: The Modify permission allows the Modify page
    Given I am authorized for the "Admin" section
    When I call access.grant_access() on "qe_rbac_perm_group" with namespace "config.channels" and mode "W"
    And I am authorized as "rbac_perm_tester" with password "rbac_perm_tester"
    And I visit the "/rhn/configuration/ChannelCreate.do" path
    Then I should see a "New Config Channel" text
    And I should not see a "403: Access Denied" text

  Scenario: The permissions of one namespace family do not leak into another
    Given I am authorized as "rbac_perm_tester" with password "rbac_perm_tester"
    When I visit the "/rhn/activationkeys/List.do" path
    Then I should see a "403: Access Denied" text
    When I am authorized for the "Admin" section
    And I call access.grant_access() on "qe_rbac_perm_group" with namespace "systems.activation_keys.list" and mode "R"
    And I am authorized as "rbac_perm_tester" with password "rbac_perm_tester"
    And I visit the "/rhn/activationkeys/List.do" path
    Then I should see a "Activation Keys" text
    And I should not see a "403: Access Denied" text

  Scenario: Cleanup: remove the RBAC permission test user and access group
    Given I am authorized for the "Admin" section
    And the access group "qe_rbac_perm_group" does not exist
    And the user "rbac_perm_tester" does not exist
    Then the access group "qe_rbac_perm_group" should not be present
    And the user "rbac_perm_tester" should not be present
