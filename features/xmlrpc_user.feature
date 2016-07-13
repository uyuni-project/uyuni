# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test XML-RPC "user" namespace.

  @xmlrpc
  Scenario: Basic user operations
    Given I am logged in via XML-RPC/user as user "admin" and password "admin"
    When I call user.listUsers()
    Then I should get at least user "admin"
    When I call user.getDetails() on user "admin"
    Then I should see at least one role that matches "_admin" suffix
    Given I make sure "testluser" is not present
    When I call user.create(sid, login, pwd, name, lastname, email) with login "testluser"
    Then when I call user.listUsers(), I should see a user "testluser"
    And I logout from XML-RPC/user namespace.

  @xmlrpc
  Scenario: Role operations
    Given I am logged in via XML-RPC/user as user "admin" and password "admin"
    When I call user.addRole() on "testluser" with the role "org_admin"
    Then I should see "org_admin" when I call user.listRoles() with "testluser"
    When I call user.removeRole() against uid "testluser" with the role "org_admin"
    Then I shall not see "org_admin" when I call user.listRoles() with "testluser" uid
    And I logout from XML-RPC/user namespace.

   @xmlrpc
  Scenario: Cleanup user tests
    Given I am logged in via XML-RPC/user as user "admin" and password "admin"
    When I delete user "testluser"
    Then I logout from XML-RPC/user namespace.
