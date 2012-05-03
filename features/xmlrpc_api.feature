# Copyright (c) 2010-2011 SUSE Linux Products GmbH.
# Licensed under the terms of the MIT license.

Feature: Test XML-RPC "api" namespace.

  Scenario: Public API test
    Given I am logged in via XML-RPC/api as user "admin" and password "admin"
    When I call getVersion, I should get "11.1" as result
    When I call systemVersion, I should get "1.7" as result
    When I call getApiNamespaces, I should get 39 namespaces
    When I call getApiNamespaceCallList, I should get 515 available api calls
    When I call getApiCallList, I should get 39 available groups

