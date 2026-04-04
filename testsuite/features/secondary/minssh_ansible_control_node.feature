# Copyright (c) 2021-2025 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_ansible
@scope_salt_ssh
@ssh_minion
Feature: Operate an Ansible control node in SSH minion

  Scenario: Log in as org admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: Deploy test playbooks and inventory file
    When I deploy testing playbooks and inventory files to "ssh_minion"

  @susemanager
  Scenario: Pre-requisite: Subscribe SUSE minions to SLE-Module-Python3-15-SP7-Pool for x86_64
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I check "SLE-Module-Python3-15-SP7-Pool for x86_64" by label
    And I click on "Next"
    And I click on "Confirm"
    And I wait until I see "Changing the channels has been scheduled." text
    And I follow "scheduled"
    And I wait until I see "1 system successfully completed this action" text, refreshing the page

  Scenario: Enable "Ansible control node" system type
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Properties" in the content area
    And I check "ansible_control_node"
    And I click on "Update Properties"
    Then I wait until I see "Ansible Control Node type has been applied." text

  Scenario: Apply highstate and check that Ansible is installed
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled" is completed
    Then "ansible" should be installed on "ssh_minion"

  Scenario: The Ansible tab appears in the system overview page
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Ansible" in the content area
    Then I should see a "Ansible Control Node Configuration" text

  Scenario: Configure some inventory and playbooks path
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Ansible" in the content area
    Then I should see a "Ansible Control Node Configuration" text
    And I enter "/srv/playbooks/" as "new_playbook_path_input"
    And I click on "new_playbook_path_save"
    And I enter "/srv/playbooks/orion_dummy/hosts" as "new_inventory_path_input"
    And I click on "new_inventory_path_save"

  Scenario: Display inventories
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Ansible" in the content area
    And I follow "Inventories" in the content area
    And I wait until I see "/srv/playbooks/orion_dummy/hosts" text
    And I click on "/srv/playbooks/orion_dummy/hosts"
    Then I wait until I see "myself" text

  Scenario: Discover playbooks and display them
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Ansible" in the content area
    And I follow "Playbooks" in the content area
    And I wait until I see "/srv/playbooks" text
    And I click on "/srv/playbooks"
    Then I wait until I see "/srv/playbooks/orion_dummy/playbook_orion_dummy.yml" text

  Scenario: Run a playbook using custom inventory
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Ansible" in the content area
    And I follow "Playbooks" in the content area
    And I wait until I see "/srv/playbooks" text
    And I click on "/srv/playbooks"
    And I wait until I see "/srv/playbooks/orion_dummy/playbook_orion_dummy.yml" text
    And I click on "orion_dummy/playbook_orion_dummy.yml"
    And I wait until I see "Playbook Content" text
    And I select "/srv/playbooks/orion_dummy/hosts" from "inventory-path-select"
    And I click on "Schedule"
    Then I should see a "Playbook execution has been scheduled" text
    And I wait until event "Execute playbook 'playbook_orion_dummy.yml' scheduled" is completed
    And file "/tmp/file.txt" should exist on "ssh_minion"

  Scenario: Cleanup: Disable Ansible and remove test playbooks and inventory file
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Properties" in the content area
    And I uncheck "ansible_control_node"
    And I click on "Update Properties"
    Then I should see a "System properties changed" text
    And I remove package "orion-dummy" from this "ssh_minion" without error control
    And I remove "/tmp/file.txt" from "ssh_minion"

  @susemanager
  Scenario: Cleanup: Unsubscribe SUSE minions from SLE-Module-Python3-15-SP7-Pool for x86_64
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I uncheck "SLE-Module-Python3-15-SP7-Pool for x86_64" by label
    And I click on "Next"
    And I click on "Confirm"
    And I wait until I see "Changing the channels has been scheduled." text
    And I follow "scheduled"
    And I wait until I see "1 system successfully completed this action" text, refreshing the page

  Scenario: Cleanup: Disable "Ansible control node" system type
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Properties" in the content area
    And I uncheck "ansible_control_node"
    And I click on "Update Properties"

  Scenario: Cleanup: Apply highstate to disable the minion as an "Ansible control node"
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled" is completed

