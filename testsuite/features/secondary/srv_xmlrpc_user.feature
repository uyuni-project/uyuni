# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: XML-RPC "user" namespace

  Scenario: Basic user operations
    Given I am logged in via XML-RPC user with the feature's user
    When I call user.list_users()
    Then I should get at least user "admin"
    When I call user.get_details() on user "admin"
    Then I should see at least one role that matches "_admin" suffix

    Given I make sure "testluser" is not present
    When I call user.create(sid, login, pwd, name, lastname, email) with login "testluser"
    Then when I call user.list_users(), I should see a user "testluser"
    And I logout from XML-RPC user namespace

  Scenario: Role operations
    Given I am logged in via XML-RPC user with the feature's user
    When I call user.add_role() on "testluser" with the role "org_admin"
    Then I should see "org_admin" when I call user.list_roles() with "testluser"
    When I call user.remove_role() against uid "testluser" with the role "org_admin"
    Then I shall not see "org_admin" when I call user.list_roles() with "testluser" uid
    And I logout from XML-RPC user namespace

  Scenario: Cleanup: user tests
    Given I am logged in via XML-RPC user with the feature's user
    When I delete user "testluser"
    Then I logout from XML-RPC user namespace
