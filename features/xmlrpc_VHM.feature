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
    Then I should get "username"
     And I should get "password"
     And I should get "hostname"
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
      And configs "hostname" should be "10.162.186.115"
      And I logout from XML-RPC/virtualhostmanager

  Scenario: Run virtual-host-gatherer
    Given I am on the Admin page
    When I follow "Task Schedules"
    And I follow "gatherer-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait for "10" seconds

  Scenario: Check new Virtual Hosts
    Given I am on the Systems page
      And I follow "Systems" in the left menu
    When I follow "10.162.186.111"
    Then I should see a "OS: VMware ESXi" text
    When I follow "Virtual Systems" in the left menu
    Then I should see a "vCenter" text
     And I should see a "NSX-l3gateway" text

  Scenario: Check delete
    Given I am logged in via XML-RPC/virtualhostmanager as user "admin" and password "admin"
    When I call virtualhostmanager.delete("vCenter")
     And I call virtualhostmanager.listVirtualHostManagers()
    Then I should get 0 returned
     And I logout from XML-RPC/virtualhostmanager
