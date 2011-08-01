# Copyright (c) 2010-2011 SUSE Linux Products GmbH.
# Licensed under the terms of the MIT license.

Feature: Test XML-RPC "system" namespace.

  Scenario: 
    Given I am logged in via XML-RPC/system as user "admin" and password "admin"
    When I call system.listSystems(), I should get a list of them.
    When I check a sysinfo by a number of XML-RPC calls, it just works. :-)

    Then I logout from XML-RPC/system.
