# Copyright (c) 2026 SUSE LLC.
# SPDX-License-Identifier: MIT
#
# Role-based access control: the Salt and image management pages (Salt > Keys, Salt > Remote Commands,
# Images > Image List) belong to the predefined "regular_user" access group, which every new user gets.
# A user opens them through that baseline, is refused them once the group is removed, and gets them
# back through a custom access group that grants the same namespaces.
#
# The access.* API calls authenticate as the last user logged into the web UI, so every step that
# calls them runs right after logging in as admin. The user.* calls always run as admin.

@scope_rbac
Feature: Role-based access control: Salt and image management pages

  Scenario: Pre-requisite: clean RBAC page test data, create the test user and an access group
    Given I am authorized for the "Admin" section
    And the access group "qe_rbac_page_group" does not exist
    And the user "rbac_page_tester" does not exist
    When I create a user with name "rbac_page_tester" and password "rbac_page_tester" with roles ""
    # Creating the user switches the identity of the access.* calls to the new user
    And I am authorized for the "Admin" section
    And I call access.create_role() with label "qe_rbac_page_group"
    Then the access group "qe_rbac_page_group" should be present

  Scenario: A new user gets the Salt and image management permissions from the regular_user group
    Given I am authorized for the "Admin" section
    When I call user.list_permissions() on user "rbac_page_tester"
    Then I should get permission "salt.keys" with mode "R"
    And I should get permission "salt.remote_commands" with mode "W"
    And I should get permission "cm.image.list" with mode "R"
    And I should not get permission "config.overview" with mode "R"

  Scenario: A new user opens the Salt and image management pages
    Given I am authorized as "rbac_page_tester" with password "rbac_page_tester"
    When I visit the "/rhn/manager/systems/keys" path
    Then I should see a "Refresh Keys" text
    When I visit the "/rhn/manager/systems/cmd" path
    Then I should see a "Find targets" text
    When I visit the "/rhn/manager/cm/images" path
    Then I should see a "Refresh Images" text

  Scenario: Remove the regular_user group from the user
    Given I am authorized for the "Admin" section
    When I call user.remove_role() on "rbac_page_tester" with the role "regular_user"
    And I call user.list_permissions() on user "rbac_page_tester"
    Then I should not get permission "salt.keys" with mode "R"
    And I should not get permission "salt.remote_commands" with mode "W"
    And I should not get permission "cm.image.list" with mode "R"

  Scenario: Deny the Salt and image management pages to a user without the regular_user group
    Given I am authorized as "rbac_page_tester" with password "rbac_page_tester"
    When I visit the "/rhn/manager/systems/keys" path
    Then I should see a "403: Access Denied" text
    When I visit the "/rhn/manager/systems/cmd" path
    Then I should see a "403: Access Denied" text
    When I visit the "/rhn/manager/cm/images" path
    Then I should see a "403: Access Denied" text
    # Without any permission even the home page is denied, and its header has no sign-out button
    When I visit the "/rhn/Logout.do" path
    Then I should see a "Sign In" button

  Scenario: Open the Salt and image management pages through a custom access group
    Given I am authorized for the "Admin" section
    When I call access.grant_access() on "qe_rbac_page_group" with namespace "salt.keys" and mode "R"
    And I call access.grant_access() on "qe_rbac_page_group" with namespace "salt.remote_commands" and mode "W"
    And I call access.grant_access() on "qe_rbac_page_group" with namespace "cm.image.list" and mode "R"
    And I call user.add_role() on "rbac_page_tester" with the role "qe_rbac_page_group"
    And I am authorized as "rbac_page_tester" with password "rbac_page_tester"
    And I visit the "/rhn/manager/systems/keys" path
    Then I should see a "Refresh Keys" text
    When I visit the "/rhn/manager/systems/cmd" path
    Then I should see a "Find targets" text
    When I visit the "/rhn/manager/cm/images" path
    Then I should see a "Refresh Images" text
    # The custom group grants no home permission either, so the header has no sign-out button
    When I visit the "/rhn/Logout.do" path
    Then I should see a "Sign In" button

  Scenario: Cleanup: remove the RBAC page test user and access group
    Given I am authorized for the "Admin" section
    And the access group "qe_rbac_page_group" does not exist
    And the user "rbac_page_tester" does not exist
    Then the access group "qe_rbac_page_group" should not be present
    And the user "rbac_page_tester" should not be present
