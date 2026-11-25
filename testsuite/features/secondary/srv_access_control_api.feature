# Copyright (c) 2026 SUSE LLC.
# SPDX-License-Identifier: MIT
#
# Role-based access control through the API: the "access" namespace (custom roles and their
# permissions) and the RBAC calls of the "user" namespace, with the effect on a regular user.
#
# The access.* calls authenticate as the last user logged into the web UI (the user.* calls
# always run as admin), so every step that calls them runs right after logging in as the
# intended user.

@scope_rbac
Feature: Role-based access control through the API

  Scenario: Pre-requisite: clean RBAC API test data and create the test user
    Given I am authorized for the "Admin" section
    And the access group "qe_rbac_api_empty" does not exist
    And the access group "qe_rbac_api_view" does not exist
    And the access group "qe_rbac_api_copy" does not exist
    And the access group "qe_rbac_api_union_a" does not exist
    And the access group "qe_rbac_api_union_b" does not exist
    And the access group "qe_rbac_api_union_copy" does not exist
    And the access group "qe_rbac_api_api_reader" does not exist
    And the access group "qe_rbac_api_created" does not exist
    And the user "rbac_api_tester" does not exist
    When I create a user with name "rbac_api_tester" and password "rbac_api_tester" with roles ""
    And I call user.list_users()
    Then I should get at least user "rbac_api_tester"

  Scenario: List the available namespaces
    Given I am authorized for the "Admin" section
    When I call access.list_namespaces()
    Then I should get permission "admin.access" with mode "R"
    And I should get permission "admin.access" with mode "W"
    And I should get permission "config.overview" with mode "R"
    And I should get permission "config.channels" with mode "W"

  Scenario: A predefined access group cannot be altered
    Given I am authorized for the "Admin" section
    Then I should get an API fault with "cannot be altered" when I call access.delete_role() on "config_admin"
    And I should get an API fault with "cannot be altered" when I call access.grant_access() on "config_admin" with namespace "config.overview" and mode "W"

  Scenario: Create an access group without permissions
    Given I am authorized for the "Admin" section
    When I call access.create_role() with label "qe_rbac_api_empty"
    Then the access group "qe_rbac_api_empty" should be present
    When I call access.list_permissions() on "qe_rbac_api_empty"
    Then I should get no permissions

  Scenario: Delete an access group
    Given I am authorized for the "Admin" section
    When I delete the access group "qe_rbac_api_empty"
    Then the access group "qe_rbac_api_empty" should not be present

  Scenario: Grant and revoke the View and Modify modes separately
    Given I am authorized for the "Admin" section
    When I call access.create_role() with label "qe_rbac_api_view"
    And I call access.grant_access() on "qe_rbac_api_view" with namespace "config.channels" and mode "R"
    And I call access.list_permissions() on "qe_rbac_api_view"
    Then I should get permission "config.channels" with mode "R"
    And I should not get permission "config.channels" with mode "W"
    When I call access.grant_access() on "qe_rbac_api_view" with namespace "config.channels" and mode "W"
    And I call access.list_permissions() on "qe_rbac_api_view"
    Then I should get permission "config.channels" with mode "W"
    When I call access.revoke_access() on "qe_rbac_api_view" with namespace "config.channels" and mode "W"
    And I call access.list_permissions() on "qe_rbac_api_view"
    Then I should get permission "config.channels" with mode "R"
    And I should not get permission "config.channels" with mode "W"

  Scenario: Grant a whole namespace family with a wildcard
    Given I am authorized for the "Admin" section
    When I call access.grant_access() on "qe_rbac_api_view" with namespace "config.*" and mode "R"
    And I call access.list_permissions() on "qe_rbac_api_view"
    Then I should get permission "config.overview" with mode "R"
    And I should get permission "config.files" with mode "R"
    And I should get permission "config.systems" with mode "R"
    And I should not get permission "config.files" with mode "W"

  Scenario: Assign the access group to a user and check the effective permissions
    Given I am authorized for the "Admin" section
    When I call user.add_role() on "rbac_api_tester" with the role "qe_rbac_api_view"
    And I call user.list_roles() on user "rbac_api_tester"
    Then I should get role "qe_rbac_api_view"
    When I call user.list_permissions() on user "rbac_api_tester"
    Then I should get permission "config.overview" with mode "R"
    And I should not get permission "config.files" with mode "W"
    When I am authorized as "rbac_api_tester" with password "rbac_api_tester"
    Then I should see a "Configuration" link in the left menu
    When I follow the left menu "Configuration > Overview"
    Then I should see a "Configuration Overview" text

  Scenario: Revoke the permissions and check that the user loses the page
    Given I am authorized for the "Admin" section
    When I call access.revoke_access() on "qe_rbac_api_view" with namespace "config.*" and mode "R"
    And I call user.list_permissions() on user "rbac_api_tester"
    Then I should not get permission "config.overview" with mode "R"
    When I am authorized as "rbac_api_tester" with password "rbac_api_tester"
    And I visit the "/rhn/configuration/Overview.do" path
    Then I should see a "403: Access Denied" text

  Scenario: Remove the access group from the user
    Given I am authorized for the "Admin" section
    When I call user.remove_role() on "rbac_api_tester" with the role "qe_rbac_api_view"
    And I call user.list_roles() on user "rbac_api_tester"
    Then I should not get role "qe_rbac_api_view"

  Scenario: Create an access group copying a predefined one
    Given I am authorized for the "Admin" section
    When I call access.create_role() with label "qe_rbac_api_copy" copying the permissions of "config_admin"
    And I call access.list_permissions() on "qe_rbac_api_copy"
    Then I should get permission "config.overview" with mode "R"
    And I should get permission "config.channels" with mode "W"
    And I should not get permission "admin.access" with mode "R"

  Scenario: Combine the permissions of several access groups
    Given I am authorized for the "Admin" section
    When I call access.create_role() with label "qe_rbac_api_union_a"
    And I call access.grant_access() on "qe_rbac_api_union_a" with namespace "config.overview" and mode "R"
    And I call access.create_role() with label "qe_rbac_api_union_b"
    And I call access.grant_access() on "qe_rbac_api_union_b" with namespace "config.files" and mode "R"
    And I call access.create_role() with label "qe_rbac_api_union_copy" copying the permissions of "qe_rbac_api_union_a" and "qe_rbac_api_union_b"
    And I call access.list_permissions() on "qe_rbac_api_union_copy"
    Then I should get permission "config.overview" with mode "R"
    And I should get permission "config.files" with mode "R"
    When I call user.add_role() on "rbac_api_tester" with the role "qe_rbac_api_union_a"
    And I call user.add_role() on "rbac_api_tester" with the role "qe_rbac_api_union_b"
    And I call user.list_permissions() on user "rbac_api_tester"
    Then I should get permission "config.overview" with mode "R"
    And I should get permission "config.files" with mode "R"
    And I should not get permission "config.systems" with mode "R"
    When I call user.remove_role() on "rbac_api_tester" with the role "qe_rbac_api_union_a"
    And I call user.list_permissions() on user "rbac_api_tester"
    Then I should not get permission "config.overview" with mode "R"
    But I should get permission "config.files" with mode "R"
    When I call user.remove_role() on "rbac_api_tester" with the role "qe_rbac_api_union_b"

  Scenario: Deny the access API to a user without permission
    Given I am authorized as "rbac_api_tester" with password "rbac_api_tester"
    Then I should get an API fault with "not available to user" when I call access.list_roles()
    And I should get an API fault with "not available to user" when I call access.create_role() with label "qe_rbac_api_created"

  Scenario: Allow only the API calls the access group grants
    Given I am authorized for the "Admin" section
    When I call access.create_role() with label "qe_rbac_api_api_reader"
    And I call access.grant_access() on "qe_rbac_api_api_reader" with namespace "api.access.list_roles" and mode "R"
    And I call user.add_role() on "rbac_api_tester" with the role "qe_rbac_api_api_reader"
    And I am authorized as "rbac_api_tester" with password "rbac_api_tester"
    Then the access group "qe_rbac_api_api_reader" should be present
    And I should get an API fault with "not available to user" when I call access.create_role() with label "qe_rbac_api_created"
    When I am authorized for the "Admin" section
    And I call access.grant_access() on "qe_rbac_api_api_reader" with namespace "api.access.create_role" and mode "W"
    And I am authorized as "rbac_api_tester" with password "rbac_api_tester"
    And I call access.create_role() with label "qe_rbac_api_created"
    Then the access group "qe_rbac_api_created" should be present

  Scenario: A user keeps the baseline API permissions of the regular_user group
    Given I am authorized as "rbac_api_tester" with password "rbac_api_tester"
    When I call channel.list_software_channels()
    Then I should get at least one software channel

  Scenario: Cleanup: remove the RBAC API test user and access groups
    Given I am authorized for the "Admin" section
    And the access group "qe_rbac_api_empty" does not exist
    And the access group "qe_rbac_api_view" does not exist
    And the access group "qe_rbac_api_copy" does not exist
    And the access group "qe_rbac_api_union_a" does not exist
    And the access group "qe_rbac_api_union_b" does not exist
    And the access group "qe_rbac_api_union_copy" does not exist
    And the access group "qe_rbac_api_api_reader" does not exist
    And the access group "qe_rbac_api_created" does not exist
    And the user "rbac_api_tester" does not exist
    Then the user "rbac_api_tester" should not be present
    And the access group "qe_rbac_api_created" should not be present
