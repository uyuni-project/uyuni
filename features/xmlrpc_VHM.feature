# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test XML-RPC "virtualhostmanager" namespace.

  Scenario: Check listAvailableVirtualHostGathererModules
    Given I am logged in via XML-RPC/virtualhostmanager as user "admin" and password "admin"
    When I call virtualhostmanager.listAvailableVirtualHostGathererModules()
    Then I should get two modules
     And I logout from XML-RPC/virtualhostmanager

  Scenario: Check listVirtualHostManagers
    Given I am logged in via XML-RPC/virtualhostmanager as user "admin" and password "admin"
    When I call virtualhostmanager.listVirtualHostManagers()
    Then I should get 0 returned
    And I logout from XML-RPC/virtualhostmanager

  Scenario: Check getModuleParameters
    Given I am logged in via XML-RPC/virtualhostmanager as user "admin" and password "admin"
    When I call virtualhostmanager.getModuleParameters() for "VMware"
    Then I should get "user"
     And I should get "pass"
     And I should get "host"
     And I should get "port"
     And I logout from XML-RPC/virtualhostmanager

  Scenario: Check create
    Given I am logged in via XML-RPC/virtualhostmanager as user "admin" and password "admin"
    When I call virtualhostmanager.create("vCenter", "VMware") and params from "/root/virtualhostmanager.create.json"
     And I call virtualhostmanager.listVirtualHostManagers()
    Then I should get 1 returned
    And I logout from XML-RPC/virtualhostmanager

  Scenario: Check getDetail
    Given I am logged in via XML-RPC/virtualhostmanager as user "admin" and password "admin"
     When I call virtualhostmanager.getDetail("vCenter")
     Then "label" should be "vCenter"
      And "org_id" should be "1"
      And "gatherer_module" should be "VMware"
      And configs "host" should be "10.162.186.115"
      And I logout from XML-RPC/virtualhostmanager

  Scenario: Check delete
    Given I am logged in via XML-RPC/virtualhostmanager as user "admin" and password "admin"
    When I call virtualhostmanager.delete("vCenter")
     And I call virtualhostmanager.listVirtualHostManagers()
    Then I should get 0 returned
     And I logout from XML-RPC/virtualhostmanager
