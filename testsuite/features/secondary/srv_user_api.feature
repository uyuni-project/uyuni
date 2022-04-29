# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_api
Feature: API "user" namespace

  Scenario: List users
    When I am logged in API as user "admin" and password "admin"
    And I call user.list_users()
    Then I should get at least user "admin"

  Scenario: List roles
    When I call user.list_roles() on user "admin"
    Then I should get at least one role that matches "_admin" suffix

  Scenario: Create user
    When I make sure "testuser" is not present
    And I call user.create() with login "testuser"
    And I call user.list_users()
    Then I should get at least user "testuser"

  Scenario: Role operations
    When I call user.add_role() on "testuser" with the role "org_admin"
    And I call user.list_roles() on user "testuser"
    Then I should get role "org_admin"
    When I call user.remove_role() on "testuser" with the role "org_admin"
    And I call user.list_roles() on user "testuser"
    Then I should not get role "org_admin"

  Scenario: Cleanup: user tests
    When I delete user "testuser"
    And I logout from API
