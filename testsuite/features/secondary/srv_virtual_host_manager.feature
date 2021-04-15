# Copyright (c) 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_virtual_host_manager
Feature: Virtual host manager web UI

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Check the VHM page
    When I follow the left menu "Systems > Virtual Host Managers"
    Then I should see a "Virtual Host Managers" text
    And I should see a "No Virtual Host Managers." text

  Scenario: Check VMware page
    When I click on "Create"
    And I follow "VMWare-based"
    Then I should see a "Add a VMWare-based Virtual Host Manager" text
    And I should see a "Label" text
    And I should see a "Hostname" text
    And I should see a "Port" text
    And I should see a "Username" text
    And I should see a "Password" text

  Scenario: Create Virtual Host
    When I follow the left menu "Systems > Virtual Host Managers"
    And I click on "Create"
    When I follow "File-based"
    Then I should see a "Add a File-based Virtual Host Manager" text
    When I enter "file-vmware" as "label"
    And I enter "file:///var/tmp/vCenter.json" as "module_url"
    And I click on "Create"
    Then I should see a "file-vmware" link

  Scenario: Run virtual-host-gatherer
    When I follow the left menu "Systems > Virtual Host Managers"
    And I follow "file-vmware"
    Then I should see a "file:///var/tmp/vCenter.json" text
    And I should see a "SUSE Test" text
    When I click on "Refresh Data"
    Then I should see a "Refreshing the data for this Virtual Host Manager has been triggered." text

  Scenario: Check new virtual hosts
    Given I am on the Systems page
    And I wait until I see "10.162.186.111" text, refreshing the page
    When I follow "10.162.186.111"
    Then I should see a "OS: VMware ESXi" text
    When I follow the left menu "Systems > System List > Virtual Systems"
    Then I should see a "vCenter" text
    And I should see a "NSX-l3gateway" text

  Scenario: Delete Virtual Host Manager
    When I follow the left menu "Systems > Virtual Host Managers"
    And I follow "file-vmware"
    And I click on "Delete"
    And I wait for "1" second
    And I click on "Delete" in "Delete Virtual Host Manager" modal
    And I wait until I see "Virtual Host Manager has been deleted." text
    Then I should see a "No Virtual Host Managers." text

  Scenario: Cleanup: delete virtual host 10.162.186.111
    Given I am on the Systems page
    When I follow "10.162.186.111"
    And I follow "Delete System"
    And I click on "Delete Profile"
    And I wait until I see "has been deleted" text

  Scenario: Cleanup: delete virtual host 10.162.186.112
    Given I am on the Systems page
    When I follow "10.162.186.112"
    And I follow "Delete System"
    And I click on "Delete Profile"
    And I wait until I see "has been deleted" text
