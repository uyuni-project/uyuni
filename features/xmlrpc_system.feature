# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test XML-RPC "system" namespace.

  Scenario: Check listSystems and sysinfo
    Given I am logged in via XML-RPC/system as user "admin" and password "admin"
    When I call system.listSystems(), I should get a list of them.
    When I check a sysinfo by a number of XML-RPC calls, it just works. :-)
    Then I logout from XML-RPC/system.

  Scenario: create a cobbler system record for a system that is not registered (requires fedora_kickstart_profile)
    Given I am logged in via XML-RPC/system as user "admin" and password "admin"
    And cobblerd is running
    When I call system.createSystemRecord() with sysName "mySystem", ksLabel "fedora_kickstart_profile", ip "10.20.30.40", mac "DE:AD:BE:EF:11:22"
    Then there is a system record in cobbler named "mySystem:1"
    Then I logout from XML-RPC/system.
