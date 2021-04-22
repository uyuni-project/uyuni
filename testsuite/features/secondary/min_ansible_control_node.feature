# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_ansible
Feature: Operate an Ansible control node in a normal minion

  Scenario: Enable "Ansible control node" system type
     Given I am on the Systems overview page of this "sle_minion"
     When I follow "Properties" in the content area
     And I check "ansible_control_node"
     And I click on "Update Properties"
     Then I should see a "Ansible Control Node type has been applied." text

  Scenario: Apply highstate and check that Ansible is installed
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
    Then "ansible" should be installed on "sle_minion"

  Scenario: The "ansible" tab appears in the UI
     Given I am on the Systems overview page of this "sle_minion"
     When I follow "Ansible" in the content area
     Then I should see a "Ansible Control Node Configuration" text

  Scenario: Pre-requisite: Deploy test playbooks and inventory file
     Given I deploy testing playbooks and inventory files to "sle_minion" 

  Scenario: Cleanup: Disable Ansible and remove test playbooks and inventory file
     Given I am on the Systems overview page of this "sle_minion"
     When I follow "Properties" in the content area
     And I uncheck "ansible_control_node"
     And I click on "Update Properties"
     Then I should see a "System properties changed" text
     And I apply highstate on "sle_minion"
     And "ansible" should be installed on "sle_minion"
     And I remove testing playbooks and inventory files from "sle_minion"

  Scenario: Configure some inventory and playbooks path
  Scenario: Display inventories
  Scenario: Discover playbooks and display them
  Scenario: Run a playbook
