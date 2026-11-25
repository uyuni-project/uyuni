# Copyright (c) 2026 SUSE LLC.
# SPDX-License-Identifier: MIT
#
# Custom access groups (Admin > Access Control): a regular user is denied the
# configuration pages until an access group copied from the configuration
# administrator grants them, and loses them again when the group is deleted.
#
# The access.* API calls authenticate as the last user logged into the web UI,
# so every step that calls them runs right after logging in as admin.

@scope_rbac
Feature: Role-based access control with custom access groups

  Scenario: Pre-requisite: clean RBAC test data and create the test user
    Given I am authorized for the "Admin" section
    And the access group "qe_rbac_config_viewer" does not exist
    And the user "rbac_tester" does not exist
    When I create a user with name "rbac_tester" and password "rbac_tester" with roles ""
    And I call user.list_users()
    Then I should get at least user "rbac_tester"
    When I call user.list_roles() on user "rbac_tester"
    Then I should not get role "qe_rbac_config_viewer"

  Scenario: Deny configuration pages to a user without permission
    Given I am authorized as "rbac_tester" with password "rbac_tester"
    Then I should not see a "Configuration" link in the left menu
    When I visit the "/rhn/configuration/Overview.do" path
    Then I should see a "403: Access Denied" text

  Scenario: Create an access group with the wizard
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Access Control"
    And I follow "Create Access Group"
    And I enter "qe_rbac_config_viewer" as "name"
    And I enter "QE RBAC configuration viewer" as "description"
    And I select "SUSE Test" from "orgId"
    And I select "Configuration Administrator" from "accessGroups"
    And I click on "Continue"
    # Copied permissions are applied only once the namespaces are fetched:
    # with "Only selected" the table stays empty until then
    And I check "showOnlySelected"
    And I wait until I do not see "There are no entries to show." text
    And I click on "Continue"
    And I select "rbac_tester" from "login"
    Then table row for "rbac_tester" should contain "galaxy-noise@localhost"
    When I click on "Continue"
    Then I should see a "Create" button
    When I wait until I do not see "No permissions selected." text
    And I click on "Create"
    And I enter "qe_rbac_config_viewer" in the placeholder "Filter by name or description"
    Then table row for "qe_rbac_config_viewer" should contain "SUSE Test"
    When I call user.list_roles() on user "rbac_tester"
    Then I should get role "qe_rbac_config_viewer"

  Scenario: Grant configuration pages through the access group
    Given I am authorized as "rbac_tester" with password "rbac_tester"
    Then I should see a "Configuration" link in the left menu
    When I follow the left menu "Configuration > Overview"
    Then I should see a "Configuration Overview" text

  Scenario: Require name, description and organization for a new access group
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Access Control"
    And I follow "Create Access Group"
    And I click on "Continue"
    Then I should see a "Name is required." text
    And I should see a "Description is required." text
    And I should see a "Organization is required." text

  Scenario: Reject a predefined or an existing access group name
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Access Control"
    And I follow "Create Access Group"
    And I enter "channel_admin" as "name"
    And I enter "QE RBAC predefined name" as "description"
    And I select "SUSE Test" from "orgId"
    And I click on "Continue"
    And I wait until I see "Review and modify the permissions for this custom group as needed." text
    And I click on "Continue"
    And I wait until I see "Search & Add Users" text
    And I click on "Continue"
    And I click on "Create"
    Then I should see a "channel_admin already exists." text
    When I follow the left menu "Admin > Access Control"
    And I follow "Create Access Group"
    And I enter "qe_rbac_config_viewer" as "name"
    And I enter "QE RBAC existing name" as "description"
    And I select "SUSE Test" from "orgId"
    And I click on "Continue"
    And I wait until I see "Review and modify the permissions for this custom group as needed." text
    And I click on "Continue"
    And I wait until I see "Search & Add Users" text
    And I click on "Continue"
    And I click on "Create"
    Then I should see a "Access Group: qe_rbac_config_viewer already exists on organization: SUSE Test." text

  Scenario: Edit the description of an access group
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Access Control"
    And I enter "qe_rbac_config_viewer" in the placeholder "Filter by name or description"
    And I click on "Edit Access Group" in row "qe_rbac_config_viewer"
    And I enter "QE RBAC configuration viewer edited" as "description"
    And I click on "Update Access Group"
    And I enter "qe_rbac_config_viewer" in the placeholder "Filter by name or description"
    Then table row for "qe_rbac_config_viewer" should contain "QE RBAC configuration viewer edited"

  Scenario: Delete the access group and revoke its permissions
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Access Control"
    And I enter "qe_rbac_config_viewer" in the placeholder "Filter by name or description"
    And I click on "Delete Access Group" in row "qe_rbac_config_viewer"
    And I click on "Delete" in "Delete Access Group" modal
    Then I should see a "Access Group 'qe_rbac_config_viewer' has been deleted." text
    And I should see a "No Access Group found." text
    When I am authorized as "rbac_tester" with password "rbac_tester"
    And I visit the "/rhn/configuration/Overview.do" path
    Then I should see a "403: Access Denied" text

  Scenario: Cleanup: remove the RBAC test user and access group
    Given I am authorized for the "Admin" section
    And the access group "qe_rbac_config_viewer" does not exist
    And the user "rbac_tester" does not exist
    Then the user "rbac_tester" should not be present
