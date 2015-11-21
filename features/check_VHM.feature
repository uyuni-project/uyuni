# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test "virtualhostmanager" Web UI.

  Scenario: Check the VHM page
    Given I am on the Systems page
    When I follow "Virtual Host Managers"
    Then I should see a "Virtual Host Managers" text
    And I should see a "No Virtual Host Managers." text

  Scenario: Check VMware page
   Given I am on the Systems page
    When I follow "Virtual Host Managers"
    And I follow "Add VMware-based Virtual Host Manager"
    Then I should see a "Add a VMware-based Virtual Host Manager" text
    And I should see a "Label:" text
    And I should see a "Hostname:" text
    And I should see a "Port:" text
    And I should see a "Username:" text
    And I should see a "Password:" text

  Scenario: Check create
   Given I am on the Systems page
    When I follow "Virtual Host Managers"
    And I follow "Add File-based Virtual Host Manager"
    Then I should see a "Add a File-based Virtual Host Manager" text
    When I enter "file-vmware" as "label"
    And I enter "file:///var/tmp/vCenter.json" as "module_url"
    And I click on "Add Virtual Host Manager"
    Then I should see a "file-vmware" link

  Scenario: Run virtual-host-gatherer single run
   Given I am authorized as "admin" with password "admin"
    When I follow "Systems" in the tabs
     And I follow "Virtual Host Managers"
     And I follow "file-vmware"
    Then I should see a "file:///var/tmp/vCenter.json" text
     And I should see a "Novell" text
    When I click on "Refresh data from this Virtual Host Manager"
    Then I should see a "Refresh for Virtual Host Manager with label 'file-vmware' was triggered." text

  Scenario: Check new Virtual Hosts
    Given I am on the Systems page
      And I follow "Systems" in the left menu
    When I follow "10.162.186.111"
    Then I should see a "OS: VMware ESXi" text
    When I follow "Virtual Systems" in the left menu
    Then I should see a "vCenter" text
     And I should see a "NSX-l3gateway" text

  Scenario: Delete Virtual Host Manager
   Given I am on the Systems page
    When I follow "Virtual Host Managers"
    And I follow "file-vmware"
    And I click on "Delete Virtual Host Manager"
    Then I should see a "Virtual Host Manager with label 'file-vmware' has been deleted." text
     And I should see a "No Virtual Host Managers." text
