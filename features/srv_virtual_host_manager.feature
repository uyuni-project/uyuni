# Copyright (c) 2017 SUSE LLC
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
    And I click on "Create"
    And I follow "VMWare-based"
    Then I should see a "Add a VMWare-based Virtual Host Manager" text
    And I should see a "Label" text
    And I should see a "Hostname" text
    And I should see a "Port" text
    And I should see a "Username" text
    And I should see a "Password" text

  Scenario: Check create Virtual Host
   Given I am on the Systems page
    When I follow "Virtual Host Managers"
    And I click on "Create"
    And I follow "File-based"
    Then I should see a "Add a File-based Virtual Host Manager" text
    When I enter "file-vmware" as "label"
    And I enter "file:///var/tmp/vCenter.json" as "module_url"
    And I click on "Create"
    Then I should see a "file-vmware" link

  Scenario: Run virtual-host-gatherer single run
   Given I am authorized as "admin" with password "admin"
    When I follow "Systems" in the left menu
     And I follow "Virtual Host Managers"
     And I follow "file-vmware"
    Then I should see a "file:///var/tmp/vCenter.json" text
     And I should see a "SUSE Test" text
    When I click on "Schedule refresh data"
    Then I should see a "Refreshing the data for this Virtual Host Manager has been triggered." text

  Scenario: Check new Virtual Hosts vhm page
    Given I am on the Systems page
    When I follow "10.162.186.111"
    Then I should see a "OS: VMware ESXi" text
    When I click Systems, under Systems node
    And I follow "Virtual Systems" in the left menu
    Then I should see a "vCenter" text
     And I should see a "NSX-l3gateway" text

  Scenario: Delete Virtual Host Manager
   Given I am on the Systems page
    When I follow "Virtual Host Managers"
    And I follow "file-vmware"
    And I click on "Delete"
    And I wait for "6" seconds
    And I wait until I see "Delete Virtual Host Manager" modal
    And I click on "Delete" in "Delete Virtual Host Manager" modal
    Then I should see a "Virtual Host Manager has been deleted." text
     And I should see a "No Virtual Host Managers." text

 Scenario: Cleanup: delete virtualhost: 10.162.186.111
   Given I am on the Systems page
    When I follow "10.162.186.111"
    And I follow "Delete System"
    And I click on "Delete Profile"

 Scenario: Cleanup: delete virtualhost: 10.162.186.112
   Given I am on the Systems page
    When I follow "10.162.186.112"
    And I follow "Delete System"
    And I click on "Delete Profile"
